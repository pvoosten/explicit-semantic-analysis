package be.vanoosten.esa.tools;

import be.vanoosten.esa.WikiIndexer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author Philip van Oosten
 */
public class RelatedTokensFinder {

    private final Vectorizer vectorizer;
    private final IndexSearcher relatedTermsSearcher;
    private final IndexReader relatedTermsIndexReader;

    public RelatedTokensFinder(final Vectorizer vectorizer, IndexReader relatedTermsIndexReader, IndexSearcher relatedTermsSearcher) {
        this.vectorizer = vectorizer;
        this.relatedTermsSearcher = relatedTermsSearcher;
        this.relatedTermsIndexReader = relatedTermsIndexReader;
    }

    public List<Map.Entry<String, Float>> findRelatedTerms(String queryText, int n) throws ParseException, IOException {
        ConceptVector vec = vectorizer.vectorize(queryText);
        Query relatedTermsQuery = vec.asQuery();
        TopDocs topTerms = relatedTermsSearcher.search(relatedTermsQuery, n);
        List<Map.Entry<String, Float>> tokens = new ArrayList<>(n);
        for (ScoreDoc sd : topTerms.scoreDocs) {
            String term = relatedTermsIndexReader.document(sd.doc).get(WikiIndexer.TEXT_FIELD);
            tokens.add(new Pair(term, sd.score));
        }
        return tokens;
    }

    static class Pair implements Map.Entry<String, Float> {

        private final String key;
        private final float value;

        public Pair(String key, float value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Float getValue() {
            return value;
        }

        @Override
        public Float setValue(Float value) {
            throw new UnsupportedOperationException("Altering values is not supported.");
        }
    }
}
