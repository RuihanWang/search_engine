package retrieval;

/**
 * A helper class to keep track of a document id and its score
 * score with a query
 *
 */
public class DocumentScore {
    /** The id of the document */
    int docId;
    /** The score for this document for a query */
    double score;

    /**
     * A public constructor for the DocumentScore class
     * @param docId the id of the document
     * @param score the score for this document for a query
     */
    public DocumentScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    /**
     * Get the id for this document
     * @return The document id value
     */
    public int getDocId() {
        return docId;
    }

    /**
     * Set the id for this document
     * @param docId The new document id value
     */
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * Get the score value for this document
     * @return The score value for this document
     */
    public double getScore() {
        return score;
    }

    /**
     * Set the score for this document
     * @param score The new score for this document
     */
    public void setScore(double score) {
        this.score = score;
    }
}
