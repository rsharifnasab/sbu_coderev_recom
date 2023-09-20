package ir.msdehghan.rm.metrics;

import java.util.Map;
import java.util.function.Consumer;

public interface EvaluationMetric<T> extends Consumer<T> {

    @Override
    void accept( T result);

    Map<Integer, Double> getResult();
}
