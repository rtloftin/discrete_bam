var FarmWorld = FarmWorld || {};

FarmWorld.two_fields = [
    {domain: "farm world", environment : "two-fields", algorithm : "BAM"},
    {domain: "farm world", environment : "two-fields", algorithm : "Model-Based"},
    {domain: "farm world", environment : "two-fields", algorithm : "Cloning"}
];

FarmWorld.six_fields = [
    {domain: "farm world", environment : "three-fields", algorithm : "BAM"},
    {domain: "farm world", environment : "three-fields", algorithm : "Model-Based"},
    {domain: "farm world", environment : "three-fields", algorithm : "Cloning"}
];

// The main script for this domain
FarmWorld.run = async function(server) {

        // Run tutorial
        await FarmWorld.tutorial(server);

        // Randomize colors
        let colors = Common.shuffle(["green","cyan","blue"]);

        // Run through the 'two-rooms' conditions
        let conditions = Common.shuffle(FarmWorld.two_fields);

        for(let i = 0; i < conditions.length; ++i) {
            await Common.alert(`You will now teach a completely new robot
            that does not know which of the fields need to be worked on, and
            does not know which machines it needs to use on these fields.
            Click "Okay" to continue.`, "Okay");

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
                return FarmWorld.Interface.build(initial, color, name);
            });
        }

        // Run through the 'doors' conditions
        conditions = Common.shuffle(FarmWorld.six_fields);

        for(let i = 0; i < conditions.length; ++i) {
            await Common.alert(`You will now teach a completely new robot
            that does not know which of the fields need to be worked on, and
            does not know which machines it needs to use on these fields.
            Click "Okay" to continue.`, "Okay");

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
                return FarmWorld.Interface.build(initial, color, name);
            });
        }
};

// The tutorial script for this domain
FarmWorld.tutorial = async function(server) {

    // Show introduction message
    await Common.alert(`In this experiment you will teach a series of
    robots how to use three different machines to grow crops.  The three
    machines are a plow, a sprinkler and a harvester.  The robots need to learn
    to plow the fields before the crops are planted, water the crops when they
    start to grow, and harvest them when they are ready.  Your job is to teach
    the robots which fields they needs to work on, and which machine to use for
    each field.  Click NEXT to move on to the tutorial.`, "Next");

    // Set up tutorial session and interface
    Common.pause("Please wait: Setting things up");

    let config = {
        "condition" : {
            "domain"      : "farm world",
            "environment" : "tutorial",
            "algorithm"   : "Expert"
        },
        "initial" : {
            "state" : {
                "x" : 6,
                "y" : 5,
                "machine" : "NONE"
            },
            "task" : { "name" : "Soil"}
        },
        "client" : { "tutorial" : true}
    };

    let initial = await server.query("start-session", config, 60000);
    let environment = await FarmWorld.Interface.build(initial, "white", "Tutorial");
    let controller = Tutorial.controller(server, environment);

    let match = (x_min, x_max, y_min, y_max) => {
        return (state) => {
            return (x_min <= state.x && x_max >= state.x &&
                y_min <= state.y && y_max >= state.y);
        };
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
    environment.set_instructions(`Now use the robot to pick up the plow
    (bottom left) and take it to the field that is highlighted with an
    orange border.  When you reach the correct field, the border will
    turn green.  Note that you won't be able to reach the field with the
    other two machines.`);

    await controller.get_state(match(1, 2, 1, 2));
    controller.disable_user();

    // Stopping a demonstration
    environment.set_instructions(`Now that you have shown the robot how to
    plow the field, you can end the current demonstration by clicking the
    END DEMONSTRATION button.`);

    await environment.request_control("stop-demonstration");
    environment.set_idle();
    environment.set_status("");

    // Learning update
    await Common.alert(`After watching a demonstration, the robot may take a
    moment to think about what it has seen, and update its behavior.
    Click NEXT to continue.`, "Next");

    // Resetting the state
    environment.set_instructions(`After showing a robot how to perform a task,
    you need to check that it can actually perform that task on its own.  Since
    the robot is already in the field with the plow, you first need to reset
    its position and take away its plow.  To do this, click the RESET
    ENVIRONMENT button.`);

    await environment.request_control("reset");
    await controller.set_state({
            "x" : 6,
            "y" : 5,
            "machine" : "NONE"
        });

    // Starting the robot
    environment.set_instructions(`You can now check whether the robot can
    plow the field on its own.  Tell the robot to start moving by clicking the
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
    provide positive feedback.  You can also give negative feedback, using the
    SHIFT key.  Try giving some negative feedback now, and notice how the
    screen flashes red.`);

    await controller.get_feedback("punishment");
    environment.flash_red();

    controller.start_agent();

    // Stopping the robot
    environment.set_instructions(`Now watch the robot to make sure it performs
    the task correctly.  Once the robot completes the task, you can
    stop it by clicking the STOP ROBOT button.`);

    await controller.get_state(match(1, 2, 1, 2));

    await environment.request_control("stop-execution");
    controller.stop_agent();
    environment.set_idle();
    environment.set_status("");

    // Learning update
    await Common.alert(`Great Work!  You have just taught the robot how to plow
    the first field.  After you stop it, the robot may again pause to
    update its behavior.  You will now need to teach the robot to handle two
    other fields.  Click NEXT to continue.`, "Next");

    // Changing the task
    environment.set_instructions(`On the left side of the screen there are
    three buttons representing the three fields that the robot
    needs to learn how to handle.  The robot is currently learning to plow
    one of the unplanted fields marked "Soil", but you also want to teach
    it to water one of the fields where plants are just starting to grow,
    marked "Grass".  To begin teaching the robot to water this field, select
    this task by clicking the GRASS button.`);

    await environment.request_task("Grass");
    await controller.set_task({
        "name" : "Grass"
    });

    // Discuss switching back and forth between tasks
    environment.set_instructions(`As you can see, one of the fields with small
    plants has now been highlighted.  You can switch between the different
    fields as many times as you like, just remember which one is selected when
    you give a new demonstration.  Click START DEMONSTRATION to continue.`);

    await environment.request_control("start-demonstration");

    // The grass demonstration
    environment.set_instructions(`You now have control of the robot.  Show it
    how to water the highlighted field using the sprinkler (bottom middle).`);

    environment.set_demonstration();
    environment.set_status("Demonstration");

    controller.enable_user();
    await controller.get_state(match(5, 6, 1, 2));

    // Stopping a demonstration
    environment.set_instructions(`Now that you have shown the robot how to
    water the grass, you can end the current demonstration by clicking the
    END DEMONSTRATION button.`);

    await environment.request_control("stop-demonstration");
    environment.set_idle();
    environment.set_status("");

    // Reseting the environment
    environment.set_instructions(`Now reset the environment and test
    whether the robot can complete the task on its own.`);

    await environment.request_control("reset");
    await controller.set_state({
        "x" : 6,
        "y" : 5,
        "machine" : "NONE"
    });

    // Testing the robot
    environment.set_instructions(`You can now test whether the robot has
    learned to water the highlighted grass field on its own.`);

    await environment.request_control("start-execution");
    environment.set_execution();
    environment.set_status("Testing");
    controller.disable_user();

    controller.start_agent();
    await controller.get_state(match(5, 6, 1, 2));

    // Stopping the robot
    environment.set_instructions(`Now that the robot has watered the field,
    you can get it to stop by clicking STOP ROBOT.`);

    await environment.request_control("stop-execution");
    controller.stop_agent();
    environment.set_idle();
    environment.set_status("");

    // Choosing the crops task
    environment.set_instructions(`Great job!  Now teach the robot to harvest
    the crops in the field marked "Crops".  Before you start teaching,
    first select this task by clicking the CROPS button on the left.`);

    await environment.request_task("Crops");
    await controller.set_task({
        "name" : "Crops"
    });

    // The crops demonstration
    environment.set_instructions(`Now start a new demonstrations by clicking
    START DEMONSTRATION, and show the robot how to harvest the crops using the
    harvester (bottom right).`);

    await environment.request_control("start-demonstration");
    environment.set_demonstration();
    environment.set_status("Demonstration");

    controller.enable_user();
    await controller.get_state(match(9, 10, 1, 2));

    // Stopping a demonstration
    environment.set_instructions(`Now that you have shown the robot how
    to harvest the crops, you can end the current demonstration by
    clicking the END DEMONSTRATION button.`);

    await environment.request_control("stop-demonstration");
    environment.set_idle();
    environment.set_status("");

    environment.enable_control("finish");

    // Reseting the environment
    environment.set_instructions(`Now reset the environment and see whether
    the robot can harvest the crops on its own.`);

    await environment.request_control("reset");
    await controller.set_state({
        "x" : 6,
        "y" : 5,
        "machine" : "NONE"
    });

    // Testing the robot
    environment.set_instructions(`You can now test whether the robot has
    learned to harvest the crops on its own.`);

    await environment.request_control("start-execution");
    environment.set_execution();
    environment.set_status("Testing");
    controller.disable_user();

    controller.start_agent();
    await controller.get_state(match(9, 10, 1, 2));

    // Stopping the robot
    environment.set_instructions(`Now that the robot has harvested the crops,
    you can stop it by clicking STOP ROBOT.`);

    await environment.request_control("stop-execution");
    environment.set_idle();
    environment.set_status("");
    controller.stop_agent();

    // Finishing the session
    environment.set_instructions(`Congratulations! You have successfully
    taught the robot to handle all three target fields.  Remember that you can
    provide as many demonstrations of each task as you like, and ask the
    robot to perform each task as many times as you like.
    Click the I'M FINISHED button to continue.`);

    await environment.request_control("finish");

    // Ready for the real thing
    await Common.alert(`You are now ready to teach the real robots.
    There will be six different robots, and you will need to teach each of
    them to handle each of the fields listed on the left side of the
    screen.  You will need to demonstrate each of the tasks at least
    once before the I'M FINISHED button will be enabled.  Only click
    I'M FINISHED when the current robot has learned all of the tasks, or
    when you believe that it cannot learn anything more than it has.
    Click NEXT to move on to the first robot.`, "Next");

    // End session
    await server.query("end-session", {}, 60000);
};

// A test mode for this domain
FarmWorld.test = async function(server) {

    Common.title("Test");

    let config = {
        "condition" : {
            "domain"      : "farm world",
            "environment" : "tutorial",
            "algorithm"   : "Expert"
        },
        "initial" : {
            "state" : {
                "x" : 6,
                "y" : 4,
                "machine" : "NONE"
            },
            "task" : { "name" : "Soil"}
        }
    };

    await Session.run(server, config, (initial) => {
        return FarmWorld.Interface.build(initial, "white", "Farm World Test");
    });

    Common.pause("Test Complete");
};

// The interface class, used to render the gravity world and get user inputs
FarmWorld.Interface = class extends Interfaces.Training {

    static async build(initial, color, name) {

        // Load robot sprite
        let drones = await Common.images({
            "green" : "/studies/common/images/farm_world/drone_green.png",
            "cyan"  : "/studies/common/images/farm_world/drone_cyan.png",
            "blue"  : "/studies/common/images/farm_world/drone_blue.png",
            "white" : "/studies/common/images/farm_world/drone_white.png"
        });

        // Load terrain and machine sprites
        let sprites = await Common.images({
            "soil"   : "/studies/common/images/farm_world/soil.png",
            "grass"  : "/studies/common/images/farm_world/grass.png",
            "crops"  : "/studies/common/images/farm_world/crops.png",
            "plow"   : "/studies/common/images/farm_world/plow.png",
            "sprinkler" : "/studies/common/images/farm_world/sprinkler.png",
            "harvester" : "/studies/common/images/farm_world/harvester.png"
        });

        // Build grid world
        return new FarmWorld.Interface(initial, drones[color], sprites, name);
    }

    constructor(initial, drone, sprites, name) {

        let instructions = `
        PLOW <img src="/studies/common/images/farm_world/plow.png" style="vertical-align: middle;" width="40" height="40" /> &rarr;
        <img src="/studies/common/images/farm_world/soil.png" style="vertical-align: middle;" width="30" height="30" />,
        SPRINKLER <img src="/studies/common/images/farm_world/sprinkler.png"style="vertical-align: middle;"  width="40" height="40" /> &rarr;
        <img src="/studies/common/images/farm_world/grass.png" style="vertical-align: middle;" width="30" height="30" />,
        HARVESTER <img src="/studies/common/images/farm_world/harvester.png" style="vertical-align: middle;" width="40" height="40" /> &rarr;
        <img src="/studies/common/images/farm_world/crops.png" style="vertical-align: middle;" width="30" height="30" /><br />
        > ARROW Keys - Move Robot<br />
        > SPACE Bar - Positive Feedback, SHIFT Key - Negative Feedback<br />
        > START DEMONSTRATION Button - Robot will learn from your actions<br />
        > START ROBOT Button - Robot will try to complete the task itself<br />
        > Select different tasks (different fields) using the buttons on the left<br />
        > I'M FINISHED will be enabled after you demonstrate each task
        `;

        // Initialize the outer interface
        super(initial.tasks, name, instructions);

        // Capture drone sprite
        this.drone = drone;

        // Machine mapping
        this.machines = {
            "PLOW" : sprites.plow,
            "SPRINKLER" : sprites.sprinkler,
            "HARVESTER" : sprites.harvester
        };

        // Gravity mapping
        this.terrain = {
            "SOIL" : sprites.soil,
            "GRASS"  : sprites.grass,
            "CROPS"  : sprites.crops
        };

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
        this.context.fillStyle = "Peru";
        this.context.fillRect(0, 0, width, height);

        // Draw terrain
        for(var row = 0; row < this.layout.height; ++row) {
            for(var column = 0; column < this.layout.width; ++column) {
                if("DIRT" != this.layout.map[row][column]) {
                    let terrain = this.terrain[this.layout.map[row][column]];

                    this.context.save();
                    this.context.translate(column * scale + half, row * scale + half);
                    this.context.drawImage(terrain, -terrain.width / 2, -terrain.height / 2);
                    this.context.restore();
                }
            }
        }

        // Draw machines
        for(var row = 0; row < this.layout.height; ++row) {
            for(var column = 0; column < this.layout.width; ++column) {
                if("NONE" != this.layout.machines[row][column]) {
                    let machine = this.machines[this.layout.machines[row][column]];

                    this.context.save();
                    this.context.translate(column * scale + half, row * scale + half);
                    this.context.drawImage(machine, -machine.width / 2, -machine.height / 2);
                    this.context.restore();
                }
            }
        }

        // Draw goal
        this.set_task(this.task.name);

        if(state.x >= this.task.x && state.y >= this.task.y
            && state.x < (this.task.x + this.task.width)
            && state.y < (this.task.y + this.task.height)) {

            this.context.strokeStyle = "LawnGreen";
            this.is_goal = true;
        } else {
            this.context.strokeStyle = "OrangeRed";
            this.is_goal = false;
        }

        this.context.lineWidth = 10;
        this.context.strokeRect(this.task.x * scale, this.task.y * scale,
            this.task.width * scale, this.task.height * scale);

        // Draw drone
        this.context.save();
        this.context.translate(state.x * scale + half, state.y * scale + half);
        this.context.drawImage(this.drone, -this.drone.width / 2, -this.drone.height / 2);
        this.context.restore();

        // Draw machine
        if("NONE" != state.machine) {
            let machine = this.machines[state.machine];

            this.context.save();
            this.context.translate(state.x * scale + half, state.y * scale + half);
            this.context.drawImage(machine, -machine.width / 2, -machine.height / 2);
            this.context.restore();
        }

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
