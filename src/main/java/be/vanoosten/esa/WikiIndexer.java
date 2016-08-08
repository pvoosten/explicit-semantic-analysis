package be.vanoosten.esa;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Philip van Oosten
 */
public class WikiIndexer extends DefaultHandler implements AutoCloseable {

    private final SAXParserFactory saxFactory;
    private boolean inPage;
    private boolean inPageTitle;
    private boolean inPageText;
    private StringBuilder content = new StringBuilder();
    private String wikiTitle;
    private int numIndexed = 0;
    private int numTotal = 0;

    public static final String TEXT_FIELD = "text";
    public static final String TITLE_FIELD = "title";
    Pattern pat;

    IndexWriter indexWriter;

    int minimumArticleLength;

    /**
     * Gets the minimum length of an article in characters that should be
     * indexed.
     *
     * @return
     */
    public int getMinimumArticleLength() {
        return minimumArticleLength;
    }

    /**
     * Sets the minimum length of an article in characters for it to be indexed.
     *
     * @param minimumArticleLength
     */
    public final void setMinimumArticleLength(int minimumArticleLength) {
        this.minimumArticleLength = minimumArticleLength;
    }

    public WikiIndexer(Analyzer analyzer, Directory directory) throws IOException {
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(true);
        saxFactory.setXIncludeAware(true);

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriter = new IndexWriter(directory, indexWriterConfig);
        String regex = "^[a-zA-z]+:.*";
        pat = Pattern.compile(regex);
        setMinimumArticleLength(2000);
    }

    public void parseXmlDump(String path) {
        parseXmlDump(new File(path));
    }

    public void parseXmlDump(File file) {
        try {
            SAXParser saxParser = saxFactory.newSAXParser();
            InputStream wikiInputStream = new FileInputStream(file);
            wikiInputStream = new BufferedInputStream(wikiInputStream);
            wikiInputStream = new BZip2CompressorInputStream(wikiInputStream, true);
            saxParser.parse(wikiInputStream, this);
        } catch (ParserConfigurationException | SAXException | FileNotFoundException ex) {
            Logger.getLogger(WikiIndexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("page".equals(localName)) {
            inPage = true;
        } else if (inPage && "title".equals(localName)) {
            inPageTitle = true;
            content = new StringBuilder();
        } else if (inPage && "text".equals(localName)) {
            inPageText = true;
            content = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inPage && inPageTitle && "title".equals(localName)) {
            inPageTitle = false;
            wikiTitle = content.toString();
        } else if (inPage && inPageText && "text".equals(localName)) {
            inPageText = false;
            String wikiText = content.toString();
            try {
                numTotal++;
                if (index(wikiTitle, wikiText)) {
                    numIndexed++;
                    if (numIndexed % 1000 == 0) {
                        System.out.println("" + numIndexed + "\t/ " + numTotal + "\t" + wikiTitle);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (inPage && "page".equals(localName)) {
            inPage = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    boolean index(String title, String wikiText) throws IOException {
        Matcher matcher = pat.matcher(title);
        if (matcher.find() || title.startsWith("Lijst van ") || wikiText.length() < getMinimumArticleLength()) {
            return false;
        }
        Document doc = new Document();
        doc.add(new StoredField(TITLE_FIELD, title));
        Analyzer analyzer = indexWriter.getAnalyzer();
        doc.add(new TextField(TEXT_FIELD, wikiText, Field.Store.NO));
        indexWriter.addDocument(doc);
        return true;
    }

    @Override
    public void close() throws IOException {
        indexWriter.close();
    }

}
