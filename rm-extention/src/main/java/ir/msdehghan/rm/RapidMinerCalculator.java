package ir.msdehghan.rm;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.internal.ColumnarExampleTable;
import com.rapidminer.tools.Ontology;
import ir.msdehghan.rm.metrics.CompositeMetricRegistry;
import ir.msdehghan.rm.metrics.RecommendationResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("deprecation")
public class RapidMinerCalculator {

    public static SimpleExampleSet processMetrics(ExampleSet examples) {
        Attribute conf1Attr = examples.getAttributes().getConfidence("1");
        Attribute labelAttr = examples.getAttributes().getLabel();
        Attribute idAttr = examples.getAttributes().getId();

        List<Result> results = new ArrayList<>(examples.size());
        for (Example e : examples) {
            Result result = Result.create(e.getValueAsString(idAttr), e.getValue(conf1Attr),
                    e.getValueAsString(labelAttr));
            results.add(result);
        }
        results.sort(Comparator.comparingDouble(Result::getConfidence).reversed());
        Map<String, List<String>> recommended = results.stream()
                .collect(Collectors.groupingBy(Result::getChangeId, Collectors.mapping(Result::getUserId, toList())));
        Map<String, List<String>> actual = results.stream()
                .filter(Result::isReviewer)
                .collect(Collectors.groupingBy(Result::getChangeId, Collectors.mapping(Result::getUserId, toList())));

        CompositeMetricRegistry metricRegistry = CompositeMetricRegistry.full(10);
        recommended.keySet().stream()
                .map(change -> new RecommendationResult(change, actual.get(change), recommended.get(change)))
                .forEach(metricRegistry);

        ColumnarExampleTable table = new ColumnarExampleTable(createAttributes(metricRegistry));
        metricRegistry.getResultAsRows().forEach(table::addRow);
        return new SimpleExampleSet(table);
    }

    private static List<Attribute> createAttributes(CompositeMetricRegistry registry) {
        return registry.getHeaders().stream()
                .map(h -> AttributeFactory.createAttribute(h, Ontology.REAL)) // 4 is real-type(not nominal type)
                .collect(toList());
    }
}

