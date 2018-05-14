package bam.human.analysis;

import bam.human.Session;

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

    boolean good(UserRecord user);
}
