var Interfaces = Interfaces || {};

// The outer interface for training agents, built around a specific environment
Interfaces.Training  = class extends Common.Observable {

    constructor(tasks, title = "", instructions = "") {

        // Construct event handler
        super();

        // Create Interface div
        this.root = document.createElement("div");
        this.root.classList.add("interface");

        // Create title div
        this.title = document.createElement("div");
        this.title.classList.add("title");
        this.title.innerHTML = title;
        this.root.appendChild(this.title);

        // Create status div
        this.status = document.createElement("div");
        this.status.classList.add("status");
        this.root.appendChild(this.status);

        // Create instruction div
        this.instructions = document.createElement("div");
        this.instructions.classList.add("instructions");
        this.instructions.innerHTML = instructions;
        this.root.appendChild(this.instructions);

        // Add environment pane in between the two control lists
        this.view = document.createElement("div");
        this.view.classList.add("content");
        this.root.appendChild(this.view);

        // Add task select buttons to task pane
        let task_div = document.createElement("div");
        task_div.classList.add("tasks");
        this.root.appendChild(task_div);

        // Add task label
        let task_label = document.createElement("div");
        task_label.classList.add("label");
        task_label.innerHTML = "Tasks";
        task_div.appendChild(task_label);

        // Add task buttons
        this.current_task = null;
        this.task_buttons = {};
        this.tasks = [];

        for(let task of tasks) {
            this.task_buttons[task.name] = document.createElement("div");
            this.task_buttons[task.name].classList.add("button");
            this.task_buttons[task.name].innerHTML = task.display_name;
            task_div.appendChild(this.task_buttons[task.name]);

            this.task_buttons[task.name].addEventListener("click", () => {
                this.fire("task", task.name);
            });

            this.tasks.push(task.name);
        }

        // Add control div
        let control_div = document.createElement("div");
        control_div.classList.add("controls");
        this.root.appendChild(control_div);

        // Add control buttons
        this.controls = {}

        // Add reset button
        this.controls["reset"] = document.createElement("div");
        this.controls["reset"].classList.add("button");
        this.controls["reset"].innerHTML = "Reset Environment";
        control_div.appendChild(this.controls["reset"]);

        this.controls["reset"].addEventListener("click", () => {
            this.fire("reset");
        });

        // Dividing line
        let hrule = document.createElement("div");
        hrule.classList.add("hrule");
        control_div.appendChild(hrule);

        // Add start demonstration button
        this.controls["start-demonstration"] = document.createElement("div");
        this.controls["start-demonstration"].classList.add("button");
        this.controls["start-demonstration"].innerHTML = "Start Demonstration";
        control_div.appendChild(this.controls["start-demonstration"]);

        this.controls["start-demonstration"].addEventListener("click", () => {
            this.fire("start-demonstration");
        });

        // Add start demonstration button
        this.controls["stop-demonstration"] = document.createElement("div");
        this.controls["stop-demonstration"].classList.add("button", "grayed");
        this.controls["stop-demonstration"].innerHTML = "End Demonstration";
        control_div.appendChild(this.controls["stop-demonstration"]);

        this.controls["stop-demonstration"].addEventListener("click", () => {
            this.fire("stop-demonstration");
        });

        // Dividing line
        control_div.appendChild(hrule.cloneNode(true));

        // Add start execution button
        this.controls["start-execution"] = document.createElement("div");
        this.controls["start-execution"].classList.add("button");
        this.controls["start-execution"].innerHTML = "Start Robot";
        control_div.appendChild(this.controls["start-execution"]);

        this.controls["start-execution"].addEventListener("click", () => {
            this.fire("start-execution");
        });

        // Add stop execution button
        this.controls["stop-execution"] = document.createElement("div");
        this.controls["stop-execution"].classList.add("button", "grayed");
        this.controls["stop-execution"].innerHTML = "Stop Robot";
        control_div.appendChild(this.controls["stop-execution"]);

        this.controls["stop-execution"].addEventListener("click", () => {
            this.fire("stop-execution");
        });

        // Dividing line
        control_div.appendChild(hrule.cloneNode(true));

        // Add finish button
        this.controls["finish"] = document.createElement("div");
        this.controls["finish"].classList.add("button", "grayed");
        this.controls["finish"].innerHTML = "I'm Finished";
        control_div.appendChild(this.controls["finish"]);

        this.controls["finish"].addEventListener("click", () => {
            this.fire("finish");
        });

        // Add feedback overlay
        this.overlay = document.createElement("div");
        this.overlay.classList.add("cover", "hidden");

        // Add feedback detection
        Interfaces.onkey(" ", () => this.fire("feedback", "reward"));
        Interfaces.onkey("Shift", () => this.fire("feedback", "punishment"));
    }

    get_root() {
        return this.root;
    }

    set_view(content) {
        while(this.view.firstChild) {
            this.view.removeChild(this.view.firstChild);
        }

        this.view.appendChild(content);
        this.view.appendChild(this.overlay);
    }

    set_instructions(instructions) {
        this.instructions.innerHTML = instructions;
    }

    set_status(status) {
        this.status.innerHTML = status;
    }

    set_task(task) {
        for(const [name, button] of Object.entries(this.task_buttons)) {
            if(name == task) {
                button.classList.add("selected");
            } else {
                button.classList.remove("selected");
            }
        }

        this.current_task = task;
    }

    get_task() {
        return this.current_task;
    }

    get_tasks() {
        return this.tasks;
    }

    set_idle() {
        this.controls["start-execution"].classList.remove("grayed");
        this.controls["stop-execution"].classList.add("grayed");
        this.controls["start-demonstration"].classList.remove("grayed");
        this.controls["stop-demonstration"].classList.add("grayed");
    }

    set_demonstration() {
        this.controls["start-execution"].classList.remove("grayed");
        this.controls["stop-execution"].classList.add("grayed");
        this.controls["start-demonstration"].classList.add("grayed");
        this.controls["stop-demonstration"].classList.remove("grayed");
    }

    set_execution() {
        this.controls["start-execution"].classList.add("grayed");
        this.controls["stop-execution"].classList.remove("grayed");
        this.controls["start-demonstration"].classList.remove("grayed");
        this.controls["stop-demonstration"].classList.add("grayed");
    }

    enable_control(control) {
        this.controls[control].classList.remove("grayed");
    }

    disable_control(control) {
        this.controls[control].classList.add("grayed");
    }

    request_control(control) {
        return Interfaces.request(this.controls[control]);
    }

    request_task(task) {
        return Interfaces.request(this.task_buttons[task]);
    }

    highlight_reset(highlight) {
        this.controls["reset"].classList.toggle("highlight", highlight);
    }

    flash_red() {
        this.overlay.classList.remove("hidden");
        this.overlay.style.backgroundColor = "rgba(250, 40, 40, 0.5)";

        if("flash_timeout" in this) {
            window.clearTimeout(this.flash_timeout);
        }

        this.flash_timeout = window.setTimeout(() => {
            this.overlay.classList.add("hidden");
        }, 200);
    }

    flash_green() {
        this.overlay.classList.remove("hidden");
        this.overlay.style.backgroundColor = "rgba(4, 250, 40, 0.5)";

        if("flash_timeout" in this) {
            window.clearTimeout(this.flash_timeout);
        }

        this.flash_timeout = window.setTimeout(() => {
            this.overlay.classList.add("hidden");
        }, 200);
    }
};

Interfaces.request = function(button) {
    button.classList.add("highlight");

    return new Promise((resolve) => {
        let handler = () => {
            button.removeEventListener("click", handler);
            button.classList.remove("highlight");
            resolve();
        };

        button.addEventListener("click", handler);
    });
}

// Sets or overides a single key listener
Interfaces.onkey = function(code, callback) {

    // Initialize listener map and global key listeners if they don't exist
    if(!('key_listeners' in Interfaces)) {
        Interfaces.key_listeners = new Map();

        document.addEventListener("keyup", (event) => {
            if(Interfaces.key_listeners.has(event.key)) {
                Interfaces.key_listeners.get(event.key).down = false;
            }
        });

        document.addEventListener("keydown", (event) => {
            if(Interfaces.key_listeners.has(event.key)) {
                let listener = Interfaces.key_listeners.get(event.key);

                if(!listener.down) {
                    listener.callback();
                    listener.down = true;
                }
            }
        });
    }

    // Check if a key listener already exists and remove it
    Interfaces.key_listeners.delete(code);

    // Check if callback defined and add it
    if(typeof callback !== 'undefined') {
        Interfaces.key_listeners.set(code, {
            "down" : false,
            "callback" : callback
        });
    }
}
