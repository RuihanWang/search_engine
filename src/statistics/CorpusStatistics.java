package statistics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A module to store statistics for the corpus
 *
 */
public class CorpusStatistics {

    /** Total number of documents in the corpus */
    private int docCount;
    /** Mapping from document id to number of index term occurrences for the document */
    private Map<Integer, Integer> docLengthMap;

    /**
     * A public constructor for the CorpusStatistics class
     * @param docLengthMap Mapping from document id to number of index term occurrences for the document
     */
    public CorpusStatistics(Map<Integer, Integer> docLengthMap) {
        if (docLengthMap == null) {
            this.docLengthMap = new HashMap<Integer, Integer>();
        } else {
            this.docLengthMap = docLengthMap;
        }
        this.docCount = this.docLengthMap.size();
    }

    /**
     * Read the mapping from document ids to document lengths from the file with
     * the given name. The file should have one document per line where each line
     * has the document id and document length separated by a tab character.
     * @param fileName The name of the mapping file to read from
     * @return A CorpusStatistics object containing the mapping from document id
     * to document length.
     */
    public static CorpusStatistics readStatsFromFile(String fileName) {
        try {
            Map<Integer, Integer> docLengthMap = new HashMap<Integer, Integer>();
            Scanner sc = new Scanner(new File(fileName));
            while (sc.hasNextInt()) {
                docLengthMap.put(sc.nextInt(), sc.nextInt());
            }
            return new CorpusStatistics(docLengthMap);
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error reading statistics file: %s", fileName));
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * Get the total number of documents in the corpus
     * @return The number of documents in the corpus
     */
    public int getDocCount() {
        return docCount;
    }

    /**
     * Get a mapping from document id to number of term occurrences in the document
     * @return The document id to document length mapping
     */
    public Map<Integer, Integer> getDocLengthMap() {
        return docLengthMap;
    }
}
