package ir.msdehghan.revrec.framework.strategy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporalMovingStrategyTest {
    @Test
    void test1() {
        var dataset = List.of(1, 2, 3, 4, 5);

        var strategy = TemporalMovingStrategy.create(dataset, Duration.ofMillis(3), value -> value);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(1, 2, 3), strategy.getTrainData());
        assertEquals(List.of(4), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(1), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(4), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(2, 3, 4), strategy.getTrainData());
        assertEquals(List.of(5), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(), strategy.getRowsToAddToTrain());

        assertFalse(strategy.hasTestData());
        assertThrows(IllegalStateException.class, strategy::getTestData);
    }

    @Test
    void test2() {
        var dataset = List.of(1, 2, 3, 6, 12);

        var strategy = TemporalMovingStrategy.create(dataset, Duration.ofMillis(3), value -> value);

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(1, 2), strategy.getTrainData());
        assertEquals(List.of(3), strategy.getTestData());
        strategy.testDataProcessed();
        assertEquals(List.of(1, 2), strategy.getRowsToRemoveFromTrain());
        assertEquals(List.of(3), strategy.getRowsToAddToTrain());

        assertTrue(strategy.hasTestData());
        assertEquals(List.of(3), strategy.getTrainData());
        assertEquals(List.of(6), strategy.getTestData());
        assertThrows(IllegalArgumentException.class, strategy::testDataProcessed);
    }

}