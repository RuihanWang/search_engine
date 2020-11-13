package retrieval;

import transform.TokenTransformer;

import java.util.List;

/**
 * A module for ranking and retrieving documents in the corpus for queries
 *
 */
public interface RetrievalModel {

    /**
     * Compute scores for all documents in the corpus based on the given query.
     * The query terms will be processed using the list of TokenTransformers.
     * The list of returned documents will have no more than resultCount documents,
     * and they will be sorted by score descending.
     * @param query A string containing query terms to be searched for in the documents
     * @param resultCount The maximum number of documents to return
     * @param tokenTransformers A list of TokenTransformer objects that will perform text processing on individual
     *                          query terms. These should ideally be the same that were used for index terms
     * @return A list of DocumentScore objects for the most relevant documents based on the given query.
     * The resulting list will contain at most resultCount DocumentScore objects sorted by score descending.
     * The DocumentScore object contains both the document id and the score.
     */
    List<DocumentScore> rankDocuments(String query, int resultCount, List<TokenTransformer> tokenTransformers);
}
