package bam.human.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a collection user records,
 * which may be filtered based on some criteria.
 */
public class UserRecords implements Iterable<UserRecord> {

    private List<UserRecord> users;

    private UserRecords(List<UserRecord> users) {
        this.users = users;
    }

    public static UserRecords of(List<UserRecord> users) {
        return new UserRecords(users);
    }

    public UserRecords filter(UserFilter... filters) {
        List<UserRecord> filtered = new ArrayList<>();

        for(UserRecord user : users) {
            boolean good_user = true;

            for(UserFilter filter : filters)
                good_user &= filter.good(user);

            if(good_user)
                filtered.add(user);
        }

        return new UserRecords(filtered);
    }

    public SessionRecords filter(SessionFilter... filters) {
        List<SessionRecord> filtered = new ArrayList<>();

        for(UserRecord user : users) {
            for(SessionRecord session : user.sessions()) {
                boolean good_session = true;

                for(SessionFilter filter : filters)
                    good_session &= filter.good(session);

                if(good_session)
                    filtered.add(session);
            }
        }

        return SessionRecords.of(filtered);
    }

    public UserRecords concat(UserRecords... user_records) {
        List<UserRecord> concatenated = new ArrayList<>();

        concatenated.addAll(users);

        for(UserRecords records : user_records)
            for(UserRecord record : records)
                concatenated.add(record);

        return new UserRecords(concatenated);
    }

    public int size() {
        return users.size();
    }

    public UserRecord get(int index) {
        return users.get(index);
    }

    @Override
    public Iterator iterator() {
        return users.iterator();
    }
}
