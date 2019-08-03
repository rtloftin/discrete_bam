var Common = Common || {};

// Displays content in the main window
Common.show = function(content) {
    let div = document.getElementById("interface");

    while(div.firstChild) {
        div.removeChild(div.firstChild);
    }

    div.appendChild(content);
}

// Show an alert overlay div with a 'next' button -- try to do this without requring special divs
Common.alert = function(message, label) {

    // Set alert message and label
    document.getElementById("alert-message").innerHTML = message;
    document.getElementById("alert-button").innerHTML = label;

    // Show the alert div
    document.getElementById("alert-overlay").classList.remove("hidden");

    // Set callback for button
    return new Promise((resolve) => {
        document.getElementById("alert-button").onclick = () => {
            document.getElementById("alert-overlay").classList.add("hidden");
            resolve();
        };
    });
}

// Displays the waiting message overlay -- try to do this without requiring special divs
Common.pause = function(message = "Please Wait") {
    document.getElementById("pause-message").innerHTML = message;
    document.getElementById("pause-overlay").classList.remove("hidden");
}

// Hides the waiting message overlay
Common.unpause = function() {
    document.getElementById("pause-overlay").classList.add("hidden");
};

// Set page title
Common.title = function(title) {
    document.title = title;
};

// Prevent user from leaving page
Common.freeze = function() {
    window.onbeforeunload = (ev) => {
        ev.returnValue = `If you leave this page, you will not be able to
        complete the study without starting over.`;
    };
};

// Redirect user
Common.redirect = function(page) {
    window.onbeforeunload = null;
    location.href = page;
};

// Loads a dictionary of image urls
Common.images = function(dictionary) {
    let promises = [];

    for(const [name, url] of Object.entries(dictionary)) {
        promises.push(new Promise((resolve, reject) => {
            let img = new Image();
            img.onload = () => resolve({"name" : name, "image" : img});
            img.onerror = () => reject("could not load: " + url);
            img.src = url;
        }));
    }

    return Promise.all(promises).then((array) => {
        let images = {};

        for(let entry of array) {
            images[entry.name] = entry.image;
        }

        return images;
    });
};

// Returns a promise that resolves after a fixed amount of time
Common.wait = function(timeout) {
    return new Promise((resolve) => {
        window.setTimeout(resolve, timeout);
    });
};

// Generates a random integer
Common.random = function(size) {
    return  Math.floor(Math.random() * size * 100.0) % size;
};

// Make a randomized copy of an array
Common.shuffle = function(array) {
    let shuffled = array.slice();

    for(let i = shuffled.length - 1; i > 0; --i) {
        let j = Common.random(i + 1);
        let temp = shuffled[i];
        shuffled[i] = shuffled[j];
        shuffled[j] = temp;
    }

    return shuffled;
};

// A class for managing an individual event handler
Common.Handler = class {

    constructor(handler, remove) {
        this.handler = handler;
        this.remove = remove;

        this.enabled = true;
    }

    call(data) {
        if(this.enabled) {
            this.handler(data);
        }

        return this;
    }

    enable(enabled = true) {
        this.enabled = enabled;

        return this;
    }

    disable() {
        this.enabled = false;

        return this;
    }
};

// A super class to add observer
Common.Observable = class {

    constructor() {
        this.handlers = {};
    }

    handler(type, handler) {

        // Add handler array if it doesn't already exist
        if(!(type in this.handlers)) {
            this.handlers[type] = [];
        }

        // Get the position of the new instance in the array
        let index = this.handlers[type].length;

        // Costruct the new handler
        let instance = new Common.Handler(handler, () => {
            this.handlers[type].splice(index, 1);
        });

        // Add the handler to the observer
        this.handlers[type].push(instance);

        // Return the handler
        return instance;
    }

    fire(type, data = {}) {
        if(type in this.handlers) {
            for(let handler of this.handlers[type]) {
                handler.call(data);
            }
        }
    }
};
