package bam.human.analysis;

import org.json.JSONException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    // The ID number for this user -- may eventually replace with a unique ID code that we use for verification
    private int id;

    private UserRecord(SessionRecords sessions, int id)  {
        this.sessions = sessions;
        this.id = id;
    }

    public static Optional<UserRecord> load(Path root, EventDecoder decoder) {
        int id = Integer.parseInt(root.getFileName().toString());
        Path session_root = root.resolve("sessions");

        if(!Files.exists(session_root)) {
            System.out.println("Could not load user " + id + ", no sessions saved");
            return Optional.empty();
        }

        try {
            DirectoryStream<Path> directories = Files.newDirectoryStream(root.resolve("sessions"));
            List<SessionRecord> sessions = new ArrayList<>();

            for (Path directory : directories)
                sessions.add(SessionRecord.load(directory, decoder));

            return Optional.of(new UserRecord(SessionRecords.of(sessions), id));
        } catch(IOException|JSONException e) {
            System.out.println("Could not load user " + id + ", error: " + e.getMessage());

            return Optional.empty();
        }
    }

    public SessionRecords sessions() {
        return sessions;
    }

    public int id() {
        return id;
    }
}
