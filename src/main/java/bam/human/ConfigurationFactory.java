package bam.human;

import bam.algorithms.Algorithm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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

    private static class Environment {

        String name;
        Remote.Factory factory;
        HashMap<String, Algorithm> algorithms;

        Environment(String name, Remote.Factory factory, HashMap<String, Algorithm> algorithms) {
            this.name = name;
            this.factory = factory;
            this.algorithms = algorithms;
        }

        Environment(JSONObject config) throws JSONException {
            this.name = config.getString("name");
            this.factory = Remote.load(config.getJSONObject("environment"));
            this.algorithms = new HashMap<>();

            JSONArray algs = config.getJSONArray("algorithms");

            for(int i=0; i < algs.length(); ++i) {
                Algorithm algorithm = Algorithm.load(algs.getJSONObject(i));
                algorithms.put(algorithm.name(), algorithm);
            }
        }

        JSONObject serialize() throws JSONException {
            JSONArray algs = new JSONArray();

            for(Algorithm algorithm : algorithms.values())
                algs.put(algorithm.serialize());

            return new JSONObject()
                    .put("name", name)
                    .put("environment", factory.serialize())
                    .put("algorithms", algs);
        }
    }

    private static class Domain {

        String name;
        HashMap<String, Environment> environments;

        Domain(String name, HashMap<String, Environment> environments) {
            this.name = name;
            this.environments = environments;
        }

        Domain(JSONObject config) throws JSONException {
            this.name = config.getString("name");
            this.environments = new HashMap<>();

            JSONArray envs = config.getJSONArray("environments");

            for(int i=0; i < envs.length(); ++i) {
                Environment environment = new Environment(envs.getJSONObject(i));
                environments.put(environment.name, environment);
            }
        }

        JSONObject serialize() throws JSONException {
            JSONArray envs = new JSONArray();

            for(Environment environment : environments.values())
                envs.put(environment.serialize());

            return new JSONObject()
                    .put("name", name)
                    .put("environments", envs);
        }
    }

    private HashMap<String, Domain> domains;

    private ConfigurationFactory(HashMap<String, Domain> domains) { this.domains = domains; }

    public static ConfigurationFactory test() {
        return new ConfigurationFactory(new HashMap<>());
    }

    public static ConfigurationFactory base() {
        return new ConfigurationFactory(new HashMap<>());
    }

    public static ConfigurationFactory load(JSONObject config) throws JSONException {
        HashMap<String, Domain> domains = new HashMap<>();

        JSONArray doms = config.getJSONArray("domains");

        for(int i=0; i < doms.length(); ++i) {
            Domain domain = new Domain(doms.getJSONObject(i));
            domains.put(domain.name, domain);
        }

        return new ConfigurationFactory(domains);
    }

    @Override
    public Session build(Connection connection, Directory directory, JSONObject config) throws Exception {

        // Parse session configuration
        JSONObject condition = config.getJSONObject("condition");
        JSONObject initial = config.getJSONObject("initial");

        // Get environment and algorithm
        Environment environment = domains.get(condition.getString("domain"))
                .environments.get(condition.getString("environment"));
        Algorithm algorithm = environment.algorithms.get(condition.getString("algorithm"));

        // Save session configuration
        directory.save("environment", environment.factory.serialize().toString(4));
        directory.save("algorithm", algorithm.serialize().toString(4));

        // Construct remote simulation
        Remote remote = environment.factory.build(algorithm, initial);

        // Construct session
        return Session.build(remote, connection, directory);
    }

    public JSONObject serialize() throws JSONException {
        JSONArray doms = new JSONArray();

        for(Domain domain : domains.values())
            doms.put(domain.serialize());

        return new JSONObject()
                .put("domains", doms);
    }
}
