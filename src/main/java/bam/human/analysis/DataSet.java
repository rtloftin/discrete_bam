package bam.human.analysis;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete data set consisting of
 * experimental results for each participant. Also
 * Includes any study-wide configuration info. This
 * Will contain methods for aggregating individual
 * sessions based on different criteria, particularly
 * by the learning algorithm used.
 */
public class DataSet {

    private List<UserRecord> users;

    private DataSet(List<UserRecord> users) { this.users = users; }

    public static DataSet load(Path root) throws IOException {

        DirectoryStream<Path> user_directories = Files.newDirectoryStream(root.resolve("users"));
        List<UserRecord> users = new ArrayList<>();

        for(Path directory : user_directories)
            UserRecord.load(directory).ifPresent((UserRecord user) -> users.add(user));

        return new DataSet(users);
    }

    public Sessions sessions(UserFilter user_filter, SessionFilter... session_filters) {
        List<SessionRecord> sessions = new ArrayList<>();

        for(UserRecord user : users) {
            if(user_filter.good(user))
                for(SessionRecord session : user.sessions()) {
                    boolean good_session = true;

                    for(SessionFilter filter : session_filters)
                        good_session &= filter.good(session);

                    if(good_session)
                        sessions.add(session);
                }
        }

        return Sessions.of(sessions);
    }

    public int participants() {
        return users.size();
    }
}
