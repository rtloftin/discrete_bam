package bam.human.analysis;

import bam.human.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Hashtable;

public interface UserFilter {

    static UserFilter all(UserFilter... filters) {
        return (UserRecord user) -> {
            for(UserFilter filter : filters)
                if(!filter.good(user))
                    return false;

            return true;
        };
    }

    static UserFilter completed(int num_sessions) {
        SessionFilter complete = SessionFilter.complete();

        return (UserRecord user) -> {
            int sessions = 0;

            for (SessionRecord session : user.sessions())
                if (complete.good(session))
                    ++sessions;

            return num_sessions <= sessions;
        };
    }

    static UserFilter codes(String... invalid) {
        HashSet<String> codes = new HashSet<>();

        for(String code : invalid)
            codes.add(code);

        return (UserRecord user) -> !codes.contains(user.code());
    }

    static UserFilter verified(Path codes) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(codes)));
        HashSet<String> valid = new HashSet<>();

        String line = reader.readLine();

        while(null != line) {
            valid.add(line);
            line = reader.readLine();
        }

        return (UserRecord user) -> valid.contains(user.code());
    }

    boolean good(UserRecord user);
}
