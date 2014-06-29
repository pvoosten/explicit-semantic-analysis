/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author user
 */
class ProducerConsumerTokenStream extends TokenStream{
    
    private volatile boolean productionInProgress;
    private final BlockingQueue<Token> queue;
    
    private final CharTermAttribute  termAtt = addAttribute(CharTermAttribute.class);

    ProducerConsumerTokenStream() {
        this.queue = new LinkedBlockingQueue<>();
        productionInProgress = true;
    }
    
    void produceToken(Token token){
        queue.add(token);
    }
    
    void finishProducingTokens() {
        productionInProgress = false;
    }

    @Override
    public boolean incrementToken() throws IOException {
        try {
            Token token = queue.poll(10, TimeUnit.MILLISECONDS);
            while (token == null && productionInProgress) {
                token = queue.poll(10, TimeUnit.MILLISECONDS);
            }
            if(token != null){
                char[] buffer = termAtt.buffer();
                System.arraycopy(token.buffer(), 0, buffer, 0, token.length());
                termAtt.setLength(token.length());
                #error verwerk het token volledig, zoals nodig.
            }
            return token != null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
