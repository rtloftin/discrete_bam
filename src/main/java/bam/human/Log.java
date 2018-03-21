package bam.human;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

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

    private List<PrintStream> streams;

    private Log(List<PrintStream> streams) { this.streams = streams; }

    public static Log dummy() {
        return new Log(new LinkedList<>());
    }

    public static Log create(OutputStream... streams) {
        List<PrintStream> print_streams = new LinkedList<>();

        for(OutputStream stream : streams)
            print_streams.add(new PrintStream(stream));

        return new Log(print_streams);
    }

    public static Log console(OutputStream... streams) {
        List<PrintStream> print_streams = new LinkedList<>();
        print_streams.add(new PrintStream(System.out));

        for(OutputStream stream : streams)
            print_streams.add(new PrintStream(stream));

        return new Log(print_streams);
    }

    public Log write(String message) {

        // Create entry
        String entry = Instant.now() + " | " + message;

        synchronized (this) {
            for(PrintStream stream : streams) {
                stream.println(entry);
                stream.flush();
            }
        }

        return this;
    }

    public void close() {
        synchronized (this) {
            for (PrintStream stream : streams)
                stream.close();
        }
    }
}
