var Tutorial = Tutorial || {};

Tutorial.controller = function(server, environment) {
    return new Tutorial.Controller(server, environment);
};

Tutorial.Controller = class {

    constructor(server, environment) {

        // Capture server
        this.server = server;

        // Capture environment
        this.environment = environment;

        // Initialize error handler
        this.on_error = () => {};

        // Set agent active to false
        this.agent = false;

        // Define action handler
        this.on_action = this.environment.handler("action", (type) => {
            this.on_action.disable();

            this.server.query("take-action", {
                "type" : type
            }).then((response) => {
                this.environment.update(response);
                this.on_action.enable();
            }).catch((error) => this.on_error(error));
        }).disable();
    }

    enable_user() {
        this.on_action.enable();
    }

    disable_user() {
        this.on_action.disable();
    }

    async start_agent() {
        this.agent = true;

        while(this.agent) {
            await this.server.query("get-action").then((response) => {
                this.environment.update(response);
            }).catch((error) => {
                this.on_error(error);
            });

            await Common.wait(600);
        }
    }

    stop_agent() {
        this.agent = false;
    }

    get_feedback(type) {
        return new Promise((resolve, reject) => {
            let handler = this.environment.handler("feedback", (feedback) => {
                if(feedback == type) {
                    handler.remove();
                    resolve();
                }
            });

            this.on_error = reject;
        });
    }

    get_action() {
        return new Promise((resolve, reject) => {
            let handler = this.environment.handler("action", (type) => {
                handler.remove();
                resolve();
            });

            this.on_error = reject;
        });
    }

    get_state(predicate) {
        return new Promise((resolve, reject) => {
            let handler = this.environment.handler("state", (state) => {
                if(predicate(state)) {
                    handler.remove();
                    resolve();
                }
            });

            this.on_error = reject;
        });
    }

    set_state(state) {
        return this.server.query("set-state", state).then((response) => {
                this.environment.update(response);
            });
    }

    set_task(task) {
        return this.server.query("task", task).then((response) => {
                this.environment.update(response);
            });
    }
};
