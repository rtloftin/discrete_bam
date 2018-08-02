package bam.human.analysis;

import bam.algorithms.optimization.Optimization;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents data collected from a single participant.
 * Really just a collection of sessions, with the
 * tutorial session separated out.  Eventually we might
 * have more user-specific data, like survey results.
 */
public class UserRecord {

    // The data from all the experimental sessions
    private SessionRecords sessions;

    // Verification code
    private String code;

    // The number of seconds required to complete the study
    private double duration;

    private UserRecord(SessionRecords sessions, String code, double duration)  {
        this.sessions = sessions;
        this.code = code;
        this.duration = duration;
    }

    public static Optional<UserRecord> load(Path root, EventDecoder decoder) {
        int id = Integer.parseInt(root.getFileName().toString());
        Path session_root = root.resolve("sessions");

        if(!Files.exists(session_root)) {
            System.out.println("Could not load user " + id + ", no sessions saved");
            return Optional.empty();
        }

        try {

            // Load sessions
            DirectoryStream<Path> directories = Files.newDirectoryStream(root.resolve("sessions"));
            List<SessionRecord> sessions = new ArrayList<>();

            for (Path directory : directories)
                sessions.add(SessionRecord.load(directory, decoder, id));

            // Get duration
            double duration = 0.0;

            BufferedReader log = new BufferedReader(new InputStreamReader(Files.newInputStream(root.resolve("log"))));
            String first = log.readLine();
            String last = first;

            if(null != first) {
                String line = log.readLine();

                while (null != line) {
                    last = line;
                    line = log.readLine();
                }

                Instant start = Instant.parse(first.split("\\s\\|\\s")[0]);
                Instant end = Instant.parse(last.split("\\s\\|\\s")[0]);

                duration = Duration.between(start, end).getSeconds();
            }

            // Get verification code if it exists
            Path code_path = root.resolve("code");
            String code = null;

            if(Files.exists(code_path)) {
                BufferedReader code_file = new BufferedReader(new InputStreamReader(Files.newInputStream(code_path)));
                code = code_file.readLine();
            }

            return Optional.of(new UserRecord(SessionRecords.of(sessions), code, duration));
        } catch(IOException|JSONException e) {
            System.out.println("Could not load user " + id + ", error: " + e.getMessage());

            return Optional.empty();
        }
    }

    public SessionRecords sessions() {
        return sessions;
    }

    public String code() {
        return code;
    }

    public double duration() {
        return duration;
    }
}
