package index;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * A class to represent an index term.
 *
 */
public class IndexTerm {

    /** The term String */
    String term;
    /** A mapping from document ids to Posting objects where this term is found  */
    Map<Integer, Posting> postingMap;

    /**
     * A public constructor for the IndexTerm class. The IndexTerm initially starts
     * with 0 postings.
     * @param term The index term String
     */
    public IndexTerm(String term) {
        this.term = term;
        this.postingMap = new TreeMap<Integer, Posting>();
    }

    /**
     * Get the index term String
     * @return The index term String
     */
    public String getTerm() {
        return term;
    }

    /**
     * Set the index term String
     * @param term The new index term String
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Get the postings mapping
     * @return The mapping from document ids to Posting objects where this index term is found
     */
    public Map<Integer, Posting> getPostingMap() {
        return postingMap;
    }

    /**
     * Set the postings mapping
     * @param postingMap The new mapping from document ids to Posting objects where this index term is found
     */
    public void setPostingMap(Map<Integer, Posting> postingMap) {
        this.postingMap = postingMap;
    }

    /**
     * Read from the index file with the given name to recreate the index in memory.
     * The returned index should be a mapping from index term Strings to IndexTerm objects,
     * where each IndexTerm object will have a mapping from document ids where the term is
     * found to Posting objects that contain the term frequencies within those documents.
     * @param indexFileName The name of the file containing the inverted index.
     * @param delimiter The String that delimits the index terms and document ids in the index file.
     * @return
     */
    public static Map<String, IndexTerm> readIndexFromFile(String indexFileName, String delimiter) {
        Map<String, IndexTerm> indexTermMap = new HashMap<String, IndexTerm>();
        try {
            Scanner sc = new Scanner(new File(indexFileName));
            // create an index entry for each line in the file
            while (sc.hasNextLine()) {
                // split the line by the delimiter
                String[] lineItems = sc.nextLine().trim().split(delimiter);
                if (lineItems.length == 0) {
                    continue;
                }
                // the first value in the row should be the index term
                String term = lineItems[0];
                IndexTerm indexTerm = new IndexTerm(term);
                // make sure we haven't already seen the index term
                if (indexTermMap.containsKey(term)) {
                    System.out.println(String.format("[ERROR] Duplicate term: %s", term));
                    continue;
                }
                indexTermMap.put(term, indexTerm);
                Map<Integer, Posting> postingMap = indexTerm.getPostingMap();
                // create posting objects by reading document ids and term frequencies
                for (int i = 1; i < lineItems.length; i++) {
                    String[] postingValues = lineItems[i].split(",");
                    int docId = Integer.parseInt(postingValues[0]);
                    int termFreq = Integer.parseInt(postingValues[1]);
                    postingMap.put(docId, new Posting(docId, termFreq));
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error reading from file %s", indexFileName));
            e.printStackTrace();
        }
        return indexTermMap;
    }
}
