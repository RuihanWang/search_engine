package query;

import java.util.Map;

/**
 * A module to parse search queries from files, providing a mapping
 * from query id to query text.
 *
 */
public interface QueryFileParser {
    
    /**
     * Parse the file with the given name.
     * @param fileName The path to the file containing the search queries to be parsed
     * @return A mapping from query id to query text.
     */
    Map<Integer, String> parseQueryFile(String fileName);
}
