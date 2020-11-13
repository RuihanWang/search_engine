package retrieval.tfidf;

import index.IndexTerm;
import index.Posting;
import retrieval.DocumentScore;
import retrieval.DocumentScoreComparator;
import retrieval.RetrievalModel;
import statistics.CorpusStatistics;
import transform.TokenTransformer;

import java.util.*;

/**
 * A class to compute the tf.idf scores of all documents for a query
 * and return the top documents sorted by score descending.
 * The tf component for a term in a document is the number of times
 * the term appears in the document divided by the length of the document.
 * The idf component for a term in a document is the log of the total number
 * of documents divided by the number of documents that the term appears in.
 * The tf.idf value is the product of the tf and idf components.
 *
 */
public class TfIdf implements RetrievalModel {

    /** A CorpusStatistics object containing statistics about documents in corpus */
    private CorpusStatistics stats;
    /** Mapping from String index terms to corresponding IndexTerm objects */
    private Map<String, IndexTerm> indexTermMap;
    /** Characters that determine initial token boundaries */
    final private static String DELIMITING_CHARACTERS = "[^a-zA-Z\\d]";

    /**
     * Public constructor for the TfIdf class
     * @param stats A CorpusStatistics object containing statistics about documents in corpus
     * @param indexTermMap Mapping from String index terms to corresponding IndexTerm objects
     */
    public TfIdf(CorpusStatistics stats, Map<String, IndexTerm> indexTermMap) {
        this.stats = stats;
        this.indexTermMap = indexTermMap;
    }

    /**
     * Computes the tf.idf score for all documents in the corpus for a query, then returns
     * a list of the top documents ranked by score descending. At most resultCount will be
     * returned since documents with score of 0 are not returned.
     * The tf component for a term in a document is the number of times the term occurs in
     * the document divided by the total number of term occurrences in the document.
     * The idf component for a term in a document is the log of the total number of documents
     * in the corpus divided by the number of documents the term appears in.
     * A tf.idf value is the product of the tf and idf component.
     * The score for a document is the sum of the tf.idf values for query terms that appear
     * in the document.
     * @param query The query containing terms to be searched for
     * @param resultCount The maximum number of documents to return in the list
     * @param tokenTransformers The list of TokenTransformer objects that will perform text
     *                          processing on the individual query terms. Ideally should be
     *                          the same used when processing index terms
     * @return A list of DocumentScore objects for the top ranked documents by tf.idf score.
     * The list will have at most resultCount documents since the documents with score 0 are
     * not added to the list.
     */
    public List<DocumentScore> rankDocuments(String query, int resultCount, List<TokenTransformer> tokenTransformers) {
        Set<Integer> docIds = this.stats.getDocLengthMap().keySet();
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
            IndexTerm queryIndexTerm = this.indexTermMap.get(queryTerm);
            Map<Integer, Posting> postingMap = queryIndexTerm.getPostingMap();
            // get idf by taking log of total number of documents divided by number of documents this term appears in
            double idf = Math.log((double) this.stats.getDocCount() / postingMap.size());
            for (int docId : postingMap.keySet()) {
                // get term frequency in current document normalized by document length
                double tf = (double) postingMap.get(docId).getTermFrequency() / this.stats.getDocLengthMap().get(docId);
                double tfidf = tf * idf;
                // add this tf.idf score to the current document's score
                if (docScores.containsKey(docId)) {
                    docScores.put(docId, docScores.get(docId) + tfidf);
                } else {
                    docScores.put(docId, tfidf);
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
