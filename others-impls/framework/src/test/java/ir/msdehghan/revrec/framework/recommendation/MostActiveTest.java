package ir.msdehghan.revrec.framework.recommendation;

import ir.msdehghan.revrec.framework.model.Review;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MostActiveTest {

    @Test
    void recommendReviewers() {
        MostActive mostActive = new MostActive(3);
        mostActive.addToState(List.of(
                new ReviewImpl(List.of("1", "2", "3")),
                new ReviewImpl(List.of("2", "3")),
                new ReviewImpl(List.of("3"))
        ));
        var recommendationResult = mostActive.recommendReviewers(new ReviewImpl(List.of()));
        assertEquals(List.of("3","2","1"), recommendationResult.getRecommended());

        mostActive.removeFromState(List.of(
                new ReviewImpl(List.of("1", "2", "3"))
        ));
        recommendationResult = mostActive.recommendReviewers(new ReviewImpl(List.of()));
        assertEquals(List.of("3","2"), recommendationResult.getRecommended());
    }

    static class ReviewImpl implements Review {
        private final List<String> reviewers;

        ReviewImpl(List<String> reviewers) {
            this.reviewers = reviewers;
        }

        @Override
        public String getId() {
            return "1";
        }

        @Override
        public String getProject() {
            return "project";
        }

        @Override
        public long getSubmitTimestamp() {
            return 0;
        }

        @Override
        public List<String> getReviewers() {
            return reviewers;
        }

        @Override
        public String getOwner() {
            return "test";
        }

        @Override
        public List<String> getFilePaths() {
            return List.of("testFile");
        }
    }
}