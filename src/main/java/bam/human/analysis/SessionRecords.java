package bam.human.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a collection of session
 * records, which may be filtered based
 * on some criteria.
 */
public class SessionRecords implements Iterable<SessionRecord> {

    private List<SessionRecord> sessions;

    private SessionRecords(List<SessionRecord> sessions) {
        this.sessions = sessions;
    }

    public static SessionRecords of(List<SessionRecord> sessions) {
        return new SessionRecords(sessions);
    }

    public SessionRecords filter(SessionFilter... filters) {
        List<SessionRecord> filtered = new ArrayList<>();

        for(SessionRecord session : sessions) {
            boolean good_session = true;

            for(SessionFilter filter : filters)
                good_session &= filter.good(session);

            if(good_session)
                filtered.add(session);
        }

        return new SessionRecords(filtered);
    }

    public SessionRecords concat(SessionRecords... session_records) {
        List<SessionRecord> concatenated = new ArrayList<>();

        for(SessionRecord record : sessions)
            concatenated.add(record);

        for(SessionRecords records : session_records)
            for(SessionRecord record : records)
                concatenated.add(record);

        return new SessionRecords(concatenated);
    }

    public int size() {
        return sessions.size();
    }

    public SessionRecord get(int index) {
        return sessions.get(index);
    }

    @Override
    public Iterator iterator() {
        return sessions.iterator();
    }
}
