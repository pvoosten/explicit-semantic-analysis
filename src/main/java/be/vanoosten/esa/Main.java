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
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 *
 * @author user
 */
public class Main {
    
    public static void main(String[] args) throws IOException, ParseException {
        String indexPath = String.join(File.separator, "D:", "Development", "esa", "nlwiki");
        File termDocIndexDirectory = new File(indexPath, "termdoc");
        File conceptTermIndexDirectory = new File(indexPath, "conceptterm");

        indexing(termDocIndexDirectory);
        searching(termDocIndexDirectory);
        createConceptTermIndex(termDocIndexDirectory, conceptTermIndexDirectory);
    }
    
    static void createConceptTermIndex(File termDocIndexDirectory, File conceptTermIndexDirectory) throws IOException{
        Directory termDocDirectory = FSDirectory.open(termDocIndexDirectory);
        IndexReader termDocReader = IndexReader.open(termDocDirectory);
        Fields fds = termDocReader.getTermVectors(100);
        IndexSearcher docSearcher = new IndexSearcher(termDocReader);
        Terms terms = SlowCompositeReaderWrapper.wrap(termDocReader).terms(TermIndexWriter.TEXT_FIELD);
        TermsEnum termsEnum = terms.iterator(TermsEnum.EMPTY);
        BytesRef bytesRef = termsEnum.term();
        while(bytesRef != null){
            Term term = new Term(TermIndexWriter.TEXT_FIELD, bytesRef);
            Query query = new TermQuery(term);
            int n = 1000;
            TopDocs td = docSearcher.search(query, n);
            if(n<td.totalHits){
                n = td.totalHits;
                td = docSearcher.search(query, n);
            }
            Document conceptTermDocument = new Document();
            
            // add the term field
            conceptTermDocument.add(new StringField(TermIndexWriter.TEXT_FIELD, bytesRef.utf8ToString(), Field.Store.YES));
            // add the term
            ProducerConsumerTokenStream pcTokenStream = new ProducerConsumerTokenStream();
            for(ScoreDoc scoreDoc : td.scoreDocs){
                Document termDocDocument = termDocReader.document(scoreDoc.doc);
                String concept = termDocDocument.get(TermIndexWriter.TITLE_FIELD);
                Token conceptToken = new Token(concept, 0, 10, "CONCEPT");
                pcTokenStream.produceToken(conceptToken);
            }
            pcTokenStream.finishProducingTokens();
            
            bytesRef = termsEnum.next();
        }
    }
    
    public static void searching(File termDocIndexDirectory) throws IOException, ParseException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        
        try (IndexReader indexReader = DirectoryReader.open(FSDirectory.open(termDocIndexDirectory))) {
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
    
    public static void indexing(File termDocIndexDirectory) throws IOException {
        try (Directory directory = FSDirectory.open(termDocIndexDirectory)) {
            WikiIndexer indexer = new WikiIndexer();
            Analyzer analyzer = new NlwikiAnalyzer();
            try (TermIndexWriter termIndexWriter = new TermIndexWriter(analyzer, directory)) {
                indexer.setTermIndexWriter(termIndexWriter);
                indexer.parseXmlDump(String.join(File.separator, "D:", "Downloads","nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
            }
        }
    }
}
