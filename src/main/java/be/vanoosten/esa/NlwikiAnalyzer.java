package be.vanoosten.esa;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

// copied from DutchAnalyzer, changed StandardTokenizer to WikipediaTokenizer
public final class NlwikiAnalyzer extends Analyzer {
  
  /** File containing default Dutch stopwords. */
  public final static String DEFAULT_STOPWORD_FILE = "dutch_stop.txt";

  /**
   * Returns an unmodifiable instance of the default stop-words set.
   * @return an unmodifiable instance of the default stop-words set.
   */
  public static CharArraySet getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  
  private static class DefaultSetHolder {
    static final CharArraySet DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(SnowballFilter.class, 
            DEFAULT_STOPWORD_FILE, StandardCharsets.UTF_8), Version.LUCENE_CURRENT);
      } catch (IOException ex) {
        // default set should always be present as it is part of the
        // distribution (JAR)
        throw new RuntimeException("Unable to load default stopword set");
      }
     }
  }


  /**
   * Contains the stopwords used with the StopFilter.
   */
  private final CharArraySet stoptable;

  // null if on 3.1 or later - only for bw compat
  private final Version matchVersion;

  public NlwikiAnalyzer(){
      this(Version.LUCENE_48);
  }
  
  /**
   * Builds an analyzer with the default stop words ({@link #getDefaultStopSet()}).
   * 
   * @param matchVersion
   */
  public NlwikiAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
  }
      
  public NlwikiAnalyzer(Version matchVersion, CharArraySet stopwords) {
    this.matchVersion = matchVersion;
    this.stoptable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stopwords));
  }
  
  /**
   * Returns a (possibly reused) {@link TokenStream} which tokenizes all the 
   * text in the provided {@link Reader}.
   *
   * @return A {@link TokenStream} built from a {@link StandardTokenizer}
   *   filtered with {@link StandardFilter}, {@link LowerCaseFilter}, 
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

