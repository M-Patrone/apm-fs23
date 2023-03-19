package ch.fhnw.apm.io;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
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

    //@Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readBytewise() throws IOException {
        try (var in = Files.newInputStream(file(5_000_000))) {
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

    @Param({"4096", "512", "32768"})
    public int bufferSize;

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readBuffered() throws IOException {
        try (var in = new BufferedInputStream(Files.newInputStream(file(5_000_000)), bufferSize)) {
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

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readByteArray() throws IOException {
        try (var in = Files.newInputStream(file(5_000_000))) {
            int byteZeroCount = 0;
            int byteCount;
            byte[] buffer = new byte[bufferSize];
            while ((byteCount = in.read(buffer)) >= 0) {
                for (int i = 0; i < byteCount; i++) {
                    if (buffer[i] == 0) {
                        byteZeroCount++;
                    }
                }
            }
            return byteZeroCount;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readBufferedNioNoBufferWrapper() throws IOException {
        try ( var fin =  new FileInputStream(file(5_000_000).toFile());
                 FileChannel fcin = fin.getChannel();
        ){

            ByteBuffer buffer = ByteBuffer.allocate( 1024 );
            int bytesRead;
            do {
                bytesRead = fcin.read(buffer);
                if (bytesRead > 0) {
                    // Do something with the data in the buffer
                    buffer.flip();
                    buffer.clear();
                }
            } while (bytesRead != -1);
            return bytesRead;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readBufferedNioBufferWrapper() throws IOException {
        try ( var fin =  new FileInputStream(file(5_000_000).toFile());
              FileChannel fcin = fin.getChannel();
        ){
            //sollte effizienter sein, da kein kopieren notwendig ist
            byte[] array = new byte[1024];
            ByteBuffer buffer = ByteBuffer.wrap(array);
            int bytesRead;
            do {
                bytesRead = fcin.read(buffer);
                if (bytesRead > 0) {
                    // Do something with the data in the buffer
                    buffer.flip();
                    buffer.clear();
                }
            } while (bytesRead != -1);
            return bytesRead;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public int readBufferedMemoryMappedFile() throws IOException {
        try ( var fin =  new FileInputStream(file(5_000_000).toFile());
              FileChannel fcin = fin.getChannel();
        ){
            //sollte effizienter sein, da kein kopieren notwendig ist
            MappedByteBuffer buffer = fcin.map( FileChannel.MapMode.READ_WRITE, 0, 1024);
            int bytesRead;
            do {
                bytesRead = fcin.read(buffer);
                if (bytesRead > 0) {
                    // Do something with the data in the buffer
                    buffer.flip();
                    buffer.clear();
                }
            } while (bytesRead != -1);
            return bytesRead;
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