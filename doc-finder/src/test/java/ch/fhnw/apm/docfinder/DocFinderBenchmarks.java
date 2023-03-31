package ch.fhnw.apm.docfinder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class DocFinderBenchmarks {

    private DocFinder finder;
    //public static final String SEARCH_TEXT = "woman friend cat";

    @Param({"16","128"})
    public int threads;
    @Param({"woman","woman friend cat help test b"})
    public String searchText;

    @Param({"false","true"})
    public boolean ignoreCase;

    @Setup
    public void setup(){
        var booksDir = Path.of("perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }

        finder = new DocFinder(booksDir, threads);
        finder.setIgnoreCase(ignoreCase);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Warmup(iterations = 2)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Result> DocFinder() throws IOException {
       return finder.findDocs(searchText);
    }
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DocFinderBenchmarks.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
