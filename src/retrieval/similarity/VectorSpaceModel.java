package retrieval.similarity;

import index.IndexTerm;
import index.Posting;

import java.util.*;

/**
 * A representation of a vector space model. Contains a normalized document
 * vector for each document in the corpus. The length of the document vector
 * is equal to the vocabulary size of the corpus, and each element in the vector
 * is the term weight for the corresponding term. The term weight is initially
 * the tf.idf value for the term in the document, then the document vector is
 * normalized such that it becomes a unit vector.
 *
 * The tf value is calculated as log(term frequency within the document) + 1
 * and the idf value is calculated as log(total document count / document frequency of term).
 *
 * This class also keeps the mapping from index term Strings to IndexTerm objects and a list
 * of index terms in the same order as expected in the document vector for convenience purposes.
 *
 */
public class VectorSpaceModel {
    private Map<Integer, Map<Integer, Double>> docVectors;
    private List<String> indexTermList;
    private Map<String, IndexTerm> indexTermMap;

    public VectorSpaceModel(Map<Integer, Map<Integer, Double>> docVectors, List<String> indexTermList, Map<String, IndexTerm> indexTermMap) {
        this.docVectors = docVectors;
        this.indexTermList = indexTermList;
        this.indexTermMap = indexTermMap;
    }

    public Map<Integer, Map<Integer, Double>> getDocVectors() {
        return docVectors;
    }

    public void setDocVectors(Map<Integer, Map<Integer, Double>> docVectors) {
        this.docVectors = docVectors;
    }

    public int getDocCount() {
        return this.docVectors.size();
    }

    public List<String> getIndexTermList() {
        return indexTermList;
    }

    public void setIndexTermList(List<String> indexTermList) {
        this.indexTermList = indexTermList;
    }

    public Map<String, IndexTerm> getIndexTermMap() {
        return indexTermMap;
    }

    public void setIndexTermMap(Map<String, IndexTerm> indexTermMap) {
        this.indexTermMap = indexTermMap;
    }

    /**
     * Get the Vector Space Model for an index. The vector space model consists of a vector of term weights for
     * each document in the index. Each element in the vector represents the term weight for a particular
     * term in the index. Since the vectors are generally sparse, each vector will be represented by a
     * Map from the vector position to the term weight for the term at that position.
     * The term weights are normalized tf.idf values for that term, such that each vector is a unit vector.
     * @param indexTermMap A mapping of String terms of the index to IndexTerm objects to represent the index itself.
     * @return The Vector Space Model as described above
     */
    public static VectorSpaceModel createFromIndex(Map<String, IndexTerm> indexTermMap) {
        // list of all index terms with the same order as the weight vectors
        List<String> indexTermList = new ArrayList<String>();
        indexTermList.addAll(indexTermMap.keySet());
        // keep track of how many documents are in the index
        int docCount = 0;
        // set of document ids
        Set<Integer> docIds = new HashSet<Integer>();

        // get the list of document ids
        for (String term : indexTermList) {
            IndexTerm indexTerm = indexTermMap.get(term);
            for (Integer docId : indexTerm.getPostingMap().keySet()) {
                if (!docIds.contains(docId)) {
                    docIds.add(docId);
                    docCount++;
                }
            }
        }

        // mapping from document id to document vectors where each
        Map<Integer, Map<Integer, Double>> docVectors = new HashMap<Integer, Map<Integer, Double>>();
        // add the tf.idf values to the document vectors
        for (int i = 0; i < indexTermList.size(); i++) {
            String term = indexTermList.get(i);
            IndexTerm indexTerm = indexTermMap.get(term);
            Map<Integer, Posting> postingMap = indexTerm.getPostingMap();
            int docFrequency = postingMap.size();
            for (int docId : postingMap.keySet()) {
                int rawTermFrequency = postingMap.get(docId).getTermFrequency();
                double tf = Math.log(rawTermFrequency) + 1;
                double idf = Math.log((double) docCount / docFrequency);
                double tfidf = tf * idf;
                if (docVectors.containsKey(docId)) {
                    Map<Integer, Double> docVector = docVectors.get(docId);
                    docVector.put(i, tfidf);
                } else {
                    Map<Integer, Double> docVector = new HashMap<Integer, Double>();
                    docVector.put(i, tfidf);
                    docVectors.put(docId, docVector);
                }
            }
        }
        // normalize the document vectors
        for (int docId : docIds) {
            Map<Integer, Double> docVector = docVectors.get(docId);
            double sum = 0;
            for (int i : docVector.keySet()) {
                sum += Math.pow(docVector.get(i), 2);
            }
            for (int i : docVector.keySet()) {
                docVector.put(i, docVector.get(i) / Math.sqrt(sum));
            }
        }
        return new VectorSpaceModel(docVectors, indexTermList, indexTermMap);
    }
}
