package bam.human.analysis;

public interface UserFilter {

    static UserFilter all(UserFilter... filters) {
        return (UserRecord user) -> {
            for(UserFilter filter : filters)
                if(!filter.good(user))
                    return false;

            return true;
        };
    }

    boolean good(UserRecord user);
}
