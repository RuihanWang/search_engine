package retrieval;

import java.util.Comparator;

/**
 * A comparator for DocumentScore objects, ordering them
 * first by score value descending, then document
 * id value ascending.
 *
 */
public class DocumentScoreComparator implements Comparator<DocumentScore> {

    /**
     * Determine which DocumentScore objects should come before the other.
     * DocumentScore objects with higher scores should
     * come first. To break ties with scores, a lower document id
     * should come before a higher document id.
     * @param ds1 The first DocumentScore object being compared
     * @param ds2 The second DocumentScore object being compared
     * @return A negative value if the first object has a higher
     * score or if the scores are the same and the first object has a lower document id.
     * A postive value if the first object has a lower score or if the
     * scores are the same and the first object has a higher document id.
     * Zero if both scores and document ids are the same.
     */
    public int compare(DocumentScore ds1, DocumentScore ds2) {
        if (ds1.getScore() > ds2.getScore()) {
            return -1;
        } else if (ds1.getScore() < ds2.getScore()) {
            return 1;
        } else {
            return ds1.getDocId() - ds2.getDocId();
        }
    }
}
