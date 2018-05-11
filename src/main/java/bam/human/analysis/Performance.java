package bam.human.analysis;

import bam.algorithms.Behavior;
import bam.algorithms.Dynamics;
import bam.algorithms.Policy;
import bam.domains.Environment;
import bam.domains.ExpertBehavior;
import bam.domains.RandomBehavior;
import bam.domains.Task;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Performance {

    public static class Evaluation {

        private final Dynamics dynamics;
        private final int depth;
        private final int episodes;

        private final List<? extends Task> tasks;

        private final Random random;

        private final Map<String, Double> random_baselines;
        private final Map<String, Double> expert_baselines;

        private Evaluation(int episodes, Environment environment, List<? extends Task> tasks) {
            this.episodes = episodes;
            this.dynamics = environment.dynamics();
            this.depth = dynamics.depth();

            this.tasks = tasks;

            this.random = ThreadLocalRandom.current();

            // Compute random and expert baselines
            Behavior baseline = RandomBehavior.with(environment, tasks);
            Behavior expert = ExpertBehavior.with(environment, tasks);

            random_baselines = new HashMap<>();
            expert_baselines = new HashMap<>();

            // Compute random baselines
            for(Task task : tasks) {
                Policy policy = baseline.policy(task.name());
                double value  = 0.0;

                for(int episode = 0; episode < episodes; ++episode)
                    value += dynamics.simulate(policy, task, task.initial(random), depth, random);

                random_baselines.put(task.name(), (value / episodes));
            }

            // Compute expert baselines
            for(Task task : tasks) {
                Policy policy = expert.policy(task.name());
                double value  = 0.0;

                for(int episode = 0; episode < episodes; ++episode)
                    value += dynamics.simulate(policy, task, task.initial(random), depth, random);

                expert_baselines.put(task.name(), (value / episodes));
            }
        }

        Performance of(Behavior behavior) {
            double ratio = 0.0;
            double trained_ratio = 0.0;

            Map<String, Double> task_ratios = new HashMap<>();

            for(Task task : tasks) {
                double random_baseline = random_baselines.get(task.name());
                double expert_baseline = expert_baselines.get(task.name());

                if(behavior.has(task.name())) {
                    Policy policy = behavior.policy(task.name());
                    double value = 0.0;

                    for(int episode = 0; episode < episodes; ++episode)
                        value += dynamics.simulate(policy, task, task.initial(random), depth, random);

                    value /= (episodes * expert_baseline);

                    ratio += value;
                    trained_ratio += value;

                    task_ratios.put(task.name(), value);
                } else {
                    ratio += (random_baseline / expert_baseline);
                }
            }

            ratio /= tasks.size();
            trained_ratio /= Math.max(1.0, task_ratios.size());

            return new Performance(ratio, trained_ratio, task_ratios);
        }

        public Set<String> tasks() {
            return random_baselines.keySet();
        }

        public double expert(String task) {
            return expert_baselines.getOrDefault(task, 0.0);
        }

        public double random(String task) {
            return random_baselines.getOrDefault(task, 0.0);
        }
    }

    public static Evaluation evaluation(int episodes, Environment environment) {
        return new Evaluation(episodes, environment, environment.tasks());
    }

    public static Evaluation evaluation(int episodes, Environment environment, Task... tasks) {
        return new Evaluation(episodes, environment, List.of(tasks));
    }

    private final double ratio;
    private final double trained_ratio;

    private final Map<String, Double> task_ratios;

    private Performance(double ratio, double trained_ratio, Map<String, Double> task_ratios) {
        this.ratio = ratio;
        this.trained_ratio = trained_ratio;
        this.task_ratios = task_ratios;
    }

    public double ratio() {
        return ratio;
    }

    public double trainedRatio() {
        return trained_ratio;
    }

    public boolean trained(String task) {
        return task_ratios.containsKey(task);
    }

    public double ratio(String task) {
        return task_ratios.getOrDefault(task, 0.0);
    }
}
