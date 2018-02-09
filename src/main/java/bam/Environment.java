package bam;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

/**
 * This interface represents an entire
 * multitask learning problem.  It includes
 * the true transition dynamics, a space of
 * possible dynamics models, a mapping from
 * reward functions to intents, and a set
 * of named tasks.
 *
 * Not clear how the domain and environment interfaces should relate. In our
 * simulations, we will always be working with a combination of an environment
 * and a domain.  We might however have domain objects on their own, with no
 * associated environment, as would be the case with the server.  Not clear if
 * we would ever have an environment without a domain, but it could happen.
 *
 * Created by Tyler on 7/26/2017.
 *
 */
public interface Environment {

    /**
     * Gets the transition dynamics for this
     * environment.
     *
     * @return the transition dynamics.
     */
    Dynamics dynamics();

    /**
     * Gets a list of tasks defined
     * for this environment.
     *
     * @return the list of tasks defined in this environment.
     */
    List<? extends Task> tasks();

    /**
     * Gets the default representation for
     * this environment and its tasks.
     *
     * @return the object containing the representation information for this environment
     */
    Representation representation();

    /**
     * Gets the name of the environment.
     *
     * @return the name of the environment
     */
    String name();

    /**
     * Gets a JSON representation of the configuration
     * options for this environment.
     *
     * @return a JSON object representing this environment
     * @throws JSONException if something goes wrong during serialization
     */
    default JSONObject serialize() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name());

        return json;
    }

    /**
     * May render an image of the environment.
     *
     * Potential issue, requires that the image be rendered to know if it is possible to render
     *
     * @return an optional result containing the rendered image of this environment
     */
    default Optional<? extends BufferedImage> render() {
        return Optional.empty();
    }
}
