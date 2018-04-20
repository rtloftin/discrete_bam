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

    private UserRecords users;

    private DataSet(UserRecords users) {
        this.users = users;
    }

    public static DataSet load(Path root, EventDecoder decoder) throws IOException {

        // Load users
        DirectoryStream<Path> user_directories = Files.newDirectoryStream(root.resolve("users"));
        List<UserRecord> users = new ArrayList<>();

        for(Path directory : user_directories)
            UserRecord.load(directory, decoder).ifPresent(users::add);

        return new DataSet(UserRecords.of(users));
    }

    public static DataSet load(Path root) throws IOException {
        return load(root, EventDecoder.JSON());
    }

    public UserRecords participants() {
        return users;
    }
}
