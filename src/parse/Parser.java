package parse;

import document.Doc;

import java.util.List;

/**
 * A module that represents a document parser. Given the file name of
 * an HTML or text file containing documents for the collection,
 * this module returns a Doc object for each document, containing
 * the document text and the document id.
 *
 */
public interface Parser {
    /**
     * Parse the document text from the file with the given path. If the
     * given path is the name of a directory, then recursively call on
     * the files in the directory. Return a list of Docs containing the
     * document ids and document text.
     * @param path The path to the file or directory containing documents
     * @return A list of Doc objects containing document ids and document texts
     * for all the documents in the file/directory.
     */
    List<Doc> parseDocs(String path);
}
