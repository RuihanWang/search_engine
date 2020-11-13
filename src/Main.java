import index.IndexTerm;
import index.Indexer;
import lucene.Lucene;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import parse.CacmParser;
import query.StructuredQueryFileParser;
import retrieval.DocumentScore;
import retrieval.RetrievalModel;
import retrieval.bm25.BM25;
import retrieval.similarity.CosineSimilarity;
import retrieval.similarity.VectorSpaceModel;
import retrieval.tfidf.TfIdf;
import statistics.CorpusStatistics;
import tokenize.Tokenizer;
import transform.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The main class that should be run in order to perform all the tasks in the project.
 * See the documentation for the main method below for more details on what is being run.
 *
 */
public class Main {

    /** The number of total documents to return when retrieving documents for a query */
    final private static int RESULT_COUNT = 100;
    /** The String format for each line in the results table for a run */
    final private static String RESULTS_TABLE_FORMAT = "%d Q0 CACM-%04d %d %f %s\n";
    /** The name of the directory containing the raw documents */
    final private static String RAW_DOC_DIRECTORY_NAME = "cacm/rawDocs";
    /** The name of the directory containing the tokenized documents */
    final private static String TOKENIZED_DOC_DIRECTORY_NAME = "cacm/tokenizedDocs";
    /** The name of the file containing corpus statistics, such as document lengths */
    final private static String CORPUS_STATS_FILE_NAME = "Corpus-Stats.txt";
    /** The name of the file containing unstemmed queries */
    final private static String UNSTEMMED_QUERY_FILE_NAME = "cacm/cacm.query";
    /** The name of the file containing the inverted index for unstemmed documents */
    final private static String UNSTEMMED_INDEX_FILE_NAME = "Unstemmed-Index.txt";
    /** The name of the directory where Lucene index files are stored */
    final private static String LUCENE_INDEX_DIR_NAME = "luceneIndex";
    /** Simple analyzer for the Lucene indexer */
    final private static Analyzer SIMPLE_ANALYZER = new SimpleAnalyzer(Version.LUCENE_47);

    /**
     * Perform the runs for all the tasks in the project.
     * -Tokenize the documents
     * -Generate corpus statistics (document lengths, document count)
     * -Apply text transformation to the tokens
     * -Build the inverted index
     * -Parse the queries
     * -Perform Cosine Similarity run
     * -Perform tf.idf run
     * -Perform BM25
     * -Perform Lucene Run
     * @param args
     */
    public static void main(String[] args) {
        // Select text transformations for tokens
        List<TokenTransformer> tokenTransformers = new ArrayList<TokenTransformer>();
        tokenTransformers.add(new CaseFolder());

        System.out.println("[INFO] Tokenizing Documents");
        Tokenizer.tokenize(RAW_DOC_DIRECTORY_NAME, new CacmParser(), TOKENIZED_DOC_DIRECTORY_NAME, CORPUS_STATS_FILE_NAME, tokenTransformers);

        System.out.println("[INFO] Writing Indexing");
        Indexer.createIndex(1, TOKENIZED_DOC_DIRECTORY_NAME, UNSTEMMED_INDEX_FILE_NAME);

        System.out.println("[INFO] Loading Index Into Memory");
        Map<String, IndexTerm> index = IndexTerm.readIndexFromFile(UNSTEMMED_INDEX_FILE_NAME, "\t");

        System.out.println("[INFO] Parsing Query File");
        Map<Integer, String> queries = new StructuredQueryFileParser().parseQueryFile(UNSTEMMED_QUERY_FILE_NAME);

        // load corpus statistics into memory
        CorpusStatistics stats = CorpusStatistics.readStatsFromFile(CORPUS_STATS_FILE_NAME);

        // Perform the runs for Task 1

        // make sure runOutput directory exists
        new File("runOutput").mkdirs();

        System.out.println("[INFO] Performing Task 1 Cosine Similarity Run");
        VectorSpaceModel vsm = VectorSpaceModel.createFromIndex(index);
        RetrievalModel cosineSimilarity = new CosineSimilarity(vsm, stats);
        performRun(queries, tokenTransformers, cosineSimilarity, "runOutput/Cosine-Similarity-Run.txt", "CosineSimilarity");

        System.out.println("[INFO] Performing Task 1 TfIdf Run");
        RetrievalModel tfidf = new TfIdf(stats, index);
        performRun(queries, tokenTransformers, tfidf, "runOutput/TfIdf-Run.txt", "TfIdf");

        System.out.println("[INFO] Performing Task 1 BM25 Run");
        // TODO: Adjust BM25 parameters
        RetrievalModel bm25 = new BM25(1.2, 100.0, 0.75, stats, index);
        performRun(queries, tokenTransformers, bm25, "runOutput/BM25-Run.txt", "BM25");

        System.out.println("[INFO] Performing Task 1 Lucene Run");
        performLuceneRun(queries);
    }

    /**
     * For each query provided, perform a retrieval run with the given retrieval model
     * and write the ranked results list to a file with the given file name.
     * The query terms will be processed using the given TokenTransformer objects,
     * which should be the same as the ones used for processing the document terms.
     * @param queries A mapping from query id to query text for the queries that will
     *                be run for the retrieval system
     * @param tokenTransformers A list of TokenTransform objects that represent text
     *                          transformation processes that will be applied to
     *                          individual query terms. These should be the same that
     *                          were used for processing the documents
     * @param retrievalModel The retrieval model that will be used to rank and retrieve
     *                       the list of top documents for each query
     * @param outFileName The name of the file that will be written containing the lists
     *                    of top documents for each query. Each row in the file is in the
     *                    following format:
     *                    query_id Q0 doc_id rank score retrieval_model_name
     * @param modelName The String name of the retrieval model used for the results table
     */
    private static void performRun(Map<Integer, String> queries,
                                   List<TokenTransformer> tokenTransformers,
                                   RetrievalModel retrievalModel,
                                   String outFileName,
                                   String modelName) {
        try {
            PrintWriter writer = new PrintWriter(new File(outFileName));
            // perform a run for each query
            for (int queryId : queries.keySet()) {
                String queryString = queries.get(queryId);
                // get the ranked list of documents for the current query
                List<DocumentScore> results = retrievalModel.rankDocuments(queryString, RESULT_COUNT, tokenTransformers);
                for (int i = 0; i < results.size(); i++) {
                    DocumentScore ds = results.get(i);
                    writer.write(String.format(RESULTS_TABLE_FORMAT, queryId, ds.getDocId(), i + 1, ds.getScore(), modelName));
                }
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Error during %s run", modelName));
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Use Lucene to index the documents. Previous index files are first removed
     * to avoid duplicates and inconsistencies with the current set of documents.
     * For each query provided, perform a retrieval run with Lucene
     * and write the ranked results list to a file.
     * @param queries A mapping from query id to query text for the queries that will
     *                be run for the retrieval system
     */
    private static void performLuceneRun(Map<Integer, String> queries) {
        // make sure Lucene index directory exists
        new File(LUCENE_INDEX_DIR_NAME).mkdirs();
        // first clear Lucene index since we will recreate it to make sure it is correct
        File luceneIndexDir = new File(LUCENE_INDEX_DIR_NAME);
        String[] indexFileNames = luceneIndexDir.list();
        for (int i = 0; i < indexFileNames.length; i++) {
            new File(String.format("%s/%s", LUCENE_INDEX_DIR_NAME, indexFileNames[i])).delete();
        }
        // create the Lucene index from the raw documents
        try {
            Lucene luceneIndexer = new Lucene(LUCENE_INDEX_DIR_NAME);
            luceneIndexer.indexFileOrDirectory(RAW_DOC_DIRECTORY_NAME);
            luceneIndexer.closeIndex();
        } catch (Exception e) {
            System.out.println("[ERROR] Error indexing documents using Lucene");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(LUCENE_INDEX_DIR_NAME)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector;
            // write the top documents to a file in a table
            PrintWriter writer = new PrintWriter(new File("runOutput/Lucene-Run.txt"));
            for (int queryId : queries.keySet()) {
                // create the query
                Query q = new QueryParser(Version.LUCENE_47, "contents", SIMPLE_ANALYZER).parse(QueryParser.escape(queries.get(queryId)));
                // find top documents based on the query
                collector = TopScoreDocCollector.create(RESULT_COUNT, true);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    // write the line in the table for this document.
                    // note that Lucene creates doc ids starting from 0, actual doc ids start at 1,
                    // so docId + 1 keeps numbering consistent
                    writer.write(String.format(RESULTS_TABLE_FORMAT, queryId, docId + 1, i + 1, hits[i].score, "Lucene"));
                }
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("[ERROR] Error performing retrieval with Lucene");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
