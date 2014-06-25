/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author user
 */
public class Main {
    
    public static void main(String[] args) throws IOException, ParseException {
        indexing();
        searching();
    }
    
    public static void searching() throws IOException, ParseException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        
        String indexPath = String.join(File.separator, "D:", "Development", "esa");
        File indexDirectory = new File(indexPath);
        try (IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexDirectory))) {
            IndexSearcher searcher = new IndexSearcher(indexReader, executorService);
            
            Analyzer analyzer = new DutchAnalyzer(Version.LUCENE_48);
            QueryParser parser = new QueryParser(Version.LUCENE_48, TermIndexWriter.TEXT_FIELD, analyzer);
            Query query = parser.parse("Galapagosreuzenschildpad kan erg groot worden");
            
            TopDocs topDocs = searcher.search(query, 10);
            
            System.out.println("total hits: " + topDocs.totalHits);
            for(ScoreDoc sd : topDocs.scoreDocs){
                System.out.println(String.format("doc {} score {} shardIndex {} title {}", sd.doc, sd.score, sd.shardIndex, indexReader.document(sd.doc).get(TermIndexWriter.TITLE_FIELD)));
            }
        }
    }
    
    public static void indexing() throws IOException {
        String directoryPath = String.join(File.separator, "D:", "Development", "esa");
        File directoryFile = new File(directoryPath);
        try (Directory directory = FSDirectory.open(directoryFile)) {
            WikiIndexer indexer = new WikiIndexer();
            Analyzer analyzer = new NlwikiAnalyzer();
            TermIndexWriter termIndexWriter = new TermIndexWriter(analyzer, directory);
            indexer.setTermIndexWriter(termIndexWriter);
            indexer.parseXmlDump(String.join(File.separator, "D:", "Downloads","nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
        }
    }
}
