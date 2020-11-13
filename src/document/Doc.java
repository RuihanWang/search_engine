package document;

/**
 * A class to represent a document in the collection.
 *
 */
public class Doc {

    /** The id for this document */
    int id;
    /** A String representing the text of this document */
    String documentText;

    /**
     * An empty constructor for the WebPage class
     */
    public Doc() {}

    /**
     * A public constructor for the WebPage class
     * @param id The document id to be assigned to this WebPage
     * @param documentText The text of this document
     */
    public Doc(int id, String documentText) {
        this.id = id;
        this.documentText = documentText;
    }

    /**
     * Get the document id for this WebPage
     * @return The document id for this WebPage
     */
    public int getId() {
        return id;
    }

    /**
     * Set the document id for this WebPage
     * @param id The new document id for this WebPage
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get a String representing the raw HTML of this WebPage
     * @return A String representing the raw HTML of this WebPage
     */
    public String getDocumentText() {
        return documentText;
    }

    /**
     * Set the String representing the raw HTML of this WebPage
     * @param documentText The new String representing the raw HTML of this WebPage
     */
    public void setDocumentText(String documentText) {
        this.documentText = documentText;
    }
}
