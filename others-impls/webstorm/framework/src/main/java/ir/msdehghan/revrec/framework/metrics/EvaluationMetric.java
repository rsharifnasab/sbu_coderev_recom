package ir.msdehghan.revrec.framework.metrics;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public interface EvaluationMetric<T> extends Consumer<T> {

    @Override
    void accept(@NotNull T result);

    Map<Integer, Double> getResult();
}
