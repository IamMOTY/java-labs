package info.kgeorgiy.ja.buduschev.stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    public static String readAll(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            stream.forEach(s -> sb.append(s).append(System.lineSeparator()));
        }
        return sb.toString();
    }

    /**
     * {@code <inputLocale> <outPutLocale> <inputFile> <outputFile>}
     * @param args
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Arguments is null");
            return;
        }
        if (args.length != 4) {
            System.err.println("Invalid count of arguments");
            return;
        }
        Locale inputLocale = Locale.forLanguageTag(args[0]);
        Locale outputLocale = Locale.forLanguageTag(args[1]);
        Path inputPath = Path.of(args[2]);
        Path outputPath = Path.of(args[3]);
        try {
            FullStatistic ts = new FullStatistic(readAll(inputPath), inputLocale);
            ReportBuilder rb = new ReportBuilder(ts, inputPath, outputLocale);
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writer.write(rb.getReport());
            } catch (IOException e) {
                System.err.println("Error while writing" + e.getLocalizedMessage());
            }
        } catch (IOException e) {
            System.err.println("Error while read input" + e.getLocalizedMessage());
        }
    }
}
