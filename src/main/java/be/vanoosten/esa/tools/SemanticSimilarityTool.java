package be.vanoosten.esa.tools;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Philip van Oosten
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
