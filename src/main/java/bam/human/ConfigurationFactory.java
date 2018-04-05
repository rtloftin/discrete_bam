package bam.human;


import bam.algorithms.Algorithm;
import bam.algorithms.BAM;
import bam.algorithms.Cloning;
import bam.algorithms.ModelBased;
import bam.algorithms.action.OldNormalizedActionModel;
import bam.algorithms.optimization.Momentum;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.variational.PointDensity;

import bam.domains.Experts;
import bam.domains.farm_world.FarmWorld;
import bam.domains.farm_world.FarmWorlds;
import bam.domains.gravity_world.GravityWorld;
import bam.domains.gravity_world.GravityWorlds;
import bam.domains.grid_world.GridWorld;
import bam.domains.grid_world.GridWorlds;
import bam.human.domains.RemoteFarmWorld;
import bam.human.domains.RemoteGravityWorld;
import bam.human.domains.RemoteGridWorld;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class builds sessions from a fixed set of configurations
 * defined by a collection of configuration files.  The session
 * configurations should be loaded from the files when the server
 * starts, so that configuration errors are detected ahead of time.
 *
 * Each configuration is defined by a domain, and environment within
 * that domain, and a learning algorithm.  Learning algorithm configurations
 * are specific to an environment, so that we can taylor each algorithm
 * to the requirements of the learning problem.  Configurations are stored in
 * a directory structure, with a root directory containing directories for
 * each domain, which then contain directories for each environment, which contain
 * A configuration file for that environment, and a directory with configuration
 * files for each algorithm.
 */
public class ConfigurationFactory implements Session.Factory {

    private static class Layout {

        String name;
        Remote.Factory factory;
        HashMap<String, Algorithm> algorithms;

        Layout(String name, Remote.Factory factory, Algorithm... algorithms) {
            this.name = name;
            this.factory = factory;

            this.algorithms = new HashMap<>();

            for(Algorithm algorithm : algorithms)
                this.algorithms.put(algorithm.name(), algorithm);
        }

        Layout(JSONObject config) throws JSONException {
            name = config.getString("name");
            factory = Remote.load(config.getJSONObject("environment"));

            algorithms = new HashMap<>();
            JSONObject json_algorithms = config.getJSONObject("algorithms");

            for(String algorithm : json_algorithms.keySet())
                algorithms.put(algorithm, Algorithm.load(json_algorithms.getJSONObject(algorithm)));
        }

        JSONObject serialize() throws JSONException {
            JSONObject json_algorithms = new JSONObject();

            for(Map.Entry<String, Algorithm> entry : algorithms.entrySet())
                json_algorithms.put(entry.getKey(), entry.getValue().serialize());

            return new JSONObject()
                    .put("name", name)
                    .put("environment", factory.serialize())
                    .put("algorithms", json_algorithms);
        }
    }

    private static class Domain {

        String name;
        HashMap<String, Layout> layouts;

        Domain(String name, Layout... layouts) {
            this.name = name;

            this.layouts = new HashMap<>();

            for(Layout layout : layouts)
                this.layouts.put(layout.name, layout);
        }

        Domain(JSONObject config) throws JSONException {
            name = config.getString("name");

            layouts = new HashMap<>();
            JSONObject json_layouts = config.getJSONObject("environments");

            for(String layout : json_layouts.keySet())
                layouts.put(layout, new Layout(json_layouts.getJSONObject(layout)));
        }

        JSONObject serialize() throws JSONException {
            JSONObject json_layouts = new JSONObject();

            for(Map.Entry<String, Layout> entry : layouts.entrySet())
                json_layouts.put(entry.getKey(), entry.getValue().serialize());

            return new JSONObject()
                    .put("name", name)
                    .put("environments", json_layouts);
        }
    }

    private HashMap<String, Domain> domains;

    private ConfigurationFactory(Domain... domains) {
        this.domains = new HashMap<>();

        for(Domain domain : domains)
            this.domains.put(domain.name, domain);
    }

    private ConfigurationFactory(HashMap<String, Domain> domains) { this.domains = domains; }

    public static ConfigurationFactory test() {

        // BAM algorithm
        Algorithm bam = BAM.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.01, 0.5)).build())
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(OldNormalizedActionModel.beta(1.0))
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build();

        // Grid world tutorial domain and expert agent
        GridWorld grid_tutorial = GridWorlds.tutorial();
        Algorithm grid_expert = Experts.algorithm(grid_tutorial);

        Layout grid_layout = new Layout("tutorial", RemoteGridWorld.with(grid_tutorial), bam, grid_expert);
        Domain grid_world = new Domain("grid world", grid_layout);

        // Gravity world tutorial domain and expert agent
        GravityWorld gravity_tutorial = GravityWorlds.tutorial();
        Algorithm gravity_expert = Experts.algorithm(gravity_tutorial);

        Layout gravity_layout = new Layout("tutorial", RemoteGravityWorld.with(gravity_tutorial), bam, gravity_expert);
        Domain gravity_world = new Domain("gravity world", gravity_layout);

        // Farm world tutorial domain and expert agent
        FarmWorld farm_tutorial = FarmWorlds.tutorial();
        Algorithm farm_expert = Experts.algorithm(farm_tutorial);

        Layout farm_layout = new Layout("tutorial", RemoteFarmWorld.with(farm_tutorial), bam, farm_expert);
        Domain farm_world = new Domain("farm world", farm_layout);

        return new ConfigurationFactory(grid_world, gravity_world, farm_world);
    }

    public static ConfigurationFactory experiment() {

        // Algorithms - BAM, Model Based, Cloning
        Algorithm bam = BAM.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.01, 0.5)).build())
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(OldNormalizedActionModel.beta(1.0))
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build();

        Algorithm model = ModelBased.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.01, 0.5)).build())
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                .planningAlgorithm(BoltzmannPlanner.algorithm(1.0))
                .actionModel(OldNormalizedActionModel.beta(1.0))
                .taskUpdates(200)
                .dynamicsUpdates(200)
                .build();

        Algorithm cloning = Cloning.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.01, 0.5)).build())
                .actionModel(OldNormalizedActionModel.beta(1.0))
                .numUpdates(200)
                .build();

        // Grid World Domain
        GridWorld grid_tutorial = GridWorlds.tutorial();
        Algorithm grid_expert = Experts.algorithm(grid_tutorial);

        Layout grid_layout = new Layout("tutorial", RemoteGridWorld.with(grid_tutorial), grid_expert);
        Layout two_rooms = new Layout("two-rooms", RemoteGridWorld.with(GridWorlds.twoRooms()), bam, model, cloning);
        Layout doors = new Layout("doors", RemoteGridWorld.with(GridWorlds.doors()), bam, model, cloning);
        Domain grid_world = new Domain("grid world", grid_layout, two_rooms, doors);

        // Farm World Domain
        FarmWorld farm_tutorial = FarmWorlds.tutorial();
        Algorithm farm_expert = Experts.algorithm(farm_tutorial);

        Layout farm_layout = new Layout("tutorial", RemoteFarmWorld.with(farm_tutorial), farm_expert);
        Layout two_fields = new Layout("two-fields", RemoteFarmWorld.with(FarmWorlds.twoFields()), bam, model, cloning);
        Layout six_fields = new Layout("six-fields", RemoteFarmWorld.with(FarmWorlds.sixFields()), bam, model, cloning);
        Domain farm_world = new Domain("farm world", farm_layout, two_fields, six_fields);

        return new ConfigurationFactory(grid_world, farm_world);
    }

    public static ConfigurationFactory load(JSONObject config) throws JSONException {
        HashMap<String, Domain> domains = new HashMap<>();
        JSONObject json_domains = config.getJSONObject("domains");

        for(String domain : json_domains.keySet())
            domains.put(domain, new Domain(json_domains.getJSONObject(domain)));

        return new ConfigurationFactory(domains);
    }

    @Override
    public Session build(Connection connection, Directory directory, JSONObject config) throws Exception {

        // Parse session configuration
        JSONObject condition = config.getJSONObject("condition");
        JSONObject initial = config.getJSONObject("initial");

        // Get environment and algorithm
        Layout layout = domains.get(condition.getString("domain"))
                .layouts.get(condition.getString("environment"));
        Algorithm algorithm = layout.algorithms.get(condition.getString("algorithm"));

        // Save session configuration
        directory.save("environment", layout.factory.serialize().toString(4));
        directory.save("algorithm", algorithm.serialize().toString(4));

        // Construct remote simulation
        Remote remote = layout.factory.build(algorithm, initial);

        // Construct session
        return Session.build(remote, connection, directory);
    }

    public JSONObject serialize() throws JSONException {
        JSONObject json_domains = new JSONObject();

        for(Map.Entry<String, Domain> entry : domains.entrySet())
            json_domains.put(entry.getKey(), entry.getValue().serialize());

        return new JSONObject()
                .put("domains", json_domains);
    }
}
