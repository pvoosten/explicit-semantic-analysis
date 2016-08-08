package be.vanoosten.esa;

import be.vanoosten.esa.tools.RelatedTokensFinder;
import be.vanoosten.esa.tools.Vectorizer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import static org.apache.lucene.util.Version.LUCENE_48;

/**
 *
 * @author Philip van Oosten
 */
public abstract class WikiFactory implements AutoCloseable {

    private Vectorizer vectorizer;
    private Analyzer analyzer;
    private RelatedTokensFinder relatedTokensFinder;
    private final File indexRootPath;
    private final File dumpFile;
    private final CharArraySet stopWords;
    private IndexSearcher relatedTermsSearcher;
    private FSDirectory relatedTermsIndex;
    private DirectoryReader relatedTermsIndexReader;

    protected WikiFactory(File indexRootPath, File dumpFile, CharArraySet stopWords) {
        this.indexRootPath = indexRootPath;
        this.dumpFile = dumpFile;
        this.stopWords = stopWords;
    }

    public final File getIndexRootPath() {
        return indexRootPath;
    }

    public final File getWikipediaDumpFile() {
        return dumpFile;
    }

    public final CharArraySet getStopWords() {
        return stopWords;
    }

    public final Analyzer getAnalyzer() {
        if (analyzer == null) {
            analyzer = new WikiAnalyzer(Version.LUCENE_48, getStopWords());
        }
        return analyzer;
    }

    public synchronized final Vectorizer getOrCreateVectorizer() {
        if (vectorizer == null) {
            try {
                vectorizer = new Vectorizer(getIndexRootPath(), getAnalyzer());
            } catch (IOException ex) {
                Logger.getLogger(EnwikiFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return vectorizer;
    }

    @Override
    public synchronized final void close() throws Exception {
        if (vectorizer != null) {
            vectorizer.close();
            vectorizer = null;
        }
        if (relatedTermsSearcher != null) {
            relatedTermsIndex.close();
            relatedTermsIndexReader.close();
            relatedTermsSearcher = null;
        }
    }

    public synchronized final IndexSearcher getOrCreateRelatedTermsSearcher() {
        if (relatedTermsSearcher == null) {
            try {
                relatedTermsIndex = FSDirectory.open(getConceptTermIndexDirectory());
                relatedTermsIndexReader = DirectoryReader.open(relatedTermsIndex);
                QueryParser conceptQueryParser = new QueryParser(LUCENE_48, WikiIndexer.TEXT_FIELD, getAnalyzer());
                relatedTermsSearcher = new IndexSearcher(relatedTermsIndexReader);
            } catch (IOException ex) {
                Logger.getLogger(WikiFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return relatedTermsSearcher;
    }

    public synchronized RelatedTokensFinder getOrCreateRelatedTokensFinder() {
        if (relatedTokensFinder == null) {
            IndexSearcher searcher = getOrCreateRelatedTermsSearcher();
            relatedTokensFinder = new RelatedTokensFinder(getOrCreateVectorizer(), relatedTermsIndexReader, relatedTermsSearcher);
        }
        return relatedTokensFinder;
    }

    public final File getConceptTermIndexDirectory() {
        return new File(getIndexRootPath(), "conceptterm");
    }

    public final File getTermDocIndexDirectory() {
        return new File(getIndexRootPath(), "termdoc");
    }
}
