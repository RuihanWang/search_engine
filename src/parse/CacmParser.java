package parse;

import document.Doc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A document parser for the CACM collection.
 * Parses document text and document ids from CACM HTML files or
 * text files containing tag-less and stemmed document text.
 *
 */
public class CacmParser implements Parser {

    /**
     * Parse the document text from the file specified by the given path.
     * If the path is a directory, then recursively call on files in the directory.
     * The HTML files must be named CACM-####.html where the #### is the document id.
     * The document text must also be located within a single <pre> tag within the file.
     * If the file is just a text file, it can contain one or more documents. Each document
     * should have a header in the form `# docId` where `docId` is replaced with the actual
     * integer document id. Returns a list of Doc objects representing the parsed documents.
     * @param path The path to the file or directory containing documents
     * @return A list of Doc objects containing doc ids and document texts
     */
    public List<Doc> parseDocs(String path) {
        List<Doc> parsedDocs = new ArrayList<Doc>();
        File filePath = new File(path);
        if (filePath.isDirectory()) {
            // path is to a directory so add documents from files in directory
            String[] fileNames = filePath.list();
            for (String fileName : fileNames) {
                List<Doc> innerList = parseDocs(String.format("%s/%s", path, fileName));
                parsedDocs.addAll(innerList);
            }
        } else {
            // path is to a file so parse HTML file and add doc to result list
            if (path.toLowerCase().endsWith(".html")) {
                parsedDocs.add(parseDocFromHtmlFile(filePath, path));
            } else {
                parsedDocs.addAll(parseDocsFromTextFile(filePath, path));
            }
        }
        return parsedDocs;
    }

    /**
     * Parse the document from the given file containing HTML.
     * The document text should be located within the single set of <pre> tags in
     * the HTML. A Doc object representing the document is returned.
     * The file should be named using the format `CACM-####.html` where #### is the doc id.
     * @param docFile The File object representing the file containing the HTML to be parsed
     * @param path The file path to the docFile object
     * @return A Doc object containing the doc id and parsed document text for the given file
     */
    private Doc parseDocFromHtmlFile(File docFile, String path) {
        Doc returnDoc = null;
        try {
            // parse HTML using jsoup library
            Document jsoupDoc = Jsoup.parse(docFile, "UTF-8");
            // get document text from CACM HTML
            String documentText = jsoupDoc.select("pre").text();
            // get the document id from the file name
            int docId = getDocIdFromPath(path);
            if (docId < 0) {
                throw new RuntimeException(String.format("Could not get document id from file name: %s", path));
            }
            // set the document to be returned
            returnDoc = new Doc(docId, documentText);
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error parsing document: %s", path));
            e.printStackTrace();
            System.exit(-1);
        }
        return returnDoc;
    }

    /**
     * Parse the documents from the given file containing documents.
     * The documents in the file must have a header in the format `# docId`
     * where `docId` is replaced with the actual integer representing the doc id.
     * The documents in the file should also not include any HTML tags.
     * @param docFile The File object representing the file containing documents to be parsed
     * @param path The file path to the docFile object
     * @return A List of Doc objects containing each of the doc ids and parsed document texts
     */
    private List<Doc> parseDocsFromTextFile(File docFile, String path) {
        List<Doc> returnDocs = new ArrayList<Doc>();
        try {
            Scanner sc = new Scanner(docFile);
            // to hold the document text for each document
            StringBuilder sb = new StringBuilder();
            // to match against document headers `# docId`
            Pattern docHeader = Pattern.compile("^# \\d+$");
            // to keep track of the current document id
            int docId = -1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (docHeader.matcher(line).matches()) {
                    // this line is a header
                    if (docId > -1) {
                        // the string builder has accumulated a document, so add to return list
                        returnDocs.add(new Doc(docId, sb.toString()));
                    }
                    // this line is the header for a new document
                    sb = new StringBuilder();
                    // set new document id
                    docId = Integer.valueOf(line.substring("# ".length()));
                } else {
                    // this line is part of the document text for the current doc
                    sb.append(line).append(" ");
                }
            }
            // add final document in the text file
            if (docId > -1) {
                returnDocs.add(new Doc(docId, sb.toString()));
            }

        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error parsing document: %s", path));
            e.printStackTrace();
            System.exit(-1);
        }
        return returnDocs;
    }

    /**
     * Parse the document id of the CACM file from the given path.
     * The CACM file must be named as CACM-####.html where #### is the doc id.
     * If no file name of this format is found in the text, then -1 is returned.
     * @param path The path to the CACM file whose doc id is to be parsed
     * @return The integer document id for the file if the file name follows CACM-####.html
     * where #### is the document id. Return -1 if the document id cannot be parsed.
     */
    private Integer getDocIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        String[] pathParts = path.split("/");

        // get the file name, which is the last part of the given path
        String fileName = pathParts[pathParts.length - 1];

        if (!fileName.contains("CACM-") || !fileName.contains(".html")) {
            return -1;
        }

        // get only the document id portion of the file name
        int docIdStartIndex = "CACM-".length();
        int docIdEndIndex = fileName.length() - ".html".length();
        return Integer.valueOf(fileName.substring(docIdStartIndex, docIdEndIndex));
    }
}
