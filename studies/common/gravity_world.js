var GravityWorld = GravityWorld || {};

// A test mode for this domain -- we won't implement an exprimental mode
GravityWorld.test = async function(server) {

    Common.title("Test");

    let config = {
        "condition" : {
            "domain"      : "gravity world",
            "environment" : "tutorial",
            "algorithm"   : "Expert"
        },
        "initial" : {
            "state" : {
                "x" : 1,
                "y" : 2,
                "direction" : "down",
                "gravity"   : "EAST"
            },
            "task" : { "name" : "Left"}
        }
    };

    await Session.run(server, config, (initial) => {
        return GravityWorld.Interface.build(initial, "white", "Gravity World Test");
    });

    Common.pause("Test Complete");
};

// The interface class, used to render the gravity world and get user inputs
GravityWorld.Interface = class extends Interfaces.Training {

    static async build(initial, color, name) {

        // Load robot sprite
        let sprites = await Common.images({
            "green"  : "/studies/common/images/gravity_world/robot_green.png",
            "red"    : "/studies/common/images/gravity_world/robot_red.png",
            "yellow" : "/studies/common/images/gravity_world/robot_yellow.png",
            "white"  : "/studies/common/images/gravity_world/robot_white.png",
            "arrow"  : "/studies/common/images/gravity_world/arrow_up.png"
        });

        // Build grid world
        return new GravityWorld.Interface(initial, sprites, color, name);
    }

    constructor(initial, sprites, color, name) {

        let instructions = "Use the arrow keys to control the robot.  " +
        "To start a demonstration of the current task, click the " +
        "'Start Demonstration' button, to have the robot try the task " +
        "itself, click 'Start Robot'.  To reset the robot's position, click " +
        "'Reset Environment'.  The name of the task you are currently " +
        "teaching is shown on the left side of the screen, and you can " +
        "select a different task by clicking on that task's name.  When " +
        "you are finished teaching, or don't believe the robot can learn " +
        "anything more, click 'I'm Finished'."

        super(initial.tasks, name, instructions);

        // Capture sprites
        this.sprite = sprites[color];
        this.arrow = sprites["arrow"];

        // Color mapping
        this.colors = {
            "GREEN" : "Green",
            "BLUE" : "Blue",
            "ORANGE" : "Orange",
            "PURPLE" : "Magenta"
        };

        // Gravity mapping
        this.gravity = {
            "NORTH" : 0.0,
            "SOUTH" : Math.PI,
            "EAST"  : 0.5 * Math.PI,
            "WEST"  : -0.5 * Math.PI
        };

        // Direction mapping
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
        this.canvas.width = scale * this.layout.width;
        this.canvas.height = scale * this.layout.height;

        // Draw background
        this.context.fillStyle = "RoyalBlue";
        this.context.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw gravity indicator
        if(state.gravity in this.gravity) {
            this.context.save();
            this.context.translate(this.canvas.width / 2, this.canvas.height / 2);
            this.context.rotate(this.gravity[state.gravity]);
            this.context.drawImage(this.arrow, -this.arrow.width / 2, -this.arrow.height / 2);
            this.context.restore();
        }

        // Draw special cells

        for(var row = 0; row < this.layout.height; ++row) {
            for(var column = 0; column < this.layout.width; ++column) {
                let color = this.layout.colors[row][column];

                if("CLEAR" != color) {
                    this.context.fillStyle = this.colors[color];
                    let gravity = this.layout.gravity[color];

                    this.context.save();
                    this.context.translate(column * scale + half, row * scale + half);
                    this.context.rotate(this.gravity[gravity]);
                    this.context.beginPath();
                    this.context.moveTo(-half, half);
                    this.context.lineTo(0, -half);
                    this.context.lineTo(half, half);
                    this.context.closePath();
                    this.context.fill();
                    this.context.restore();
                }
            }
        }

        // Draw goal
        this.context.save();
        this.context.fillStyle = "Orange";
        this.context.translate(this.task.x * scale + half, this.task.y * scale + half);
        this.context.beginPath();
        this.context.arc(0, 0, 0.8* half, 0, 2 * Math.PI);
        this.context.closePath();
        this.context.fill();
        this.context.restore();

        this.set_task(this.task.name);

        // Draw robot
        this.context.save();
        this.context.translate(state.x * scale + half, state.y * scale + half);
        this.context.rotate(this.angles[state.direction]);
        this.context.drawImage(this.sprite, -this.sprite.width / 2, -this.sprite.height / 2);
        this.context.restore();

        // Fire state event -- used mainly for tutorials
        this.fire("state", state);
    }

    get_depth() {
        return this.depth;
    }

    no_op() {
        return "stay";
    }
};
