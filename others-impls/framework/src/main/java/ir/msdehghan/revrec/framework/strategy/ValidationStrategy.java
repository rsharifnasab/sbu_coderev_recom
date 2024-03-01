package ir.msdehghan.revrec.framework.strategy;
import java.util.List;

public interface ValidationStrategy<T> {
    List<T> getTrainData();

    List<T> getTestData();

    boolean hasTestData();

    List<T> getRowsToRemoveFromTrain();

    List<T> getRowsToAddToTrain();

    void testDataProcessed();
}
