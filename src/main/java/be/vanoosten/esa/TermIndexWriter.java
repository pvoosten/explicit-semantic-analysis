/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * Writes the term index of all wikipedia articles.
 * 
 * @author pvoosten
 */
public class TermIndexWriter implements AutoCloseable{
        
    public static final String TEXT_FIELD = "text";
    public static final String TITLE_FIELD = "title";

    IndexWriter indexWriter;
    
    public TermIndexWriter(Analyzer analyzer, Directory directory) throws IOException{
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        indexWriter = new IndexWriter(directory, indexWriterConfig);
    }
    
    void index(String title, String wikiText) throws IOException{
        Document doc = new Document();
        doc.add(new StoredField(TITLE_FIELD, title));
        Analyzer analyzer = indexWriter.getAnalyzer();
        doc.add(new TextField(TEXT_FIELD, wikiText, Field.Store.NO));
        indexWriter.addDocument(doc);
    }

    @Override
    public void close() throws IOException {
        indexWriter.close();
    }
}
