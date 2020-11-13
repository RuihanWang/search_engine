package retrieval.bm25;

import index.IndexTerm;
import index.Posting;
import retrieval.DocumentScore;
import retrieval.DocumentScoreComparator;
import retrieval.RetrievalModel;
import statistics.CorpusStatistics;
import transform.TokenTransformer;

import java.util.*;

/**
 * A class to compute the BM25 scores of all documents for a query
 * and return the top documents sorted by score descending.
 * The BM25 scoring function is based on the function mentioned in the
 * Croft textbook and assumes that no relevance information is present since
 * outside of this project, it is unlikely that relevance information would be
 * present, so the results of evaluation should more closely match the performance
 * outside of this project.
 *
 */
public class BM25 implements RetrievalModel {

    /** Constants required for the BM25 scoring function */
    private double k1, k2, b, avdl;
    /** A CorpusStatistics object containing statistics about documents in corpus */
    CorpusStatistics stats;
    /** Mapping from String index terms to corresponding IndexTerm objects */
    Map<String, IndexTerm> indexTermMap;
    /** Characters that determine initial token boundaries */
    final private static String DELIMITING_CHARACTERS = "[^a-zA-Z\\d]";

    /**
     * Public constructor for the BM25 class. Sets the given parameters and computes the
     * average document length (avdl) value from the document lengths in the corpus stats.
     * @param k1 The k1 parameter value in the BM25 scoring function. Controls impact of
     *           the term frequency values in the documents
     * @param k2 The k2 parameter value in the BM25 scoring function. Controls impact of
     *           the term frequency values in the query
     * @param b The b parameter value in the BM25 scoring function. Controls impact of
     *          document length in the score calculation
     * @param stats A CorpusStatistics object containing statistics about documents in corpus
     * @param indexTermMap Mapping from String index terms to corresponding IndexTerm objects
     */
    public BM25(double k1, double k2, double b, CorpusStatistics stats, Map<String, IndexTerm> indexTermMap) {
        this.k1 = k1;
        this.k2 = k2;
        this.b = b;
        this.stats = stats;
        this.indexTermMap = indexTermMap;
        double avdl = 0;
        Map<Integer, Integer> docLengthMap = stats.getDocLengthMap();
        for (int docId : docLengthMap.keySet()) {
            avdl += docLengthMap.get(docId);
        }
        this.avdl = avdl / docLengthMap.size();
    }

    /**
     * Computes the BM25 score for all documents in the corpus for a query, then returns
     * a list of the top documents ranked by score descending. At most resultCount will be
     * returned since documents with a score of 0 are not returned.
     * The BM25 scoring function is based on the function mentioned in the
     * Croft textbook and assumes that no relevance information is present since
     * outside of this project, it is unlikely that relevance information would be
     * present, so the results of evaluation should more closely match the performance
     * outside of this project.
     * @param query A string containing query terms to be searched for in the documents
     * @param resultCount The maximum number of documents to return
     * @param tokenTransformers A list of TokenTransformer objects that will perform text processing on individual
     *                          query terms. These should ideally be the same that were used for index terms
     * @return A list of DocumentScore objects for the top ranked documents by BM25 score.
     * The list will have at most resultCount documents since the documents with score 0 are
     * not added to the list.
     */
    public List<DocumentScore> rankDocuments(String query, int resultCount, List<TokenTransformer> tokenTransformers) {
        Map<Integer, Integer> docLengthMap = this.stats.getDocLengthMap();
        Set<Integer> docIds = docLengthMap.keySet();
        // mapping from document ids to partial tf.idf scores
        Map<Integer, Double> docScores = new HashMap<Integer, Double>();
        // keep track of term frequency within query
        Map<String, Integer> queryTermFreqMap = new HashMap<String, Integer>();
        String[] queryTerms = query.split(DELIMITING_CHARACTERS);
        // process query terms just as document terms were processed
        for (int i = 0; i < queryTerms.length; i++) {
            String term = queryTerms[i];
            for (TokenTransformer transformer : tokenTransformers) {
                term = transformer.transform(term);
            }
            // ignore query term if it is not an index term
            if (!this.indexTermMap.containsKey(term)) {
                continue;
            }
            if (queryTermFreqMap.containsKey(term)) {
                queryTermFreqMap.put(term, queryTermFreqMap.get(term) + 1);
            } else {
                queryTermFreqMap.put(term, 1);
            }
        }

        for (String queryTerm : queryTermFreqMap.keySet()) {
            int queryTermFreq = queryTermFreqMap.get(queryTerm);
            IndexTerm queryIndexTerm = this.indexTermMap.get(queryTerm);
            Map<Integer, Posting> postingMap = queryIndexTerm.getPostingMap();
            for (int docId : postingMap.keySet()) {
                int docTermFreq = postingMap.get(docId).getTermFrequency();
                int docLength = docLengthMap.get(docId);
                int docFreq = postingMap.size();
                double K = k1 * ((1 - b) + b * ((double) docLength / avdl));
                // calculate the idf-like component of the BM25 scoring function
                // NOTE: Add 1 before taking the log to avoid negative BM25 values. When the term appears
                // in more than half the documents, the original value inside the log is less than 1, so taking
                // the log would normally result in a negative number. The added 1 should avoid this issue.
                double idfComponent = Math.log(1 + 1.0/((double) docFreq / (this.stats.getDocCount() - docFreq + 0.5)));
                // calculate the tf-like component of the BM25 scoring function related to the document
                double tfDocComponent = ((k1 + 1) * docTermFreq) / (K + docTermFreq);
                // calculate the tf-like component of the BM25 scoring function related to the query
                double tfQueryComponent = ((k2 + 1) * queryTermFreq) / (k2 + queryTermFreq);
                double partialScore = idfComponent * tfDocComponent * tfQueryComponent;
                if (docScores.containsKey(docId)) {
                    docScores.put(docId, docScores.get(docId) + partialScore);
                } else {
                    docScores.put(docId, partialScore);
                }
            }
        }
        // sort and return the results list
        List<DocumentScore> documentScoreList = new ArrayList<DocumentScore>();
        for (int docId : docIds) {
            if (docScores.containsKey(docId) && docScores.get(docId) > 0) {
                documentScoreList.add(new DocumentScore(docId, docScores.get(docId)));
            }
        }
        documentScoreList.sort(new DocumentScoreComparator());
        return documentScoreList.subList(0, Math.min(resultCount, documentScoreList.size()));
    }
}
