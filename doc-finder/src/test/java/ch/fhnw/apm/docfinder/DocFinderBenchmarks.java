package ch.fhnw.apm.docfinder;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@State(Scope.Benchmark)
public class DocFinderBenchmarks {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DocFinderBenchmarks.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

//    @Benchmark
//    public void helloJmh() {
//        System.out.println("Hello, JMH!");
//    }

    public final String SEARCH_TEXT = "woman friend cat";
    public DocFinder finder;

    @Setup
    public void setUp(){
        var booksDir = Path.of("perf-tests/books").toAbsolutePath();
        this.finder=new DocFinder(booksDir);;
    }
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3, time = 20)
    public List<Result> docFinderJmh() throws IOException {
        return finder.findDocs(SEARCH_TEXT);
    }
}
