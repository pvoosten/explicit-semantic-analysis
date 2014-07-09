/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteArrayDataOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 *
 * @author user
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        /*
        String indexPath = String.join(File.separator, "D:", "Development", "esa", "nlwiki");
        File wikipediaDumpFile = new File(String.join(File.separator, "D:", "Downloads", "nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
        String startTokens = "geheim anoniem auteur verhalen lezen schrijven wetenschappelijk artikel peer review";
        */
        String indexPath = String.join(File.separator, "D:", "Development", "esa", "enwiki");
        File wikipediaDumpFile = new File(indexPath, String.join(File.separator, "dump", "enwiki-20140614-pages-articles-multistream.xml.bz2"));
        String startTokens = "secret anonymous author stories read write scientific article peer review";
        CharArraySet stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

        File termDocIndexDirectory = new File(indexPath, "termdoc");
        File conceptTermIndexDirectory = new File(indexPath, "conceptterm");
                
        indexing(termDocIndexDirectory, wikipediaDumpFile, stopWords);
        searching(termDocIndexDirectory);
        createConceptTermIndex(termDocIndexDirectory, conceptTermIndexDirectory);
        for (String queryText : startTokens.split(" ")) {
            findRelatedTerms(termDocIndexDirectory, conceptTermIndexDirectory, queryText, stopWords);
            System.out.println("");
        }
    }

    static void findRelatedTerms(File termDocIndexDirectory, File conceptTermIndexDirectory, String queryText, CharArraySet stopWords) throws IOException, ParseException {
        try (
                Directory conceptIndex = FSDirectory.open(termDocIndexDirectory);
                IndexReader conceptIndexReader = DirectoryReader.open(conceptIndex);
                Directory relatedTermsIndex = FSDirectory.open(conceptTermIndexDirectory);
                IndexReader relatedTermsIndexReader = DirectoryReader.open(relatedTermsIndex);) {
            IndexSearcher conceptSearcher = new IndexSearcher(conceptIndexReader);
            Analyzer analyzer = new WikiAnalyzer(Version.LUCENE_48, stopWords);
            QueryParser conceptQueryParser = new QueryParser(Version.LUCENE_48, TermIndexWriter.TEXT_FIELD, analyzer);

            IndexSearcher relatedTermsSearcher = new IndexSearcher(relatedTermsIndexReader);

            Query conceptQuery = conceptQueryParser.parse(queryText);
            System.out.println(String.format("%s[shape=box, color=\"red\"];", queryText));
            BooleanQuery relatedTermsQuery = new BooleanQuery();
            TopDocs topDocs = conceptSearcher.search(conceptQuery, 100);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                String concept = conceptIndexReader.document(sd.doc).get(TermIndexWriter.TITLE_FIELD);
                TermQuery conceptAsTermQuery = new TermQuery(new Term("concept", concept));
                conceptAsTermQuery.setBoost(sd.score);
                relatedTermsQuery.add(conceptAsTermQuery, Occur.SHOULD);
            }
            TopDocs topTerms = relatedTermsSearcher.search(relatedTermsQuery, 20);
            // System.out.println("A total of " + topTerms.totalHits + " related terms found.");
            for (ScoreDoc sd : topTerms.scoreDocs) {
                String term = relatedTermsIndexReader.document(sd.doc).get(TermIndexWriter.TEXT_FIELD);
                System.out.println(String.format(Locale.US, "%s -- %s [label=%.3f];", queryText, term, sd.score));
            }
        }
    }

    static void createConceptTermIndex(File termDocIndexDirectory, File conceptTermIndexDirectory) throws IOException {
        ExecutorService es = Executors.newFixedThreadPool(2);

        final Directory termDocDirectory = FSDirectory.open(termDocIndexDirectory);
        final IndexReader termDocReader = IndexReader.open(termDocDirectory);
        final IndexSearcher docSearcher = new IndexSearcher(termDocReader);

        Fields fields = MultiFields.getFields(termDocReader);
        if (fields != null) {
            Terms terms = fields.terms(TermIndexWriter.TEXT_FIELD);
            TermsEnum termsEnum = terms.iterator(null);

            final IndexWriterConfig conceptIndexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, null);
            try (IndexWriter conceptIndexWriter = new IndexWriter(FSDirectory.open(conceptTermIndexDirectory), conceptIndexWriterConfig)) {
                int t = 0;
                BytesRef bytesRef;
                while ((bytesRef = termsEnum.next()) != null) {
                    String termString = bytesRef.utf8ToString();
                    if (termString.matches("^[a-zA-Z]+:/.*$") || termString.matches("^\\d+$")) {
                        continue;
                    }
                    if (termString.charAt(0) >= '0' && termString.charAt(0) <= '9') {
                        continue;
                    }
                    if (termString.contains(".") || termString.contains("_")) {
                        continue;
                    }
                    if (t++ == 1000) {
                        t = 0;
                        System.out.println(termString);
                    }
                    TopDocs td = SearchTerm(bytesRef, docSearcher);

                    // add the concepts to the token stream
                    byte[] payloadBytes = new byte[5];
                    ByteArrayDataOutput dataOutput = new ByteArrayDataOutput(payloadBytes);
                    ProducerConsumerTokenStream pcTokenStream = new ProducerConsumerTokenStream();
                    double norm = ConceptSimilarity.SIMILARITY_FACTOR;
                    int last = 0;
                    for(ScoreDoc scoreDoc : td.scoreDocs){
                        if(scoreDoc.score/norm < ConceptSimilarity.SIMILARITY_FACTOR ||
                                last>= 1.0f / ConceptSimilarity.SIMILARITY_FACTOR) break;
                        norm += scoreDoc.score * scoreDoc.score;
                        last++;
                    }
                    for (int i=0; i<last; i++) {
                        ScoreDoc scoreDoc = td.scoreDocs[i];
                        Document termDocDocument = termDocReader.document(scoreDoc.doc);
                        String concept = termDocDocument.get(TermIndexWriter.TITLE_FIELD);
                        Token conceptToken = new Token(concept, i * 10, (i + 1) * 10, "CONCEPT");
                        // set similarity score as payload
                        int integerScore = (int) ((scoreDoc.score/norm)/ConceptSimilarity.SIMILARITY_FACTOR);
                        dataOutput.reset(payloadBytes);
                        dataOutput.writeVInt(integerScore);
                        BytesRef payloadBytesRef = new BytesRef(payloadBytes, 0, dataOutput.getPosition());
                        conceptToken.setPayload(payloadBytesRef);
                        pcTokenStream.produceToken(conceptToken);
                    }
                    pcTokenStream.finishProducingTokens();

                    Document conceptTermDocument = new Document();
                    AttributeSource attributeSource = termsEnum.attributes();
                    conceptTermDocument.add(new StringField(TermIndexWriter.TEXT_FIELD, termString, Field.Store.YES));
                    conceptTermDocument.add(new TextField("concept", pcTokenStream));
                    conceptIndexWriter.addDocument(conceptTermDocument);
                }
            }
        }
    }

    private static TopDocs SearchTerm(BytesRef bytesRef, IndexSearcher docSearcher) throws IOException {
        Term term = new Term(TermIndexWriter.TEXT_FIELD, bytesRef);
        Query query = new TermQuery(term);
        int n = 1000;
        TopDocs td = docSearcher.search(query, n);
        if (n < td.totalHits) {
            n = td.totalHits;
            td = docSearcher.search(query, n);
        }
        return td;
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

    private static void searchForQuery(final QueryParser parser, final IndexSearcher searcher, final String queryString, final IndexReader indexReader) throws ParseException, IOException {
        Query query = parser.parse(queryString);
        TopDocs topDocs = searcher.search(query, 12);
        System.out.println(String.format("%d hits voor \"%s\"", topDocs.totalHits, queryString));
        for (ScoreDoc sd : topDocs.scoreDocs) {
            System.out.println(String.format("doc %d score %.2f shardIndex %d title \"%s\"", sd.doc, sd.score, sd.shardIndex, indexReader.document(sd.doc).get(TermIndexWriter.TITLE_FIELD)));
        }
    }

    public static void indexing(File termDocIndexDirectory, File wikipediaDumpFile, CharArraySet stopWords) throws IOException {
        try (Directory directory = FSDirectory.open(termDocIndexDirectory)) {
            WikiIndexer indexer = new WikiIndexer();
            Analyzer analyzer = new WikiAnalyzer(Version.LUCENE_48, stopWords);
            try (TermIndexWriter termIndexWriter = new TermIndexWriter(analyzer, directory)) {
                indexer.setTermIndexWriter(termIndexWriter);
                indexer.parseXmlDump(wikipediaDumpFile);
            }
        }
    }
}
