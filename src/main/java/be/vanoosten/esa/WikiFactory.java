/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa;

import be.vanoosten.esa.tools.Vectorizer;
import java.io.File;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author user
 */
public interface WikiFactory {

    File getIndexRootPath();

    File getWikipediaDumpFile();

    CharArraySet getStopWords();

    Analyzer getAnalyzer();
    
    Vectorizer getOrCreateVectorizer();
}
