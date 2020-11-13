package query;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.*;

/**
 * A class to parse structured files containing search queries and return a
 * mapping from query id to query text.
 *
 */
public class StructuredQueryFileParser implements QueryFileParser {

    /**
     * Read the file with the given file name containing the queries and return
     * a mapping from query id to query text. The file should be structured as follows:
     * -Each document should be surrounded by <DOC></DOC> tags.
     * -Within each document, the query id should be surrounded by <DOCNO></DOCNO> tags.
     * -After the closing <DOCNO></DOCNO> tag, the query text should be present.
     * @param fileName The path to the file containing the search queries to be parsed
     * @return A mapping from query id to query text for queries in the structured file
     */
    public Map<Integer, String> parseQueryFile(String fileName) {
        Map<Integer, String> queries = new TreeMap<Integer, String>();
        try {
            // use the jsoup library to parse the query file based on the tags
            Document queryDoc = Jsoup.parse(new File(fileName), "UTF-8");
            // iterate through each query in the file located between <DOC> tags
            for (Element docElement : queryDoc.select("DOC")) {
                // get the query id nested within each document
                Element docNoElement = docElement.children().get(0);
                int queryId = Integer.parseInt(docNoElement.text().trim());
                // remove the query id tag and query id
                docNoElement.html("");
                // get the query text and add it to the mapping
                String queryString = docElement.text();
                queries.put(queryId, queryString);
            }
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error parsing query file: %s", fileName));
            e.printStackTrace();
            System.exit(-1);
        }
        return queries;
    }
}
