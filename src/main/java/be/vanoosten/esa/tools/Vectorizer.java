package be.vanoosten.esa.tools;

import static be.vanoosten.esa.WikiIndexer.TEXT_FIELD;
import static org.apache.lucene.util.Version.LUCENE_48;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Can present text as a vector of weighted concepts.
 *
 * @author Philip van Oosten
 */
public class Vectorizer implements AutoCloseable {

    Directory termToConceptDirectory;
    IndexReader indexReader;
    IndexSearcher searcher;
    QueryParser queryParser;
    int conceptCount;

    /**
     * Creates a new Vectorizer
     *
     * @param indexDirectory The directory where to find the indices
     * @param analyzer The analyzer to use to create search queries
     * @throws java.io.IOException
     */
    public Vectorizer(File indexDirectory, Analyzer analyzer) throws IOException {
        File termConceptDirectory = new File(indexDirectory, "termdoc");
        termToConceptDirectory = FSDirectory.open(termConceptDirectory);
        indexReader = DirectoryReader.open(termToConceptDirectory);
        searcher = new IndexSearcher(indexReader);
        queryParser = new QueryParser(LUCENE_48, TEXT_FIELD, analyzer);
        conceptCount = 100;
    }

    public ConceptVector vectorize(String text) throws ParseException, IOException {
        Query query = queryParser.parse(text);
        TopDocs td = searcher.search(query, conceptCount);
        return new ConceptVector(td, indexReader);
    }

    public int getConceptCount() {
        return conceptCount;
    }

    public void setConceptCount(int conceptCount) {
        this.conceptCount = conceptCount;
    }

    @Override
    public void close() {
        try {
            indexReader.close();
            termToConceptDirectory.close();
        } catch (IOException ex) {
            Logger.getLogger(Vectorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
