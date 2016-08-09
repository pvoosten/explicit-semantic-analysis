package be.vanoosten.esa;

import static be.vanoosten.esa.WikiIndexer.TITLE_FIELD;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.core.StopAnalyzer;
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
import static org.apache.lucene.util.Version.LUCENE_48;

/**
 *
 * @author Philip van Oosten
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        /*
        String indexPath = String.join(File.separator, "D:", "Development", "esa", "nlwiki");
        File wikipediaDumpFile = new File(String.join(File.separator, "D:", "Downloads", "nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
        String startTokens = "geheim anoniem auteur verhalen lezen schrijven wetenschappelijk artikel peer review";
        */
        WikiFactory factory = new EnwikiFactory();
        File indexPath = factory.getIndexRootPath();
        File wikipediaDumpFile = factory.getWikipediaDumpFile();
        String startTokens = "secret anonymous author stories read write scientific article peer review";
        CharArraySet stopWords = factory.getStopWords();

        File termDocIndexDirectory = factory.getTermDocIndexDirectory();
        File conceptTermIndexDirectory = factory.getConceptTermIndexDirectory();

        // The following lines are commented, because they can take a looong time.
        // indexing(termDocIndexDirectory, wikipediaDumpFile, stopWords);
        // createConceptTermIndex(termDocIndexDirectory, conceptTermIndexDirectory);
    }

    /**
     * Creates a concept-term index from a term-to-concept index (a full text index of a Wikipedia dump).
     * @param termDocIndexDirectory The directory that contains the term-to-concept index, which is created by {@code indexing()} or in a similar fashion.
     * @param conceptTermIndexDirectory The directory that shall contain the concept-term index.
     * @throws IOException
     */
    static void createConceptTermIndex(File termDocIndexDirectory, File conceptTermIndexDirectory) throws IOException {
        ExecutorService es = Executors.newFixedThreadPool(2);

        final Directory termDocDirectory = FSDirectory.open(termDocIndexDirectory);
        final IndexReader termDocReader = IndexReader.open(termDocDirectory);
        final IndexSearcher docSearcher = new IndexSearcher(termDocReader);

        Fields fields = MultiFields.getFields(termDocReader);
        if (fields != null) {
            Terms terms = fields.terms(WikiIndexer.TEXT_FIELD);
            TermsEnum termsEnum = terms.iterator(null);

            final IndexWriterConfig conceptIndexWriterConfig = new IndexWriterConfig(LUCENE_48, null);
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
                    CachingTokenStream pcTokenStream = new CachingTokenStream();
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
                        String concept = termDocDocument.get(WikiIndexer.TITLE_FIELD);
                        Token conceptToken = new Token(concept, i * 10, (i + 1) * 10, "CONCEPT");
                        // set similarity score as payload
                        int integerScore = (int) ((scoreDoc.score/norm)/ConceptSimilarity.SIMILARITY_FACTOR);
                        dataOutput.reset(payloadBytes);
                        dataOutput.writeVInt(integerScore);
                        BytesRef payloadBytesRef = new BytesRef(payloadBytes, 0, dataOutput.getPosition());
                        conceptToken.setPayload(payloadBytesRef);
                        pcTokenStream.produceToken(conceptToken);
                    }

                    Document conceptTermDocument = new Document();
                    AttributeSource attributeSource = termsEnum.attributes();
                    conceptTermDocument.add(new StringField(WikiIndexer.TEXT_FIELD, termString, Field.Store.YES));
                    conceptTermDocument.add(new TextField("concept", pcTokenStream));
                    conceptIndexWriter.addDocument(conceptTermDocument);
                }
            }
        }
    }

    private static TopDocs SearchTerm(BytesRef bytesRef, IndexSearcher docSearcher) throws IOException {
        Term term = new Term(WikiIndexer.TEXT_FIELD, bytesRef);
        Query query = new TermQuery(term);
        int n = 1000;
        TopDocs td = docSearcher.search(query, n);
        if (n < td.totalHits) {
            n = td.totalHits;
            td = docSearcher.search(query, n);
        }
        return td;
    }

    private static void searchForQuery(final QueryParser parser, final IndexSearcher searcher, final String queryString, final IndexReader indexReader) throws ParseException, IOException {
        Query query = parser.parse(queryString);
        TopDocs topDocs = searcher.search(query, 12);
        System.out.println(String.format("%d hits voor \"%s\"", topDocs.totalHits, queryString));
        for (ScoreDoc sd : topDocs.scoreDocs) {
            System.out.println(String.format("doc %d score %.2f shardIndex %d title \"%s\"", sd.doc, sd.score, sd.shardIndex, indexReader.document(sd.doc).get(TITLE_FIELD)));
        }
    }

    /**
     * Creates a term to concept index from a Wikipedia article dump.
     * @param termDocIndexDirectory The directory where the term to concept index must be created
     * @param wikipediaDumpFile The Wikipedia dump file that must be read to create the index
     * @param stopWords The words that are not used in the semantic analysis
     * @throws IOException
     */
    public static void indexing(File termDocIndexDirectory, File wikipediaDumpFile, CharArraySet stopWords) throws IOException {
        try (Directory directory = FSDirectory.open(termDocIndexDirectory)) {
            Analyzer analyzer = new WikiAnalyzer(LUCENE_48, stopWords);
            try(WikiIndexer indexer = new WikiIndexer(analyzer, directory)){
                indexer.parseXmlDump(wikipediaDumpFile);
            }
        }
    }
}
