import com.rapidminer.example.ExampleSet;
import ir.msdehghan.rm.RapidMinerCalculator;

public class Metrics {
    public static ExampleSet process(ExampleSet input) {
        return RapidMinerCalculator.processMetrics(input);
    }
}
