package retrieval.similarity;

import index.IndexTerm;
import retrieval.DocumentScore;
import retrieval.DocumentScoreComparator;
import retrieval.RetrievalModel;
import statistics.CorpusStatistics;
import transform.TokenTransformer;

import java.util.*;

/**
 * A class to compute the cosine similarity scores between queries and documents in the corpus,
 * then rank the documents based on those cosine similarity scores.
 *
 */
public class CosineSimilarity implements RetrievalModel {

    /** The VectorSpaceModel object containing normalize document vectors and index representation */
    private VectorSpaceModel vsm;
    /** An object containing statistics about the documents in the corpus */
    private CorpusStatistics stats;
    /** Characters that determine initial token boundaries */
    final private static String DELIMITING_CHARACTERS = "[^a-zA-Z\\d]";

    /**
     * Public constructor for the CosineSimilarity class
     * @param vsm A VectorSpaceModel object containing the normalized document weight vectors,
     *            index term mapping and index term list
     */
    public CosineSimilarity(VectorSpaceModel vsm, CorpusStatistics stats) {
        this.vsm = vsm;
        this.stats = stats;
    }

    /**
     * Calculates the cosine similarity score between the given query and all of the documents in the given
     * index, then returns the top documents sorted by cosine similarity.
     * Note, if a document has a cosine similarity score of zero, then it will not be returned since it does
     * not appear to be relevant to the search query. As a result, this method may return a list with a size
     * less than the given resultCount parameter.
     * @param query A string containing query terms to be searched for in the documents
     * @param resultCount The maximum number of documents to return
     * @param tokenTransformers A list of TokenTransformer objects that will perform text processing on individual
     *                          query terms. These should ideally be the same that were used for index terms
     * @return A list of DocumentScore objects for the most relevant documents based on the given query.
     * The resulting list will contain at most resultCount DocumentScore objects sorted by cosine similarity,
     * and documents with a score of 0 will not be returned. The DocumentScore object contains both the
     * document id and the cosine similarity score.
     */
    public List<DocumentScore> rankDocuments(String query, int resultCount, List<TokenTransformer> tokenTransformers) {
        // list of index terms in the same order as expected the term weights in the document vectors
        List<String> indexTermList = vsm.getIndexTermList();
        // the number of documents in the corpus
        int docCount = vsm.getDocCount();
        // mapping from String index terms to IndexTerm objects
        Map<String, IndexTerm> indexTermMap = vsm.getIndexTermMap();
        // maps document id to document vector. each document vector is a map from term number to term weight value
        Map<Integer, Map<Integer, Double>> docVectors = vsm.getDocVectors();

        // mapping from index term to position in the weight vectors
        Map<String, Integer> termNumberMapping = new HashMap<String, Integer>();
        for (int i = 0; i < indexTermList.size(); i++) {
            termNumberMapping.put(indexTermList.get(i), i);
        }

        // keep track of term frequency within query
        Map<String, Integer> queryTermFreqMap = new HashMap<String, Integer>();
        String[] queryTerms = query.split(DELIMITING_CHARACTERS);
        // process query terms just as document terms were processed
        for (int i = 0; i < queryTerms.length; i++) {
            String term = queryTerms[i];
            for (TokenTransformer transformer : tokenTransformers) {
                term = transformer.transform(term);
            }
            queryTerms[i] = term;
        }
        for (int i = 0; i < queryTerms.length; i++) {
            String term = queryTerms[i];
            if (queryTermFreqMap.containsKey(term)) {
                queryTermFreqMap.put(term, queryTermFreqMap.get(term) + 1);
            } else {
                queryTermFreqMap.put(term, 1);
            }
        }
        Map<Integer, Double> queryVector = new HashMap<Integer, Double>();
        double sum = 0;
        // set the tf.idf value for the query term
        for (String queryTerm : queryTermFreqMap.keySet()) {
            if (termNumberMapping.containsKey(queryTerm)) {
                int i = termNumberMapping.get(queryTerm);
                int docFrequency = indexTermMap.get(queryTerm).getPostingMap().size();
                double tf = Math.log(queryTermFreqMap.get(queryTerm)) + 1;
                double idf = Math.log((double) docCount / docFrequency);
                double tfidf = tf * idf;
                queryVector.put(i, tfidf);
                // accumulate the normalization constant
                sum += Math.pow(tfidf, 2);
            }
        }
        // normalize the query vector
        for (int i : queryVector.keySet()) {
            queryVector.put(i, queryVector.get(i) / Math.sqrt(sum));
        }

        // list of document ids and their cosine similarity values with the query
        List<DocumentScore> documentScoreList = new ArrayList<DocumentScore>();

        for (int docId : docVectors.keySet()) {
            Map<Integer, Double> docVector = docVectors.get(docId);
            double similarity = 0;
            // compute the dot product of the current document vector and query vector
            // to get the cosine similarity score
            for (int i : queryVector.keySet()) {
                if (docVector.containsKey(i)) {
                    similarity += queryVector.get(i) * docVector.get(i);
                }
            }
            // only record the document similarity in the
            if (similarity > 0) {
                documentScoreList.add(new DocumentScore(docId, similarity));
            }
        }

        // sort the documents by their cosine similarity values
        Collections.sort(documentScoreList, new DocumentScoreComparator());
        // return the top results based on resultCount
        return documentScoreList.subList(0, Math.min(resultCount, documentScoreList.size()));
    }

    /**
     * Get the vector space model object
     * @return The vector space model object
     */
    public VectorSpaceModel getVsm() {
        return vsm;
    }

    /**
     * Set the vector space model object
     * @param vsm The new vector space model object
     */
    public void setVsm(VectorSpaceModel vsm) {
        this.vsm = vsm;
    }
}
