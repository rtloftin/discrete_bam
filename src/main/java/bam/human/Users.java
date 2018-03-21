package bam.human;

import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of users connected to the server, and
 * the configuration for new users that we wish to add. This
 * class also allows us to limit the number of users in the system
 *
 * MAY EVENTUALLY COMBINE THIS WITH THE USER CLASS, DEPENDING ON HOW COMPLEX THESE BOTH BECOME
 */
class Users {

    /**
     * A builder class used to configure user
     * groups. Defines the maximum number of
     * users, the root data directory, and the
     * session factory for all users.
     */
    static class Builder {
        private int max_users = 4;
        private Directory directory = null;
        private OldSession.Factory session_factory = null;

        private Builder() {}

        public Builder maxUsers(int max_users) {
            this.max_users = max_users;

            return  this;
        }

        public Builder dataRoot(Directory directory) {
            this.directory = directory;

            return this;
        }

        public Builder sessionFactory(OldSession.Factory session_factory) {
            this.session_factory = session_factory;

            return this;
        }

        public Users build() {
            if(null == directory)
                throw new RuntimeException("No root directory defined for user data");
            if(null == session_factory)
                throw new RuntimeException("No environment factory defined");

            return new Users(this);
        }
    }

    static Builder builder() { return new Builder(); }

    // Configuration
    private final int max_users;
    private final Directory directory;
    private final OldSession.Factory session_factory;

    // The list of active users
    private final List<User> users;

    private Users(Builder builder) {
        this.max_users = builder.max_users;
        this.directory = builder.directory;
        this.session_factory = builder.session_factory;

        users = new LinkedList<>();
    }

    void add(Connection connection) {

        // Check if we have too many users already
        if(users.size() >= max_users)
            connection.refuse();

        // Try to create the user
        try {
            users.add(User.with(connection, directory.unique("users"), this));
        } catch(IOException e) {
            connection.decline();
        }
    }

    void remove(User user) {
        users.remove(user);
    }

    OldSession session(Connection connection, Directory directory, Connection.Message config) throws JSONException {
        return session_factory.build(connection, directory, config);
    }
}
