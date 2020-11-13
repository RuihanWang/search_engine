package query;

import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * A class to parse unstructured files containing search queries and return a
 * mapping from query id to query text.
 *
 */
public class UnstructuredQueryFileParser implements QueryFileParser {

    /**
     * Read the file with the given file name containing the queries and return
     * a mapping from query id to query text. Each line should contain the query text,
     * and the query ids will be the line number in the file.
     * @param fileName The path to the file containing the search queries to be parsed
     * @return A mapping from query id to query text for queries in the unstructured file
     */
    public Map<Integer, String> parseQueryFile(String fileName) {
        Map<Integer, String> queries = new TreeMap<Integer, String>();
        try {
            Scanner sc = new Scanner(new File(fileName));
            int queryId = 1;
            // add each line in the file as a query string, using the line number as the query id
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                queries.put(queryId, line);
                queryId++;
            }
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error parsing query file: %s", fileName));
            e.printStackTrace();
            System.exit(-1);
        }
        return queries;
    }
}
