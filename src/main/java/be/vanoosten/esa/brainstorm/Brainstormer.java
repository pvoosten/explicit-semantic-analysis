/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa.brainstorm;

import be.vanoosten.esa.WikiFactory;
import be.vanoosten.esa.tools.RelatedTokensFinder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author user
 */
public class Brainstormer {

    public static final String SCORE_PROPERTY = "score";
    public static final String RELATED_EDGE = "related";
    public static final String ERROR_EDGE = "error";

    private final Graph g;
    private final WikiFactory wikiFactory;
    private final Vertex[] startTokenVertices;

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
        List<Vertex> endPoints = new ArrayList<>();
        endPoints.addAll(Arrays.asList(startTokenVertices));
        // Until end condition reached, find new tokens
        RelatedTokensFinder relatedTokensFinder = wikiFactory.getOrCreateRelatedTokensFinder();
        do {
            List<Vertex> newEndPoints = new ArrayList<>();
            for (Vertex endPoint : endPoints) {
                String endPointToken = (String) endPoint.getId();
                try {
                    List<Entry<String, Float>> relatedTerms = relatedTokensFinder.findRelatedTerms(endPointToken, breadth);
                    for (Entry<String, Float> entry : relatedTerms) {
                        String token = entry.getKey();
                        float score = entry.getValue();
                        Vertex newTokenVertex = g.getVertex(token);
                        if (newTokenVertex == null) {
                            newTokenVertex = g.addVertex(token);
                            newEndPoints.add(newTokenVertex);
                        }
                        Edge e = endPoint.addEdge(RELATED_EDGE, newTokenVertex);
                        e.setProperty(SCORE_PROPERTY, score);
                    }
                } catch (ParseException | IOException ex) {
                    Logger.getLogger(Brainstormer.class.getName()).log(Level.SEVERE, null, ex);
                    Edge e = endPoint.addEdge(ERROR_EDGE, getErrorVertex());
                    e.setProperty("message", ex.getMessage());
                }
            }
            endPoints = newEndPoints;
        } while (depth-- > 0);

        // Until no more tokens are branched, branch tokens with degree 1
        // start with endPoints, which contains all the leaves.
        while (!endPoints.isEmpty()) {
            Vertex endPoint = endPoints.get(0);
            long degree = endPoint.getEdges(Direction.BOTH, RELATED_EDGE).spliterator().getExactSizeIfKnown();
            if (degree == 1) {
                Edge edge = endPoint.getEdges(Direction.BOTH, RELATED_EDGE).iterator().next();
                Vertex newEndpoint = edge.getVertex(Direction.IN) == endPoint ? endPoint : edge.getVertex(Direction.OUT);
                endPoints.add(newEndpoint);
                g.removeEdge(edge);
                g.removeVertex(endPoint);
            }
        }
    }

    private Vertex getErrorVertex() {
        Vertex errorVertex = g.getVertex("error");
        if (errorVertex == null) {
            errorVertex = g.addVertex("error");
        }
        return errorVertex;
    }

    private Vertex[] addStartTokens(String[] startTokens) {
        // Add start tokens
        Vertex start = g.addVertex("* * * START * * *");
        Vertex[] startTokenVertices = new Vertex[startTokens.length];
        for (int i = 0; i < startTokens.length; i++) {
            startTokenVertices[i] = g.addVertex(startTokens[i]);
            Edge e = start.addEdge("start", startTokenVertices[i]);
        }
        return startTokenVertices;
    }
}
