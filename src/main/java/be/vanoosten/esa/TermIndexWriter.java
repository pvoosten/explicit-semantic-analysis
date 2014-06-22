/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

/**
 * Writes the term index of all wikipedia articles.
 * 
 * @author pvoosten
 */
public class TermIndexWriter {

    
    Directory directory;
    IndexWriter indexWriter;
    Analyzer analyzer;
    
    public TermIndexWriter(){
        String directoryPath = String.join(File.separator, "D:", "Development", "esa");
        File directoryFile = new File(directoryPath);
        try {
            directory = FSDirectory.open(directoryFile);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            indexWriter = new IndexWriter(directory, indexWriterConfig);
        } catch (IOException ex) {
            Logger.getLogger(TermIndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setAnalyzer(Analyzer analyzer){
        this.analyzer = analyzer;
    }
    
    public void close(){
        try {
            directory.close();
        } catch (IOException ex) {
            Logger.getLogger(TermIndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void index(String title, String wikiText){
        try {
            Document doc = new Document();
            doc.add(new StoredField("title", title));
            doc.add(new TextField("text", wikiText, Store.NO));
            indexWriter.addDocument(doc);
        } catch (IOException ex) {
            Logger.getLogger(TermIndexWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
