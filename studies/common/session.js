class Session extends Common.Observable {

    static async run(server, config, domain) {

        // Contact server and initialize session
        Common.pause("Please wait: Getting robot ready");
        let initial = await server.query("start-session", config, 180000);

        // Build the environment and interface
        let environment = await domain(initial);

        // Build session object
        let session = new Session(server, environment);

        // Show the interface
        Common.show(environment.get_root());
        Common.unpause();

        // Wait for the session to finish
        await new Promise((resolve, reject) => {
            session.handler("finish", resolve);
            session.handler("error", (message) => reject(message));
        });

        // Disable the interface and do one more learning update
        await session.close();

        // Close session on the server
        Common.pause("Please wait: Putting robot away");
        await server.query("end-session", {}, 180000);
        Common.unpause();
    }

    constructor(server, environment) {

        // Construct event handler
        super();

        // Capture server and environment
        this.server = server;
        this.environment = environment;

        // Keep track of the tasks that have been trained
        this.tasks_shown = new Map();
        this.finish_enabled = false;

        // Attach action handlers
        this.on_finish = this.environment.handler("finish", () => {
            this.fire("finish");
        }).disable();

        this.on_action = this.environment.handler("action", async (type) => {
            if(!this.paused) {

                // Block further user actions until request is complete
                this.on_action.disable();

                // Send the action to the server and get the next state
                await this.server.query("take-action", {
                    "type"    : type,
                    "on-task" : ("demonstration" == this.context)
                }).then((response) => {
                    this.environment.update(response);
                }).catch((error) => this.fire("error", error));

                // Restore user control
                this.on_action.enable();

                // Mark the current task as having been demonstrated
                if(!this.finish_enabled && "demonstration" == this.context) {
                    this.tasks_shown.set(this.environment.get_task(), true);
                }
            }
        });

        this.on_reset = this.environment.handler("reset", async () => {
            if(!this.paused) {

                // Block further resets until request is complete
                this.on_reset.disable();

                // Fire implicit no-op if necessary
                this.no_op();

                // Send the reset request to the server and get the new state
                await this.server.query("reset").then((response) => {
                    this.environment.update(response);
                }).catch((error) => {
                    this.fire("error", error);
                });

                // Allow resets again
                this.on_reset.enable();
            }
        });

        this.on_task = this.environment.handler("task", async (task) => {
            if(!this.paused) {

                // Block further task changes until request is complete
                this.on_task.disable();

                // Fire implicit no-op if necessary
                this.no_op();

                await this.server.query("task", {
                    "name" : task
                }).then((response) => {
                    this.environment.update(response);
                }).catch((error) => this.fire("error", error));

                // Allow task changes again
                this.on_task.enable();
            }
        });

        this.on_feedback = this.environment.handler("feedback", (type) => {
            if(!this.paused && "execution" == this.context) {
                if("reward" == type) {
                    this.environment.flash_green();
                } else if("punishment" == type) {
                    this.environment.flash_red();
                }

                this.server.query("feedback", {
                    "type" : type
                }).catch((error) => this.fire("error", error));
            }
        });

        // Attach mode handlers
        this.environment.handler("start-demonstration", () => {
            this.start_demonstration();
        });

        this.environment.handler("stop-demonstration", () => {
            this.start_idle();
        });

        this.environment.handler("start-execution", () => {
            this.start_execution();
        });

        this.environment.handler("stop-execution", () => {
            this.start_idle();
        });

        // Initialize context
        this.context = "idle";
        this.paused = false;
    }

    async start_idle() {
        if(!this.paused && "idle" != this.context) {

            // Clear previous context
            this.stop_loops();
            this.on_action.disable();

            // Fire implicit no-op if necessary
            this.no_op();

            // Always do learning update
            await this.learn();

            // Set context to idle
            this.context = "idle"
            this.on_action.enable();
            this.environment.set_idle();

            // Display status
            this.environment.set_status("");

            // Highlight reset state if we are at the goal
            if(this.environment.goal()) {
                this.environment.highlight_reset(true);
            }
        }
    }

    async start_demonstration() {
        if(!this.paused && "demonstration" != this.context) {

            // Clear previous mode
            this.stop_loops();
            this.on_action.disable();

            // Do learning update if needed
            if("execution" == this.context) {
                await this.learn();
            }

            this.context = "demonstration";
            this.on_action.enable();
            this.environment.set_demonstration();

            // Display status
            this.environment.set_status("Demonstrating");
        }
    }

    async start_execution() {
        if(!this.paused && "execution" != this.context) {

            // Clear previous mode
            this.stop_loops();
            this.on_action.disable();

            // Do learning update if needed
            if("demonstration" == this.context) {
                await this.learn();
            }

            // Set execution mode
            this.context = "execution";
            this.environment.set_execution();

            // Display status
            this.environment.set_status("Testing");

            // Start execution loop
            let loop = { active : true };
            this.current_loop = loop;

            const max_steps = this.environment.get_depth();
            let step_count = 0;

            while(loop.active /* && max_steps > (step_count++) */) {
                await this.server.query("get-action").then((response) => {
                    this.environment.update(response);
                }).catch((error) => {
                    this.fire("error", error);
                });

                await Common.wait(500);
            }
        }
    }

    async close() {
        this.stop_loops();
        this.on_action.disable();

        if("idle" != this.context) {
            await this.learn();
        }

        this.pause();
    }

    stop_loops() {
        if("current_loop" in this) {
            this.current_loop.active = false;
        }
    }

    pause() {
        this.paused = true;
    }

    unpause() {
        this.paused = false;
    }

    async learn() {

        // Pause the session
        this.pause();
        Common.pause("Please wait: Learning");

        // Set the baseline timer -- so it always appears to learn
        let timer = Common.wait(5000);

        // Start the update on the server
        await this.server.query("update", {}, 180000).catch((error) => {
            this.fire("error", error);
        });

        // Wait for timer
        await timer;

        // Check if all tasks have been demonstrated
        if(!this.finish_enabled) {
            let all_tasks = true;

            for(const name of this.environment.get_tasks()) {
                all_tasks = all_tasks && this.tasks_shown.has(name);
            }

            if(all_tasks) {
                this.environment.enable_control("finish");
                this.on_finish.enable();
                this.finish_enabled = true;
            }
        }

        // Unpause the session
        Common.unpause();
        this.unpause();
    }

    async no_op() {

        // Fire implicit no-op if necessary
        if("demonstration" == this.context) {
            await this.server.query("take-action", {
                "type"    : this.environment.no_op(),
                "on-task" : true
            }).then((response) => {
                this.environment.update(response);
            }).catch((error) => this.fire("error", error));
        }
    }
}
