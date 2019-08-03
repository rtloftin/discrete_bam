var GridWorld = GridWorld || {};

GridWorld.two_rooms = [
    {domain: "grid world", environment : "two-rooms", algorithm : "BAM"},
    {domain: "grid world", environment : "two-rooms", algorithm : "Model-Based"},
    {domain: "grid world", environment : "two-rooms", algorithm : "Cloning"}
];

GridWorld.doors = [
    {domain: "grid world", environment : "doors", algorithm : "BAM"},
    {domain: "grid world", environment : "doors", algorithm : "Model-Based"},
    {domain: "grid world", environment : "doors", algorithm : "Cloning"}
];

// The main script for this domain
GridWorld.run = async function(server) {

    // Run tutorial
    await GridWorld.tutorial(server);

    // Randomize colors
    let colors = Common.shuffle(["green","red","yellow"]);

    // Run through the 'two-rooms' conditions
    let conditions = Common.shuffle(GridWorld.two_rooms);

    for(let i = 0; i < conditions.length; ++i) {
        await Common.alert(`You will now teach a completely new robot
        that knows nothing about the current building or the goal
        locations inside this building.  Click "Okay" to continue.`, "Okay");

        let color = colors[i];
        let name = "Robot " + (i + 1) + " of " +
            conditions.length + " - Environment 1 of 2";

        let config = {
            "condition" : conditions[i],
            "client" : {
                "sprite" : color,
                "environment" : 0,
                "algorithm" : i
            }
        };

        await Session.run(server, config, (initial) => {
            return GridWorld.Interface.build(initial, color, name);
        });
    }

    // Run through the 'doors' conditions
    conditions = Common.shuffle(GridWorld.doors);

    for(let i = 0; i < conditions.length; ++i) {
        await Common.alert(`You will now teach a completely new robot
        that knows nothing about the current building or the goal
        locations inside this building.  Click "Okay" to continue.`, "Okay");

        let color = colors[i];
        let name = "Robot " + (i + 1) + " of " +
            conditions.length + " - Environment 2 of 2";

        let config = {
            "condition" : conditions[i],
            "client" : {
                "sprite" : color,
                "environment" : 1,
                "algorithm" : i
            }
        };

        await Session.run(server, config, (initial) => {
            return GridWorld.Interface.build(initial, color, name);
        });
    }
};

// The tutorial script for this domain
GridWorld.tutorial = async function(server) {

    // Show introduction message
    await Common.alert(`In this experiment you will teach a series of
    robots to navigate to different locations in a building.  To teach the
    robots, you will need to take control of them and show them how to get to
    each goal location.  After giving a demonstration, you can ask a robot to
    try moving to the goal on its own.  The robots need to be able to reach each
    of the goals from any part of the building, so keep training and testing
    until you are confident that a robot knows how to reach each location.
    Click NEXT to move on to the tutorial.`, "Next");

    /*
     * Set up the tutorial.
     *
     * NOTE: We show the wait message before
     * the tutorial because the tutorial will
     * be the only time we have to load the
     * sprites for the environment.
     */
    Common.pause("Please wait: Setting things up");

    let config = {
        "condition" : {
            "domain"      : "grid world",
            "environment" : "tutorial",
            "algorithm"   : "Expert"
        },
        "initial" : {
            "state" : {
                "x" : 5,
                "y" : 9,
                "direction" : "up"
            },
            "task" : { "name" : "Top"}
        },
        "client" : { "tutorial" : true }
    };

    let initial = await server.query("start-session", config, 60000);
    let environment = await GridWorld.Interface.build(initial, "white", "Tutorial");
    let controller = Tutorial.controller(server, environment);

    let match = (x, y) => {
        return (state) => (x == state.x && y == state.y);
    };

    // Show the environment
    Common.show(environment.get_root());
    Common.unpause();

    // Moving the robot
    environment.set_instructions(`You can show the robot what to do by
    controlling it yourself.  To start, practice moving the robot around
    using the ARROW keys.  You can move the robot up, down, left and right.`);

    controller.enable_user();
    await controller.get_action();

    // Starting a demonstration
    environment.set_instructions(`Right now you aren't teaching the robot
    anything.  The robot only learns from your actions during a demonstration.
    To begin a new demonstration, click the START DEMONSTRATION button.`)

    await environment.request_control("start-demonstration");
    environment.set_demonstration();
    environment.set_status("Demonstration");

    // Controlling the robot
    environment.set_instructions(`Now use the ARROW keys to show the robot how
    to reach the goal location marked by the orange circle.  When the robot
    reaches the goal, the edge of the circle will turn green.`);

    controller.enable_user();

    await controller.get_state(match(5, 0));
    controller.disable_user();

    // Stopping a demonstration
    environment.set_instructions(`Now that you have shown the robot how to
    reach the goal, you can end the current demonstration by clicking the
    END DEMONSTRATION button.`);

    await environment.request_control("stop-demonstration");
    environment.set_idle();
    environment.set_status("");

    // Learning update
    await Common.alert(`After watching a demonstration, the robot may take a
    moment to think about what it has seen, and update its behavior.
    Click NEXT to continue.`, "Next");

    // Resetting the state
    environment.set_instructions(`You should now check whether the robot has
    learned to perform the current task.  Since the robot is already at the
    goal location, you will first need to move it to a new location that is
    farther away. You can do this by clicking the RESET ENVIRONMENT button.`);

    await environment.request_control("reset");
    await controller.set_state({
            "x" : 4,
            "y" : 5,
            "direction" : "left"
        });

    // Starting the robot
    environment.set_instructions(`You can now check whether the robot can
    reach the goal on its own.  Tell the robot to start moving by clicking the
    START ROBOT button.`);

    await environment.request_control("start-execution");
    environment.set_execution();
    environment.set_status("Testing");

    controller.start_agent();
    await controller.get_state((state) => true);
    controller.stop_agent();

    // Positive feedback
    environment.set_instructions(`The robot will now move on its own and
    attempt to complete the currently selected task.  While the robot
    is moving you can give it positive feedback using the SPACE bar.  Try
    giving it positive feedback now.`);

    await controller.get_feedback("reward");
    environment.flash_green();

    controller.start_agent();
    await controller.get_state((state) => true);
    controller.stop_agent();

    // Negative feedback
    environment.set_instructions(`Notice how the screen flashes green when you
    provide positive feedback.  You can also give negative feedback using the
    SHIFT key.  Try giving some negative feedback now, and notice how the
    screen flashes red.`);

    await controller.get_feedback("punishment");
    environment.flash_red();

    controller.start_agent();

    // Stopping the robot
    environment.set_instructions(`Now watch the robot to make sure it performs the
    task correctly.  Once the robot reaches the goal, you can
    stop it by clicking the STOP ROBOT button.`);

    await controller.get_state(match(5, 0));

    await environment.request_control("stop-execution");
    controller.stop_agent();
    environment.set_idle();
    environment.set_status("");

    // Learning update
    await Common.alert(`Great Work!  You have taught the robot how to reach the
    first goal location.  After you stop it, the robot may again pause to
    update its behavior.  You will now need to teach the robot to reach
    the second goal.  Click NEXT to continue.`, "Next");

    // Changing the task
    environment.set_instructions(`On the left side of the screen there are
    two buttons representing the two specific goal locations that the robot
    needs to learn how to reach.  The robot is currently learning to reach
    the "Top" goal location, but you also want to teach it to reach the
    "Bottom" goal location.  To start teaching it about this goal,
    you will need to select that task by clicking the BOTTOM button.`);

    await environment.request_task("Bottom");
    await controller.set_task({
        "name" : "Bottom"
    });

    // Discuss switching back and forth between tasks
    environment.set_instructions(`As you can see, the goal location has now
    been moved to the bottom of the screen.  You can switch between the
    "Top" and "Bottom" tasks as many times as you like, just remember which
    task is selected when you give a new demonstration.
    Click START DEMONSTRATION to continue.`);

    await environment.request_control("start-demonstration");

    // The demonstration
    environment.set_instructions(`You now have control of the robot.  Now
    use the ARROW keys to show it how to reach the new goal marked by
    the orange circle.`);

    environment.set_demonstration();
    environment.set_status("Demonstration");

    controller.enable_user();
    await controller.get_state(match(5, 10));

    // Stopping a demonstration
    environment.set_instructions(`Now that you have shown the robot how to reach
    the goal, you can end the current demonstration by clicking the
    END DEMONSTRATION button.`);

    await environment.request_control("stop-demonstration");
    environment.set_idle();
    environment.set_status("");

    environment.enable_control("finish");

    // Reseting the environment
    environment.set_instructions(`Now reset the environment and see whether
    the robot can reach the goal on its own.`);

    await environment.request_control("reset");
    await controller.set_state({
            "x" : 4,
            "y" : 5,
            "direction" : "left"
        });

    // Testing the robot
    environment.set_instructions(`Now you can test whether the robot can
    reach the goal on its own.`);

    await environment.request_control("start-execution");
    controller.disable_user();
    environment.set_execution();
    environment.set_status("Testing");

    controller.start_agent();
    await controller.get_state(match(5, 10));

    // Stopping the robot
    environment.set_instructions(`Finally, now that the robot has reached
    the goal, you can stop it by clicking STOP ROBOT.`);

    await environment.request_control("stop-execution");
    controller.stop_agent();
    environment.set_idle();
    environment.set_status("");

    // Finishing the session
    environment.set_instructions(`Congratulations! You have successfully
    taught the robot to reach both goal locations.  Remember that you can
    provide as many demonstrations as you like, and ask the robot to
    perform each task as many times as you like.  Click the
    I'M FINISHED button to continue.`);

    await environment.request_control("finish");

    // Ready for the real thing
    await Common.alert(`You are now ready to teach the real robots. There
    will be six different robots, and you will need to teach each of
    them to reach each of the goal locations listed on the left side of the
    screen.  You will need to demonstrate each of the tasks at least once
    before the I'M FINISHED button will be enabled.  Only click I'M FINISHED
    when you believe the current robot has learned all of the tasks, or when
    you believe that it cannot learn anything more than it has.
    Click NEXT to move on to the first robot.`, "Next");

    // End session
    await server.query("end-session", {}, 60000);
};

// A test mode for this domain
GridWorld.test = async function(server) {

    Common.title("Test");

    let config = {
        "condition" : {
            "domain"      : "grid world",
            "environment" : "tutorial",
            "algorithm"   : "Expert"
        },
        "initial" : {
            "state" : {
                "x" : 5,
                "y" : 9,
                "direction" : "up",
            },
            "task" : { "name" : "Top"}
        }
    };

    await Session.run(server, config, (initial) => {
        return GridWorld.Interface.build(initial, "white", "Grid World Test");
    });

    Common.pause("Test Complete");
};

// The interface class, used to render the grid world and get user inputs
GridWorld.Interface = class extends Interfaces.Training {

    static async build(initial, color, name) {

        // Load robot sprites
        let sprites = await Common.images({
            "green"  : "/studies/common/images/grid_world/robot_green.png",
            "red"    : "/studies/common/images/grid_world/robot_red.png",
            "yellow" : "/studies/common/images/grid_world/robot_yellow.png",
            "white"  : "/studies/common/images/grid_world/robot_white.png"
        });

        // Build grid world
        return new GridWorld.Interface(initial, sprites[color], name);
    }

    constructor(initial, sprite, name) {

        let instructions = `
        > ARROW Keys - Move Robot<br />
        > SPACE Bar - Positive Feedback, SHIFT Key - Negative Feedback<br />
        > START DEMONSTRATION Button - Robot will learn from your actions<br />
        > START ROBOT Button - Robot will try to complete the task itself<br />
        > Select different tasks (different locations) using the buttons on the left<br />
        > I'M FINISHED will be enabled after you demonstrate each task
        `;

        // Initialize the outer interface
        super(initial.tasks, name, instructions);

        // Capture sprite
        this.sprite = sprite;

        // Angle mapping
        this.angles = {
            "up" : 0.0,
            "down" : Math.PI,
            "left" : -0.5 * Math.PI,
            "right" : 0.5 * Math.PI
        }

        // Construct gui elements
        this.canvas = document.createElement("canvas");
        this.context = this.canvas.getContext("2d");

        this.set_view(this.canvas);

        // Register event handlers
        Interfaces.onkey("ArrowUp", () => this.fire("action", "up"));
        Interfaces.onkey("ArrowDown", () => this.fire("action", "down"));
        Interfaces.onkey("ArrowLeft", () => this.fire("action", "left"));
        Interfaces.onkey("ArrowRight", () => this.fire("action", "right"));

        // Get episode length
        this.depth = initial.depth

        // Render initial state
        this.update(initial);
    }

    update(data) {

        // Capture layout and task
        if('layout' in data) {
            this.layout = data.layout;
        }

        if('task' in data) {
            this.task = data.task;
        }

        // Get the current state
        let state = data.state;

        // Compute cell size
        var scale = 60;
        var half = scale / 2;

        // Resize canvas
        let width = scale * this.layout.width
        let height = scale * this.layout.height;

        let factor = Math.min((600 / Math.max(600, width)), (500 / Math.max(500, height)));

        this.canvas.width = factor * width;
        this.canvas.height = factor * height;

        this.context.scale(factor, factor);

        // Draw background
        this.context.fillStyle = "RoyalBlue";
        this.context.fillRect(0, 0, width, height);

        // Draw map
        this.context.fillStyle = "White";

        for(var row = 0; row < this.layout.height; ++row) {
            for(var column = 0; column < this.layout.width; ++column) {
                if(this.layout.map[row][column])
                    this.context.fillRect(column * scale, row * scale, scale, scale);
            }
        }

        // Draw goal
        this.set_task(this.task.name);

        this.context.save();
        this.context.fillStyle = "Orange";
        this.context.strokeStyle = "LawnGreen"
        this.context.lineWidth = 6;
        this.context.translate(this.task.x * scale + half, this.task.y * scale + half);
        this.context.beginPath();
        this.context.arc(0, 0, 0.8* half, 0, 2 * Math.PI);
        this.context.closePath();
        this.context.fill();

        if(state.x == this.task.x && state.y == this.task.y) {
            this.context.stroke();
            this.is_goal = true;
        } else {
            this.is_goal = false;
        }

        this.context.restore();

        // Draw robot
        this.context.save();
        this.context.translate(state.x * scale + half, state.y * scale + half);
        this.context.rotate(this.angles[state.direction]);
        this.context.drawImage(this.sprite, -this.sprite.width / 2, -this.sprite.height / 2);
        this.context.restore();

        // Unhighlight the reset button if it is already highlighted
        this.highlight_reset(false);

        // Fire state event -- used mainly for tutorials
        this.fire("state", state);
    }

    get_depth() {
        return this.depth;
    }

    no_op() {
        return "stay";
    }

    goal() {
        return this.is_goal;
    }
};
