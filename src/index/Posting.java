package index;

/**
 * A class to represent an inverted index posting containing the document
 * id where an index is found and the term frequency for that index term
 * in this document.
 */
public class Posting {

    /** The document id where an index term is found */
    int docId;
    /** The number of times the term is found in a document */
    int termFrequency;

    /**
     * Public constructor for the Posting class
     * @param docId The document id where an index term is found
     * @param termFrequency The number of times the index term occurs in this document
     */
    public Posting(int docId, int termFrequency) {
        this.docId = docId;
        this.termFrequency = termFrequency;
    }

    /**
     * Get the document id
     * @return The document id
     */
    public int getDocId() {
        return docId;
    }

    /**
     * Set the document id
     * @param docId The new document id
     */
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * Get the term frequency count for the index term in this document
     * @return The term frequency count
     */
    public int getTermFrequency() {
        return termFrequency;
    }

    /**
     * Set the term frequency count for the index term in this document
     * @param termFrequency The new term frequency count
     */
    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }
}
