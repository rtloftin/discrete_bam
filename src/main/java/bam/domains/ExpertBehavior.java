package bam.domains;

import bam.algorithms.Behavior;
import bam.algorithms.Expert;

import java.util.List;

public class ExpertBehavior {

    public static Behavior with(Environment environment, List<? extends Task> tasks) {
        Behavior behavior = Behavior.get();

        for(Task task : tasks)
            behavior.put(task.name(), Expert.with(environment.dynamics(), task).policy());

        return behavior;
    }
}
