package index;


import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * A module to create an index from tokenized documents
 *
 */
public class Indexer {

    /**
     * Read the tokenized documents in the tokenizedDocDirectoryName directory
     * and build an index for these documents using n-grams based on the given n.
     * Each entry in the index will include the index term and a posting list,
     * where each posting contains a document id where the index term is found,
     * as well as a term frequency count describing the number of times that term
     * is found in the document.
     * The index will be written to a file with the given outFileName.
     * @param n The value of n for the n-gram that will form each index term
     * @param tokenizedDocDirectoryName The directory name containing all the tokenized documents
     * @param outFileName The name of the file that the created index will be saved in
     */
    public static void createIndex(int n,
                            String tokenizedDocDirectoryName,
                            String outFileName) {
        System.out.println(String.format("[INFO] Creating index with %d-grams in file %s", n, outFileName));
        // mapping from String terms to IndexTerm objects
        Map<String, IndexTerm> indexTermMap = new TreeMap<String, IndexTerm>();
        // mapping from tokenized file names to document length
        Map<String, Integer> documentLengthMap = new HashMap<String, Integer>();
        // directory of the tokenized documents
        File tokenizedDocDirectory = new File(tokenizedDocDirectoryName);
        // file names of tokenized documents
        String[] tokenizedFileNames = tokenizedDocDirectory.list();
        // loop through all the tokenized files
        for (String tokenizedFileName : tokenizedFileNames) {
            // list to keep track of all the tokens in the current document (not necessarily unique)
            List<String> tokenList = new ArrayList<String>();
            // document id for the current document
            int docId = Integer.valueOf(tokenizedFileName.substring("file".length(), tokenizedFileName.length() - ".txt".length()));
            // mapping from String term to the term frequency within the current document
            Map<String, Integer> termFrequencyMap = new HashMap<String, Integer>();
            // keep track of number of total tokens in this document
            int tokenCount = 0;
            try {
                Scanner sc = new Scanner(new File(tokenizedDocDirectoryName + "/" + tokenizedFileName));
                // add all the tokens to the term frequency map for this document
                while (sc.hasNext()) {
                    tokenList.add(sc.next());
                    tokenCount++;
                }
                // record the total number of tokens found in this document
                documentLengthMap.put(tokenizedFileName, tokenCount);
                // create the n-grams
                for (int i = 0; i <= tokenList.size() - n; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = i; j < i + n; j++) {
                        sb.append(tokenList.get(j) + " ");
                    }
                    String term = sb.toString().trim();
                    // update the term frequency count for the current n-gram
                    if (termFrequencyMap.containsKey(term)) {
                        termFrequencyMap.put(term, termFrequencyMap.get(term) + 1);
                    } else {
                        termFrequencyMap.put(term, 1);
                    }
                }
                // add all the terms and frequencies to the index
                for (String term : termFrequencyMap.keySet()) {
                    if (indexTermMap.containsKey(term)) {
                        IndexTerm indexTerm = indexTermMap.get(term);
                        Posting posting = new Posting(docId, termFrequencyMap.get(term));
                        indexTerm.getPostingMap().put(docId, posting);
                    } else {
                        IndexTerm indexTerm = new IndexTerm(term);
                        Posting posting = new Posting(docId, termFrequencyMap.get(term));
                        indexTerm.getPostingMap().put(docId, posting);
                        indexTermMap.put(term, indexTerm);
                    }
                }
            } catch (Exception e) {
                System.out.println("[ERROR] Error reading tokenized file " + tokenizedFileName);
                e.printStackTrace();
            }
        }
        // write the index to a file
        writeIndex(indexTermMap, outFileName);
    }

    /**
     * Write the given index to a file with the given name
     * @param indexTermMap The mapping from index term Strings to the IndexTerm objects
     * @param outFileName The name of the file where the index will be written
     */
    private static void writeIndex(Map<String, IndexTerm> indexTermMap, String outFileName) {
        try {
            PrintWriter writer = new PrintWriter(new File(outFileName));
            for (String term : indexTermMap.keySet()) {
                // create the entry for the current term consisting of the
                // index term followed by docId,termFrequency pairs separated
                // by tab characters
                StringBuilder sb = new StringBuilder();
                IndexTerm indexTerm = indexTermMap.get(term);
                sb.append(term);
                for (int docId : indexTerm.getPostingMap().keySet()) {
                    Posting posting = indexTerm.getPostingMap().get(docId);
                    sb.append("\t" + docId + "," + posting.getTermFrequency());
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("[ERROR] Error writing index");
            e.printStackTrace();
        }
    }
}
