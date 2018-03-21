package bam.human;

import bam.algorithms.Algorithm;
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
public class ConfigurationFactory implements OldSession.Factory {

    private HashMap<String, Remote.Factory> environments;
    private HashMap<String, Algorithm> algorithms;

    @Override
    public OldSession build(Connection connection, Directory directory, Connection.Message config) throws JSONException {
        JSONObject condition = config.data().getJSONObject("condition");
        JSONObject initial = config.data().getJSONObject("initial");

        // Get the algorithm and the environment
        String env_key = condition.getString("domain") + "_" + condition.getString("environment");
        String alg_key = env_key + "_" + condition.getString("algorithm");

        if(!environments.containsKey(env_key) || !algorithms.containsKey(alg_key))
            throw new JSONException("Unsupported environment or algorithm requested");

        Algorithm algorithm = algorithms.get(alg_key);
        Remote remote = environments.get(env_key).build(algorithm, initial);

        Connection.Listener listener = connection.listen()
                .add("take-action", (Connection.Message message) -> {
                    try {
                        remote.takeAction(message.data().getJSONObject("action"));

                        message.capture();
                        message.respond(new JSONObject()
                                .put("state", remote.getState())
                                .put("layout", remote.getLayout()));
                    } catch(JSONException e) { /* Ignore bad message */ }
                }).add("get-action", (Connection.Message message) -> {
                    try {
                        remote.takeAction();

                        message.capture();
                        message.respond(new JSONObject()
                                .put("state", remote.getState())
                                .put("layout", remote.getLayout()));
                    } catch(JSONException e) { /* Ignore bad message */ }
                }).add("task", (Connection.Message message) -> {
                    try {
                        remote.setTask(message.data());

                        message.capture();
                        message.respond(new JSONObject()
                                .put("state", remote.getState())
                                .put("layout", remote.getLayout()));

                        System.out.println("Set task to: " + message.data().getString("name"));
                    } catch(JSONException e) { System.out.println("failed to set task"); }
                }).add("update", (Connection.Message message) -> {
                    try {


                        // agent.integrate();

                        message.capture();
                        message.respond(new JSONObject());
                    } catch(JSONException e) { /* Nothing to be done */}
                }).add("reset", (Connection.Message message) -> {
                    try {
                        remote.resetState();

                        message.capture();
                        message.respond(new JSONObject()
                                .put("state", remote.getState())
                                .put("layout", remote.getLayout()));
                    } catch(JSONException e) { /* Nothing to be done */}
                });

        config.capture();
        config.respond(new JSONObject()
                .put("state", remote.getState())
                .put("layout", remote.getLayout())
                .put("tasks", remote.getTasks())
                .put("depth", remote.getDepth()));

        // Return new session, currently, there is no data recorded, but eventually we will want to close all of the data and log files when the session ends
        return new OldSession() {
            @Override
            public void end() { listener.remove(); }
        };
    }

    /**
     *
     */
    public static ConfigurationFactory baseConfiguration() {
        return null;
    }
}
