package tokenize;

import document.Doc;
import parse.Parser;
import transform.TokenListTransformer;
import transform.TokenTransformer;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * A module to parse raw HTML documents into tokens for indexing.
 *
 */
public class Tokenizer {

    /** Characters that determine initial token boundaries */
    final private static String DELIMITING_CHARACTERS = "[^a-zA-Z\\d]";

    /**
     * Parse the raw HTML document texts in the rawDocDicrectoryName directory into tokens,
     * then transform the tokens to be ready for indexing. Write the tokenized and transformed
     * text to files in the tokenizedDocDirectoryName directory.
     * This method will also write a file mapping document ids to the number of tokens in the document.
     * Each of the TokenTransformer objects in the given list will be applied to each of
     * the parsed tokens.
     * The provided ContentExtract will convert the raw HTML into tokenizable text by removing
     * HTML tags and noisy sections of the web page. If null is provided, then the default
     * content extractor will be used that only removes HTML tags.
     * @param rawDocDirectoryName The name of the directory containing raw HTML files
     * @param tokenizedDocDirectoryName The name of the directory that will contain tokenized document files
     * @param documentLengthFileName The name of the tab-separated file that will map document ids to number of
     *                               tokens in the document
     * @param tokenTransformers The list of TokenTransformers that will transform each token
     * @return A mapping from tokenized document file names to generated document ids
     */
    public static void tokenize(String rawDocDirectoryName,
                                Parser docParser,
                                String tokenizedDocDirectoryName,
                                String documentLengthFileName,
                                List<TokenTransformer> tokenTransformers) {
        // make sure tokenized document directory exists
        new File(tokenizedDocDirectoryName).mkdirs();
        // get the list of parsed Doc objects
        List<Doc> docs = docParser.parseDocs(rawDocDirectoryName);
        // store a mapping from document id to document length
        Map<Integer, Integer> docLengthMap = new HashMap<Integer, Integer>();
        for (Doc doc : docs) {
            String documentText = doc.getDocumentText();
            // parse the document text into tokens separated by slashes and white spaces
            List<String> tokens = new ArrayList<String>(Arrays.asList(documentText.trim().split(DELIMITING_CHARACTERS)));
            // transform the tokens to be ready for indexing
            tokens = TokenListTransformer.transformTokenList(tokens, tokenTransformers);
            // save the tokens to a file
            String tokenizedFileName = String.format("%s/file%04d.txt", tokenizedDocDirectoryName, doc.getId());
            writeTokenizedDocument(tokenizedFileName, tokens);
            // save the document length
            docLengthMap.put(doc.getId(), tokens.size());
        }
        // write tab-separated file mapping document ids to number of tokens in document
        writeDocumentLengths(documentLengthFileName, docLengthMap);

    }

    /**
     * Write the given tokens for a document to a file with the given name
     * @param outFileName The name of the file where the tokens will be written
     * @param tokens The list of tokens found in a document after text transformation
     */
    private static void writeTokenizedDocument(String outFileName, List<String> tokens) {
        try {
            PrintWriter writer = new PrintWriter(new File(outFileName));
            StringBuilder sb = new StringBuilder();
            for (String token : tokens) {
                sb.append(token + " ");
            }
            writer.write(sb.toString().trim());
            writer.close();
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error writing tokenized file %s", outFileName));
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Write the mapping from document id to number of tokens in the document, separated by tabs
     * @param outFileName The name of the mapping file that will be written
     * @param docLengthMap The mapping from document ids to document length values
     */
    private static void writeDocumentLengths(String outFileName, Map<Integer, Integer> docLengthMap) {
        try {
            PrintWriter writer = new PrintWriter(new File(outFileName));
            for (int docId : docLengthMap.keySet()) {
                writer.write(String.format("%d\t%d\n", docId, docLengthMap.get(docId)));
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error writing document length file %s", outFileName));
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
