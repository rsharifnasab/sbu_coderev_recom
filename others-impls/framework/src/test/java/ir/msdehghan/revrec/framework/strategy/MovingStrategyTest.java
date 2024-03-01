package ir.msdehghan.revrec.framework.strategy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovingStrategyTest {

    @Test
    void testNonOverlappingSlidingValidationStrategy() {
        var dataset = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        var strategy = MovingStrategy.slidingBatchByBatch(dataset, 3, 2);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(1, 2, 3), strategy.getTrainData());
        assertEquals(List.of(4, 5), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(1, 2), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(4, 5), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(3, 4, 5), strategy.getTrainData());
        assertEquals(List.of(6, 7), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(3, 4), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(6, 7), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(5, 6, 7), strategy.getTrainData());
        assertEquals(List.of(8), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(), strategy.getRowsToAddToTrain());

        assertFalse(strategy.hasTestData());
        assertThrows(IllegalStateException.class, strategy::getTestData);
    }

    @Test
    void testOverlappingSlidingValidationStrategy() {
        var dataset = List.of(1, 2, 3, 4, 5);

        var strategy = MovingStrategy.slidingOneByOne(dataset, 3);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(4), strategy.getTestData());
        assertEquals(List.of(1, 2, 3), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(1), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(4), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(5), strategy.getTestData());
        assertEquals(List.of(2, 3, 4), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(), strategy.getRowsToAddToTrain());

        assertFalse(strategy.hasTestData());
        assertThrows(IllegalStateException.class, strategy::getTestData);
    }

    @Test
    void testBatchIncrementalValidationStrategy() {
        var dataset = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        var strategy = MovingStrategy.incrementalBatchByBatch(dataset, 3, 2);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(4, 5), strategy.getTestData());
        assertEquals(List.of(1, 2, 3), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(4, 5), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(6, 7), strategy.getTestData());
        assertEquals(List.of(1, 2, 3, 4, 5), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(6, 7), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(8), strategy.getTestData());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(), strategy.getRowsToAddToTrain());

        assertFalse(strategy.hasTestData());
        assertThrows(IllegalStateException.class, strategy::getTestData);
    }

    @Test
    void testOneByOneIncrementalValidationStrategy() {
        var dataset = List.of(1, 2, 3, 4, 5);

        var strategy = MovingStrategy.incrementalOneByOne(dataset, 3);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(4), strategy.getTestData());
        assertEquals(List.of(1, 2, 3), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(4), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(5), strategy.getTestData());
        assertEquals(List.of(1, 2, 3, 4), strategy.getTrainData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(), strategy.getRowsToAddToTrain());

        assertFalse(strategy.hasTestData());
        assertThrows(IllegalStateException.class, strategy::getTestData);
    }
}