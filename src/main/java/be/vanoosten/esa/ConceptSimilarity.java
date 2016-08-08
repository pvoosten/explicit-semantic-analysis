package be.vanoosten.esa;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.ByteArrayDataInput;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Philip van Oosten
 */
public class ConceptSimilarity extends TFIDFSimilarity{
    
    public static final float SIMILARITY_FACTOR = 0.00001f;

    private final ByteArrayDataInput dataInput = new ByteArrayDataInput();

    @Override
    public float coord(int overlap, int maxOverlap) {
        return 1f/maxOverlap;
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        return 1f;
    }

    @Override
    public float tf(float freq) {
        return 1f;
    }

    @Override
    public float idf(long docFreq, long numDocs) {
        return (float) Math.log(1.0*numDocs/docFreq);
    }

    @Override
    public float lengthNorm(FieldInvertState state) {
        return 1f;
    }

    @Override
    public float decodeNormValue(long norm) {
        return 1f;
    }

    @Override
    public long encodeNormValue(float f) {
        return 1L;
    }

    @Override
    public float sloppyFreq(int distance) {
        return 0f;
    }
    
    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        synchronized(dataInput){
            dataInput.reset(payload.bytes);
            return SIMILARITY_FACTOR* dataInput.readVInt();
        }
    }
}
