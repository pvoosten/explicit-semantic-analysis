/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa.tools;

import be.vanoosten.esa.tools.Vectorizer;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author user
 */
public class SemanticSimilarityTool {

    Vectorizer vectorizer;
    
    public SemanticSimilarityTool(Vectorizer vectorizer) {
        this.vectorizer = vectorizer;
    }
    
    public float findSemanticSimilarity(String formerText, String latterText) throws ParseException, IOException{
        ConceptVector formerVector = vectorizer.vectorize(formerText);
        ConceptVector latterVector = vectorizer.vectorize(latterText);
        return formerVector.dotProduct(latterVector);
    }
    
}
