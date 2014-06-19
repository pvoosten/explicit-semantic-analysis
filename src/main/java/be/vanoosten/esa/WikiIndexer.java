/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package be.vanoosten.esa;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author user
 */
public class WikiIndexer extends DefaultHandler {
    
    private final MediaWikiParserFactory wikiFactory;
    private final MediaWikiParser wikiParser;
    private final SAXParserFactory saxFactory;
    private boolean inPage;
    private boolean inPageTitle;
    private boolean inPageText;
    private StringBuilder content = new StringBuilder();
    
    public WikiIndexer(Language language) {
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(true);
        saxFactory.setXIncludeAware(true);
        
        wikiFactory = new MediaWikiParserFactory(language);
        wikiParser = wikiFactory.createParser();
    }
    
    public void parseXmlDump(String path){
        parseXmlDump(new File(path));
    }
    
    public void parseXmlDump(File file){
        try {
            SAXParser saxParser = saxFactory.newSAXParser();
            InputStream wikiInputStream = new FileInputStream(file);
            wikiInputStream = new BufferedInputStream(wikiInputStream);
            wikiInputStream = new BZip2CompressorInputStream(wikiInputStream, true);
            saxParser.parse(wikiInputStream, this);
        } catch (ParserConfigurationException | SAXException | FileNotFoundException ex) {
            Logger.getLogger(WikiIndexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikiIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        WikiIndexer indexer = new WikiIndexer(Language.dutch);
        indexer.parseXmlDump(String.join(File.separator, "D:", "Downloads","nlwiki", "nlwiki-20140611-pages-articles-multistream.xml.bz2"));
    }
    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if("page".equals(localName)){
            inPage = true;
        }else if(inPage && "title".equals(localName)){
            inPageTitle = true;
            content = new StringBuilder();
        }else if(inPage && "text".equals(localName)){
            inPageText = true;
            content = new StringBuilder();
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(inPage && inPageTitle && "title".equals(localName)){
            inPageTitle = false;
            System.out.println(content.toString());
        }else if(inPage && inPageText && "text".equals(localName)){
            inPageText = false;
            ParsedPage page = wikiParser.parse(content.toString());
            if(!page.getCategories().isEmpty()){
                System.out.println("Categories:\n===========");
                page.getCategories().forEach(x -> System.out.println(x.getText()));
            }
            page.getTemplates().forEach(tpl-> System.out.println(tpl.getName()));
        }else if(inPage && "page".equals(localName)){
            inPage = false;
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }
}
