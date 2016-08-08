package be.vanoosten.esa;

import java.io.File;
import org.apache.lucene.analysis.core.StopAnalyzer;

/**
 *
 * @author Philip van Oosten
 */
public class EnwikiFactory extends WikiFactory {

    public EnwikiFactory() {
        super(indexRootPath(),
                new File(indexRootPath(), String.join(File.separator, "dump", "enwiki-20140614-pages-articles-multistream.xml.bz2")),
                StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }

    private static File indexRootPath() {
        return new File(String.join(File.separator, "D:", "Development", "esa", "enwiki"));
    }
}
