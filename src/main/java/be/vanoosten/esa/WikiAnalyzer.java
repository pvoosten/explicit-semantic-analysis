package be.vanoosten.esa;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

// copied from DutchAnalyzer, changed StandardTokenizer to WikipediaTokenizer
public final class WikiAnalyzer extends Analyzer {

    /**
     * Contains the stopwords used with the StopFilter.
     */
    private final CharArraySet stoptable;

    // null if on 3.1 or later - only for bw compat
    private final Version matchVersion;

    public WikiAnalyzer(Version matchVersion, CharArraySet stopwords) {
        this.matchVersion = matchVersion;
        this.stoptable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stopwords));
    }

    /**
     * Returns a (possibly reused) {@link TokenStream} which tokenizes all the
     * text in the provided {@link Reader}.
     *
     * @param aReader
     * @return A {@link TokenStream} built from a {@link StandardTokenizer}
     * filtered with {@link StandardFilter}, {@link LowerCaseFilter},
     *   {@link StopFilter}
     */
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader aReader) {
        final Tokenizer source = new WikipediaTokenizer(aReader);
        TokenStream result = new StandardFilter(matchVersion, source);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stoptable);
        return new Analyzer.TokenStreamComponents(source, result);
    }
}
