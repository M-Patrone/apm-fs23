package ch.fhnw.apm.io;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newOutputStream;

@State(Scope.Benchmark)
public class FileIOBenchmarks {

    private static final Path BASE_DIR = Path.of("files");

    private static final Set<Integer> FILE_SIZES = Set.of(
            5_000_000,
            50_000_000,
            250_000_000);

    static {
        if (!exists(BASE_DIR)) {
            throw new AssertionError("'files' dir not found; is benchmark executed in correct dir?");
        }
        for (var size : FILE_SIZES) {
            var file = file(size);
            if (!exists(file)) {
                System.err.print("creating file '" + file + "'... ");
                createRandomFile(file, size);
                System.err.println("done.");
            }
        }
    }

    private static void createRandomFile(Path file, int size) {
        var random = new Random();
        try (var out = new BufferedOutputStream(newOutputStream(file))) {
            for (int i = 0; i < size; i++) {
                out.write(random.nextInt());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path file(int size) {
        if (!FILE_SIZES.contains(size)) {
            throw new AssertionError("invalid file size: " + size + "; add to FILE_SIZES before running the benchmark");
        }
        return BASE_DIR.resolve("file-" + size + ".bin");
    }

    //@Param({"50000000","5000000"})
    @Param({"5000000"})
    public int fileSize;

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public int read() throws IOException {
        try (var in = Files.newInputStream(file(fileSize))) {
            int byteZeroCount = 0;
            int b;
            while ((b = in.read()) >= 0) {
                if (b == 0) {
                    byteZeroCount++;
                }
            }
            return byteZeroCount;
        }
    }

//    @Benchmark
//    @BenchmarkMode(Mode.SampleTime)
//    @Warmup(iterations = 1)
//    @Measurement(iterations = 3)
//    public int readBuffer() throws IOException {
//        try (var in = new BufferedInputStream(Files.newInputStream(file(fileSize)))) {
//            int byteZeroCount = 0;
//            int b;
//            while ((b = in.read()) >= 0) {
//                if (b == 0) {
//                    byteZeroCount++;
//                }
//            }
//            return byteZeroCount;
//        }
//    }
//    @Benchmark
//    @BenchmarkMode(Mode.SampleTime)
//    @Warmup(iterations = 1)
//    @Measurement(iterations = 3)
//    public int readSeveralBytes() throws IOException {
//        try (var in = (Files.newInputStream(file(fileSize)))) {
//            int byteZeroCount = 0;
//            byte[] bytes = new byte[1024];
//            int b;
//            while ((b = in.read(bytes)) >= 0) {
//
//                if (b == 0) {
//                    byteZeroCount+=b;
//                }
//            }
//            return byteZeroCount;
//        }
//    }

    @Param({"512","8192","32768"})
    private int bufferSize;
//    @Benchmark
//    @BenchmarkMode(Mode.SampleTime)
//    @Warmup(iterations = 1)
//    @Measurement(iterations = 3)
//    public int readByteWithBufferSize() throws IOException {
//        try (var in = (Files.newInputStream(file(fileSize))) ) { //hinzufügen von BufferedInputstream
//            int byteZeroCount = 0;
//            while(true) {
//                var bytes = in.readNBytes(bufferSize); //buffer grösse
//                if(bytes.length == 0){
//                    break;
//                }
//                for (byte b : bytes) {
//                    if (b == 0) {
//                        byteZeroCount++;
//                    }
//                }
//            }
//            return byteZeroCount;
//        }
//    }
//    @Benchmark
//    @BenchmarkMode(Mode.SampleTime)
//    @Warmup(iterations = 1)
//    @Measurement(iterations = 3)
//    public int readByteWithBuffer() throws IOException {
//        try (var in = Files.newInputStream(file(fileSize))) {
//            int byteZeroCount = 0;
//            int byteCount;
//            byte[] buffer = new byte[bufferSize];
//            while ((byteCount = in.read(buffer)) >= 0) {
//                for (int i = 0; i < byteCount; i++) {
//                    if (buffer[i] == 0) {
//                        byteZeroCount++;
//                    }
//                }
//            }
//            return byteZeroCount;
//        }
//    }
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    public int readByteWithBufferNio() throws IOException {
        try (var in = new FileInputStream(file(fileSize).toFile());
             FileChannel fcin = in.getChannel();
        ) {
            int byteZeroCount = 0;
            int byteCount;
            ByteBuffer bf = ByteBuffer.allocateDirect(bufferSize);
            while ((byteCount = fcin.read(bf)) >= 0) {
                byteZeroCount+=byteCount;
                bf.flip();
                bf.clear();
            }
            return byteZeroCount;
        }
    }



    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FileIOBenchmarks.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}