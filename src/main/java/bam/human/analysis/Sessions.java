package bam.human.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific collection of
 * learning sessions for which summary
 * statistics can be computed.  Can be
 * filtered into smaller data sets, which
 * is necessary for separating data into
 * different experimental conditions.
 */
public class Sessions {

    private List<SessionRecord> sessions;

    private Sessions(List<SessionRecord> sessions) { this.sessions = sessions; }

    public static Sessions of(List<SessionRecord> sessions) {
        return new Sessions(sessions);
    }

    public Sessions filter(SessionFilter filter) {
        List<SessionRecord> filtered = new ArrayList<>();

        for(SessionRecord session : sessions)
            if(filter.good(session))
                filtered.add(session);

        return new Sessions(filtered);
    }

    public List<SessionRecord> records() {
        return sessions;
    }
}
