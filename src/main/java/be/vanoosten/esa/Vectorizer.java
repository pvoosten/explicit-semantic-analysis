/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.File;
import java.io.IOException;
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
import org.apache.lucene.util.Version;

/**
 * Can present text as a vector of weighted concepts.
 * @author PvO
 */
public class Vectorizer implements AutoCloseable{

    
    Directory termToConceptDirectory;
    IndexReader indexReader;
    IndexSearcher searcher;
    QueryParser queryParser;
    
    /**
     * Creates a new Vectorizer
     * @param indexDirectory The directory where to find the indices
     * @param analyzer The analyzer to use to create search queries
     * @throws java.io.IOException
     */
    public Vectorizer(File indexDirectory, Analyzer analyzer) throws IOException {
        File termConceptDirectory = new File(indexDirectory, "termdoc");
        termToConceptDirectory = FSDirectory.open(termConceptDirectory);
        indexReader = DirectoryReader.open(termToConceptDirectory);
        searcher = new IndexSearcher(indexReader);
        queryParser = new QueryParser(Version.LUCENE_48, TermIndexWriter.TEXT_FIELD, analyzer);
    }
    
    public ConceptVector vectorize(String text) throws ParseException, IOException{
        Query query = queryParser.parse(text);
        TopDocs td = searcher.search(query, 100);
        if(td.totalHits > 100){
            td = searcher.search(query, td.totalHits);
        }
        return new ConceptVector(td, indexReader);
    }

    @Override
    public void close() throws Exception {
        indexReader.close();
        termToConceptDirectory.close();
    }
    
    
    
    
    
}
