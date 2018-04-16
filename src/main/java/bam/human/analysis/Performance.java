package bam.human.analysis;

import bam.algorithms.Behavior;
import bam.algorithms.Dynamics;
import bam.algorithms.Policy;
import bam.domains.Environment;
import bam.domains.ExpertBehavior;
import bam.domains.RandomBehavior;
import bam.domains.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Performance {

    public static class Evaluation {

        private final Dynamics dynamics;
        private final int depth;
        private final int episodes;

        private final List<? extends Task> tasks;

        private final Map<String, Double> random_baselines;
        private final Map<String, Double> expert_baselines;

        private Evaluation(int episodes, Environment environment, List<? extends Task> tasks) {
            this.episodes = episodes;
            this.dynamics = environment.dynamics();
            this.depth = dynamics.depth();

            this.tasks = tasks;

            // Compute random and expert baselines
            Behavior random = RandomBehavior.with(environment, tasks);
            Behavior expert = ExpertBehavior.with(environment, tasks);

            random_baselines = new HashMap<>();
            expert_baselines = new HashMap<>();

            for(Task task : tasks) {
                Policy policy = random.policy(task.name());
                double value  = 0.0;

                for(int episode = 0; episode < episodes; ++episode) {
                    int start = task.initial(ThreadLocalRandom.current());
                    value += dynamics.simulate(policy, task, start, depth, ThreadLocalRandom.current());
                }

                random_baselines.put(task.name(), value / episodes);
            }

            for(Task task : tasks) {
                Policy policy = expert.policy(task.name());
                double value  = 0.0;

                for(int episode = 0; episode < episodes; ++episode) {
                    int start = task.initial(ThreadLocalRandom.current());
                    value += dynamics.simulate(policy, task, start, depth, ThreadLocalRandom.current());
                }

                expert_baselines.put(task.name(), value / episodes);
            }
        }

        Performance of(Behavior behavior) {
            double mean = 0.0;
            double trained_mean = 0.0;

            Map<String, Double> task_means = new HashMap<>();

            for(Task task : tasks) {
                if(behavior.has(task.name())) {
                    Policy policy = behavior.policy(task.name());
                    double value = 0.0;

                    for(int episode = 0; episode < episodes; ++episode) {
                        int start = task.initial(ThreadLocalRandom.current());
                        value += dynamics.simulate(policy, task, start, depth, ThreadLocalRandom.current());
                    }

                    value /= episodes * depth;

                    mean += value;
                    trained_mean += value;

                    task_means.put(task.name(), value);
                } else {
                    mean += random_baselines.get(task.name());
                }
            }

            mean /= tasks.size();
            trained_mean /= task_means.size();

            return new Performance(mean, trained_mean, task_means);
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

    private final double mean;
    private final double trained_mean;

    private final Map<String, Double> task_means;

    private Performance(double mean, double trained_mean, Map<String, Double> task_means) {
        this.mean = mean;
        this.trained_mean = trained_mean;
        this.task_means = task_means;
    }

    public double mean() {
        return mean;
    }

    public double trainedMean() {
        return trained_mean;
    }

    public boolean trained(String task) {
        return task_means.containsKey(task);
    }

    public double mean(String task) {
        return task_means.getOrDefault(task, 0.0);
    }
}
