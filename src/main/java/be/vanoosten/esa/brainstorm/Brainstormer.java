package be.vanoosten.esa.brainstorm;

import be.vanoosten.esa.WikiFactory;
import be.vanoosten.esa.tools.RelatedTokensFinder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Philip van Oosten
 */
public class Brainstormer {

    public static final String SCORE_PROPERTY = "score";
    public static final String RELATED_EDGE = "related";
    public static final String ERROR_EDGE = "error";
    public static final String START_VERTEX = "start";
    public static final String TOKEN_PROPERTY = "token";

    private final Graph g;
    private final WikiFactory wikiFactory;
    private final Integer[] startTokenVertices;

    private int lastVertexId = 0;

    /**
     *
     * @param wikiFactory the factory to create all required resources from
     * @param breadth the maximum number of related tokens per token
     * @param depth the maximum number of tries
     * @param startTokens the tokens to start searching from
     */
    public Brainstormer(WikiFactory wikiFactory, int breadth, int depth, String... startTokens) {
        this.wikiFactory = wikiFactory;
        g = new TinkerGraph();

        startTokenVertices = addStartTokens(startTokens);

        // mark the start token vertices as end points
        List<Integer> endPoints = Arrays.asList(startTokenVertices);
        // Until end condition reached, find new tokens
        RelatedTokensFinder relatedTokensFinder = wikiFactory.getOrCreateRelatedTokensFinder();
        do {
            List<Integer> newEndPoints = new ArrayList<>();
            for (Integer endPoint : endPoints) {
                String endPointToken = g.getVertex(endPoint).getProperty(TOKEN_PROPERTY).toString();
                try {
                    List<Entry<String, Float>> relatedTerms = relatedTokensFinder.findRelatedTerms(endPointToken, breadth);
                    for (Entry<String, Float> entry : relatedTerms) {
                        String token = entry.getKey();
                        int id = lastVertexId;
                        Integer newTokenVertex = addTokenVertex(token);
                        float score = entry.getValue();
                        if (id != lastVertexId) {
                            // the token was added
                            newEndPoints.add(newTokenVertex);
                        }
                        Edge e = addEdge(endPoint, newTokenVertex, RELATED_EDGE);
                        e.setProperty(SCORE_PROPERTY, score);
                    }
                } catch (ParseException | IOException ex) {
                    Logger.getLogger(Brainstormer.class.getName()).log(Level.SEVERE, null, ex);
                    Vertex v = g.getVertex(endPoint);
                    Edge e = v.addEdge(ERROR_EDGE, getErrorVertex());
                    e.setProperty("message", ex.getMessage());
                }
            }
            endPoints = newEndPoints;
        } while (depth-- > 0);

        // Until no more tokens are branched, branch tokens with degree 1
        // start with endPoints, which contains all the leaves.
        while (!endPoints.isEmpty()) {
            Integer endPoint = endPoints.get(0);
            Vertex vEnd = g.getVertex(endPoint);
            if(vEnd == null){
                endPoints.remove(0);
                continue;
            }
            Counter c = new Counter();
            vEnd.getEdges(Direction.BOTH, RELATED_EDGE).spliterator().forEachRemaining(e -> c.count());
            int degree = c.c;
            if (degree == 1) {
                Edge edge = vEnd.getEdges(Direction.BOTH, RELATED_EDGE).iterator().next();
                Vertex newEndpoint = edge.getVertex(Direction.IN) == vEnd ? edge.getVertex(Direction.OUT) : edge.getVertex(Direction.IN);
                endPoints.add(Integer.parseInt(newEndpoint.getId().toString()));
                g.removeEdge(edge);
                g.removeVertex(vEnd);
            } else if (degree > 1){
                endPoints.remove(0);
            }
        }
    }

    private Edge addEdge(int fromVertexId, int toVertexId, String type) {
        return g.getVertex(fromVertexId).addEdge(type, g.getVertex(toVertexId));
    }

    private int addTokenVertex(final String token) {
        return addTokenVertex(token, false);
    }
    
    private class Counter{
        int c = 0;
        void count(){
            c++;
        }
    }

    private int addTokenVertex(final String token, final boolean startToken) {
        Iterable<Vertex> verts = g.getVertices(TOKEN_PROPERTY, token);
        Counter c = new Counter();
        verts.spliterator().forEachRemaining(v -> c.count());
        final int count = c.c;
        int vertexId = -1;
        if (count <= 0L) {
            lastVertexId++;
            vertexId = lastVertexId;
            Vertex v = g.addVertex(vertexId);
            v.setProperty(TOKEN_PROPERTY, token);
            v.setProperty(START_VERTEX, startToken);
        } else if (count == 1L) {
            vertexId = Integer.parseInt(verts.iterator().next().getId().toString());
        } else {
            throw new IllegalStateException("There are multiple vertices with the same token");
        }
        return vertexId;
    }

    private Vertex getErrorVertex() {
        Vertex errorVertex = g.getVertex("error");
        if (errorVertex == null) {
            errorVertex = g.addVertex("error");
        }
        return errorVertex;
    }

    private Integer[] addStartTokens(final String[] startTokens) {
        // Add start tokens
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        Integer[] startTokenVertices = new Integer[startTokens.length];
        for (int i = 0; i < startTokens.length; i++) {
            startTokenVertices[i] = addTokenVertex(startTokens[i], true);
        }
        return startTokenVertices;
    }

    public String toNeatoScript() {
        StringBuilder buf = new StringBuilder();
        buf.append("graph g {\nnode[shape=box];\n");
        writeNeatoVertices(buf, true);
        writeNeatoVertices(buf, false);
        writeNeatoEdges(buf);
        buf.append("}");
        return buf.toString();
    }

    void writeNeatoEdges(StringBuilder buf){
        for(Edge e : g.getEdges()){
            if(RELATED_EDGE.equals(e.getLabel())){
                String from = e.getVertex(Direction.OUT).getId().toString();
                String to = e.getVertex(Direction.IN).getId().toString();
                float score = e.getProperty(Brainstormer.SCORE_PROPERTY);
                buf.append(from).append(" -- ").append(to).append(" [label=\"").append(score).append("\"];").append('\n');
            }
        }
    }
    
    void writeNeatoVertices(StringBuilder buf, boolean startToken) {
        for (Vertex v : g.getVertices(START_VERTEX, startToken)) {
            int id = Integer.parseInt(v.getId().toString());
            String token = (String) v.getProperty(TOKEN_PROPERTY);
            writeNeatoVertex(buf, id, token, startToken);
        }
    }

    private void writeNeatoVertex(StringBuilder buf, int id, String token, boolean startToken) {
        buf.append(id).append(startToken?"[color=red, label=\"" : "[label=\"").append(token).append("\"];");
        buf.append('\n');
    }
}
