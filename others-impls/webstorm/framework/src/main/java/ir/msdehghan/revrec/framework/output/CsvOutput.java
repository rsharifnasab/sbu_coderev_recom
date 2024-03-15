package ir.msdehghan.revrec.framework.output;

import ir.msdehghan.revrec.framework.metrics.CompositeMetricRegistry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.Validate;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class CsvOutput implements Closeable {
    private final String[] headers;
    private final CSVPrinter printer;

    public CsvOutput(List<String> headers, Path filePath) throws IOException {
        this.headers = headers.toArray(String[]::new);
        boolean writeHeaders = false;
        if (!Files.exists(filePath)) {
            writeHeaders = true;
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
        }
        var writer = Files.newBufferedWriter(filePath, APPEND, CREATE);
        printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
        if (writeHeaders) {
            printer.printRecord(headers);
        }
    }

    public void write(List<Map<String, String>> result, Map<String,String> additionalFields)
            throws IOException {
        for (Map<String, String> metricColumns : result) {
            final var columns = new HashMap<>(metricColumns);
            columns.putAll(additionalFields);
            write(columns);
        }
    }

    public void write(Map<String, String> rowMap) throws IOException {
        List<String> row = new ArrayList<>(headers.length);
        for (String header : headers) {
            if (!rowMap.containsKey(header)) {
                throw new IllegalStateException("Row does not contain '" + header + "'");
            }
            row.add(rowMap.get(header));
        }
        printer.printRecord(row);
    }

    public void write(List<String> columns) throws IOException {
        Validate.isTrue(columns.size() == headers.length, "Row does not match header %s", columns);
        printer.printRecord(columns);
    }

    @Override
    public void close() throws IOException {
        printer.close(true);
    }

    public static void write(CompositeMetricRegistry registry, Path filePath,
                               Map<String, String> staticFields) throws IOException {
        List<String> csvHeaders = new ArrayList<>(registry.getCsvHeaders());
        csvHeaders.addAll(staticFields.keySet());
        try(CsvOutput csvOutput = new CsvOutput(csvHeaders, filePath)) {
            csvOutput.write(registry.getResultAsRows(), staticFields);
        }
    }

    public static void write(CompositeMetricRegistry registry, Path filePath,
                             List<Map.Entry<String, String>> staticFields) throws IOException {
        List<String> csvHeaders = new ArrayList<>(registry.getCsvHeaders());
        csvHeaders.addAll(staticFields.stream().map(Map.Entry::getKey).toList());
        try(CsvOutput csvOutput = new CsvOutput(csvHeaders, filePath)) {
            csvOutput.write(registry.getResultAsRows(), Map.ofEntries(staticFields.toArray(Map.Entry[]::new)));

        }
    }
}
