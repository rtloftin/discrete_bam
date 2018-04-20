package bam.human;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * This class represents a reference
 * to a data directory, and includes
 * methods for generating uniquely
 * named and/or timestamped
 * sub-directories.
 */
public class Directory {

    private Path path;

    private Directory(Path path) { this.path = path; }

    public static Directory dummy(String name) throws IOException {
        return new Directory(Files.createTempDirectory(name));
    }

    public static Directory local(String path) throws IOException {
        return new Directory(Files.createDirectories(Paths.get(path)));
    }

    public static Directory local(Path path) throws IOException {
        return new Directory(Files.createDirectories(path));
    }

    public void save(String name, String content) throws IOException {
        PrintStream stream = new PrintStream(stream(name));
        stream.print(content);
        stream.close();
    }

    public OutputStream stream(String name) throws IOException {
        return Files.newOutputStream(path.resolve(name));
    }

    public Directory unique(String name) throws IOException {
        Path dir = path.resolve(name);
        int index = 0;

        while(Files.exists(dir.resolve(String.format("%04d", index))))
            ++index;

        return new Directory(Files.createDirectories(dir.resolve(String.format("%04d", index))));
    }

    public Directory timestamped(String name) throws IOException {

        // Format current time as string
        String time = DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm")
                .withZone(ZoneId.systemDefault()).format(Instant.now());

        // Get unique path
        Path parent = path.resolve(name);
        Path child = parent.resolve(time);

        if(Files.exists(child)) {
            int index = 1;

            do {
                child = parent.resolve(time + String.format("_%04d", index++));
            } while(Files.exists(child));
        }

        // Create directory
        return new Directory(Files.createDirectories(child));
    }
}
