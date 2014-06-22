/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import java.io.File;

/**
 *
 * @author user
 */
public class Main {
    public static void main(String[] args) {
        WikiIndexer indexer = new WikiIndexer(WikiConstants.Language.dutch);
        TermIndexWriter termIndexWriter = new TermIndexWriter();
        indexer.setTermIndexWriter(termIndexWriter);
        indexer.parseXmlDump(String.join(File.separator, "D:", "Downloads","nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
        

    }
    
}
