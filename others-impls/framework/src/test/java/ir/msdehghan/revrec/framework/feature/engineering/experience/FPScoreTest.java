package ir.msdehghan.revrec.framework.feature.engineering.experience;

import ir.msdehghan.revrec.framework.feature.engineering.time.LastReviewsState;
import ir.msdehghan.revrec.framework.model.Review;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FPScoreTest {

    @Test
    void addColumnValues() {
        var state = new LastReviewsState(Duration.ofDays(60));
        var fp = new FPScore(state);
        TestReview r1 = new TestReview(List.of("p1"), List.of("d1/f1"), 1);
        state.addToState(r1);
        fp.setTargetReview(r1);
        TestReview r2 = new TestReview(List.of("p2"), List.of("d1/f2"), 2);
        state.addToState(r2);
        fp.setTargetReview(r2);
        HashMap<String, Object> row = new HashMap<>();
        fp.addColumnValues(row, "p2");
        System.out.println(row);

        row = new HashMap<>();
        fp.addColumnValues(row, "p1");
        System.out.println(row);


        TestReview r3 = new TestReview(List.of("p1"), List.of("d1/f1"), Duration.ofDays(31).toMillis());
        state.addToState(r3);
        fp.setTargetReview(r3);

        row = new HashMap<>();
        fp.addColumnValues(row, "p2");
        System.out.println(row);

        row = new HashMap<>();
        fp.addColumnValues(row, "p1");
        System.out.println(row);
    }

    record TestReview(List<String> reviewers, List<String> filePaths, long time) implements Review {

        @Override
        public String getId() {
            return "1";
        }

        @Override
        public String getProject() {
            return "p";
        }

        @Override
        public long getSubmitTimestamp() {
            return time;
        }

        @Override
        public List<String> getReviewers() {
            return reviewers;
        }

        @Override
        public String getOwner() {
            return "1";
        }

        @Override
        public List<String> getFilePaths() {
            return filePaths;
        }
    }
}