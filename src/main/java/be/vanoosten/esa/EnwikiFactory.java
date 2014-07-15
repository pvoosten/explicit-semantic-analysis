/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa;

import be.vanoosten.esa.tools.Vectorizer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 *
 * @author user
 */
public class EnwikiFactory implements WikiFactory {

    protected File indexRootPath;
    protected File dumpFile;
    protected CharArraySet stopWords;
    protected Vectorizer vectorizer;

    private Analyzer analyzer;

    public EnwikiFactory() {
        indexRootPath = new File(String.join(File.separator, "D:", "Development", "esa", "enwiki"));
        dumpFile = new File(indexRootPath, String.join(File.separator, "dump", "enwiki-20140614-pages-articles-multistream.xml.bz2"));
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    @Override
    public File getIndexRootPath() {
        return indexRootPath;
    }

    @Override
    public File getWikipediaDumpFile() {
        return dumpFile;
    }

    @Override
    public CharArraySet getStopWords() {
        return stopWords;
    }

    @Override
    public final Analyzer getAnalyzer() {
        if (analyzer == null) {
            analyzer = new WikiAnalyzer(Version.LUCENE_48, getStopWords());
        }
        return analyzer;
    }

    @Override
    public Vectorizer getOrCreateVectorizer() {
        if (vectorizer == null) {
            try {
                vectorizer = new Vectorizer(getIndexRootPath(), getAnalyzer());
            } catch (IOException ex) {
                Logger.getLogger(EnwikiFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return vectorizer;
    }

}
