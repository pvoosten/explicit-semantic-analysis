/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;

/**
 *
 * @author user
 */
class CachingTokenStream extends TokenStream {

    private int i = -1;
    private final ArrayList<Token> queue;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);

    CachingTokenStream() {
        this.queue = new ArrayList<>();
    }

    void produceToken(Token token) {
        queue.add(token);
    }

    @Override
    public boolean incrementToken() throws IOException {
        i++;
        if (queue.size() <= i) {
            return false;
        }
        final Token token = queue.get(i);
        int tokenLength = token.length();
        termAtt.resizeBuffer(Math.max(termAtt.buffer().length, tokenLength));
        termAtt.setLength(tokenLength);
        final char[] buffer = termAtt.buffer();
        System.arraycopy(token.buffer(), 0, buffer, 0, token.length());
        payloadAtt.setPayload(token.getPayload());
        return true;
    }
}
