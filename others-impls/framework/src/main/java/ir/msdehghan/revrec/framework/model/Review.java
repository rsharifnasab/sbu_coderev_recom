package ir.msdehghan.revrec.framework.model;

import java.util.List;

public interface Review {
    String getId();
    String getProject();
    long getSubmitTimestamp();
    default long getClosedTimestamp() {
        throw new UnsupportedOperationException("This dataset does not provide closed timestamp");
    }
    List<String> getReviewers();
    String getOwner();
    List<String> getFilePaths();
}
