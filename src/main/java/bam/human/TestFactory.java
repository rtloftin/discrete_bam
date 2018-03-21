package bam.human;


import bam.algorithms.Agent;
import bam.algorithms.BAM;
import bam.algorithms.action.NormalizedActionModel;
import bam.algorithms.optimization.Momentum;
import bam.algorithms.planning.BoltzmannPlanner;
import bam.algorithms.variational.PointDensity;
import bam.domains.NavGrid;
import bam.domains.grid_world.GridWorld;
import bam.domains.grid_world.GridWorlds;
import bam.human.domains.RemoteGridWorld;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A session in which the agent is already an
 * expert at performing all of the tasks, and
 * training messages are just ignored.  Used
 * for tutorials and for testing.
 */
public class TestFactory implements OldSession.Factory {

    @Override
    public OldSession build(Connection connection, Directory directory, Connection.Message config) throws JSONException {

        // Build the domain and agent

        //GridWorld environment = GridWorlds.centerBlock(NavGrid.FOUR);
        GridWorld environment = GridWorlds.threeRooms(NavGrid.FOUR);

        // Agent agent = DummyAgent.with(environment.representation());
        Agent agent = BAM.builder()
                .taskSource(PointDensity.builder()
                        .optimization(Momentum.with(0.01, 0.5)).build())
                .dynamicsOptimization(Momentum.with(0.01, 0.5))
                // .dynamicsOptimization(AdaGrad.with(1.0, 0.7))
                .planningAlgorithm(BoltzmannPlanner.algorithm( 1.0))
                .actionModel(NormalizedActionModel.beta(1.0))
                .taskUpdates(20)
                .dynamicsUpdates(20)
                .emUpdates(10)
                .useTransitions(true)
                .build().agent(environment.representation());

        // Initialize the remote environment
        RemoteGridWorld remote = RemoteGridWorld.with(environment, agent, config.data().getJSONObject("state"));

        // Attach message listeners
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

                        long time = System.nanoTime();
                        agent.integrate();
                        time = System.nanoTime() - time;
                        System.out.println("Data integration took " + (time / 1000000) + " milliseconds");

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
}
