package bam.human.analysis;

import java.util.ArrayList;
import java.util.List;

public interface UserStatistic<T> {

    static UserStatistic<Integer> sessions() {
        return (UserRecord user) -> user.sessions().size();
    }

    T of(UserRecord user);

    default List<T> of(UserRecords records) {
        List<T> statistics = new ArrayList<>();

        for(UserRecord record : records)
            statistics.add(of(record));

        return statistics;
    }
}
