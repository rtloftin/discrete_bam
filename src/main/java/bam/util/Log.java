package bam.util;

import java.io.*;
import java.time.Instant;

/**
 * A utility class which implements
 * logging functionality.  Logs
 * will generally be independent of
 * snapshots.  Note that the
 * timestamps used may not be the
 * most accurate available, so
 * should not be used for benchmarking
 * specific pieces of the code.
 *
 * Created by Tyler on 9/26/2017.
 */
public class Log {

    private PrintStream[] streams;

    private Log(PrintStream... streams) { this.streams = streams; }

    public static Log create(PrintStream... streams) { return new Log(streams); }

    public static Log dummy() { return new Log(); }

    public static Log console() { return new Log(new PrintStream(System.out)); }

    public static Log file(File file) throws IOException { return new Log(new PrintStream(file)); }

    public static Log combined(File file) throws IOException {
        return new Log(new PrintStream(file), new PrintStream(System.out));
    }

    public Log write(String message) {

        // Create entry
        String entry = Instant.now() + " " + message;

        synchronized (this) {
            for(PrintStream stream : streams) {
                stream.println(entry);
                stream.flush();
            }
        }

        return this;
    }

    public void close() {
        for(PrintStream stream : streams)
            stream.close();
    }
}
