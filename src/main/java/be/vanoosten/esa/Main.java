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
        String indexPath = String.join(File.separator, "D:", "Development", "esa", "nlwiki");
        File indexDirectory = new File(indexPath);

        // indexing(indexDirectory);
        searching(indexDirectory);
    }
    
    public static void searching(File indexDirectory) throws IOException, ParseException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        
        try (IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexDirectory))) {
            IndexSearcher searcher = new IndexSearcher(indexReader, executorService);
            
            Analyzer analyzer = new DutchAnalyzer(Version.LUCENE_48);
            QueryParser parser = new QueryParser(Version.LUCENE_48, TermIndexWriter.TEXT_FIELD, analyzer);
            
            // searchForQuery(parser, searcher, "anoniem", indexReader);
            // searchForQuery(parser, searcher, "verborgen kennis", indexReader);
            // searchForQuery(parser, searcher, "korte verhalen kortverhalen", indexReader);
            // searchForQuery(parser, searcher, "essentie van verhalen", indexReader);
            // searchForQuery(parser, searcher, "wijsheid", indexReader);
            
            searchForQuery(parser, searcher, "wezenlijkheid -potter -smurf -\"lijst van\"", indexReader);
            searchForQuery(parser, searcher, "verborgen verleden", indexReader);
            searchForQuery(parser, searcher, "verborgen verhalen", indexReader);
            searchForQuery(parser, searcher, "levenswijsheid vermogen en wijsheid -sirach", indexReader);
            
        }
    }

    private static void searchForQuery(QueryParser parser, IndexSearcher searcher, String queryString, final IndexReader indexReader) throws ParseException, IOException {
        Query query = parser.parse(queryString);
        TopDocs topDocs = searcher.search(query, 12);
        System.out.println(String.format("%d hits voor \"%s\"",topDocs.totalHits, queryString));
        for(ScoreDoc sd : topDocs.scoreDocs){
            System.out.println(String.format("doc %d score %.2f shardIndex %d title \"%s\"", sd.doc, sd.score, sd.shardIndex, indexReader.document(sd.doc).get(TermIndexWriter.TITLE_FIELD)));
        }
    }
    
    public static void indexing(File indexDirectory) throws IOException {
        try (Directory directory = FSDirectory.open(indexDirectory)) {
            WikiIndexer indexer = new WikiIndexer();
            Analyzer analyzer = new NlwikiAnalyzer();
            try (TermIndexWriter termIndexWriter = new TermIndexWriter(analyzer, directory)) {
                indexer.setTermIndexWriter(termIndexWriter);
                indexer.parseXmlDump(String.join(File.separator, "D:", "Downloads","nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
            }
        }
    }
}
####

Een index van alle wikipedia-artikels alleen is genoeg om te verifiëren of termen/teksten semantisch aan elkaar verwant zijn.

Om actief naar termen te zoeken die verwant zijn aan verschillende andere termen, is er nog een beetje meer werk nodig dan maken van de term-concept-index,
nl. een concept-term index. De concept-term index mag ook frasen bevatten, ipv enkel termen.

Om de concept-term-index op te bouwen:
- overloop alle termen/frasen
- Zoek de concepten die erbij passen. Cutoff op similarity of top n of ???
- Maak een document met term als titel/id en de gevonden concepten als tokens.

Om van een term naar gerelateerde termen te gaan:
- zoek term in de term-concept index ==> concepten
- zoek concept in de concept-term index ==> andere termen
- Geef als score aan de gevonden termen de score van term-concept search maal score uit concept-term search.
- Eventueel de scores normaliseren, maar uiteindelijk is toch vooral de volgorde van belang.

