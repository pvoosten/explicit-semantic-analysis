/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizerFactory;
import org.apache.lucene.util.AttributeSource;

/**
 * Writes the term index of all wikipedia articles.
 * 
 * @author pvoosten
 */
public class TermIndexWriter {

    public TermIndexWriter(){
        
    }
    
    void index(String wikiText){
        WikipediaTokenizerFactory wtf = new WikipediaTokenizerFactory(new HashMap<>());
        Reader reader = new StringReader(wikiText);
        AttributeSource attSource = new AttributeSource();
        TokenStream ts = wtf.create(reader);
        try {
            CharTermAttribute ctatt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while(ts.incrementToken()){
                String term = ctatt.toString();
                System.out.println(term);
            }
            ts.end();
            ts.close();
        } catch (IOException ex) {
            Logger.getLogger(WikiIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
