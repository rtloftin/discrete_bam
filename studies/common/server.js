/*
 * This class represents a single, global connection to a learning
 * server over a websocket.  It handles the initialization of this
 * connection, and the JSON serialization/deserialization of messages.
 * Connection errors are passed to the client only when the client
 * tries to connect to or communicate with the server.
 */
class Server {

    static connect(url, timeout = 300000) {
        return new Promise((resolve, reject) => {

            // Start timer
            window.setTimeout(() => {
                reject("timeout");
            }, timeout);

            // Construct socket
            let socket = new WebSocket(url);

            // If the connection fails, we will get at least an error or a close event
            socket.onclose = () => reject("socket closed immediately");
            socket.onerror = () => reject("socket encountered an error");

            // Even if we get a response, we need to make sure that the server didn't report an error
            socket.onmessage = (message) => {
                try {
                    let response = JSON.parse(message.data);

                    if("ready" in response) {
                        resolve(new Server(socket));
                    } else if("error" in response) {
                        throw response.error;
                    } else {
                        throw "bad response";
                    }
                } catch(error) {
                    console.error("could not connect to server: " + error);
                    reject(error);
                }
            };
        });
    }

    // Takes in an intialized websocket and attaches message and error handlers
    constructor(socket) {

        // Capture the socket
        this.socket = socket;

        // Initialze query ID
        this.query_id = 0;

        // Build listeners
        this.callbacks = new Map();

        // Attach message handler
        this.socket.onmessage = (message) => this.on_message(message);

        // Attach error and close handlers
        this.socket.onerror = () => this.onclose();
        this.socket.onclose = () => this.onclose();
    }

    // When a message is received
    on_message(message) {
        try {
            let response  = JSON.parse(message.data);

            if("callback" in response && this.callbacks.has(response.callback)) {
                let callback = this.callbacks.get(response.callback);

                if("data" in response) {
                    callback.resolve(response.data);
                } else if("error" in response) {
                    callback.reject(response.error);
                } else {
                    callback.reject("bad response");
                }

                this.callbacks.delete(response.callback);
            } else {
                throw "no callback found";
            }
        } catch(error) {
            console.error("bad message from server: " + error);
        }
    }

    // When the connection dies, this alerts all outstanding listeners
    on_close() {
        for(let callback of this.callbacks) {
            callback.reject("server connection closed");
        }

        this.callbacks.clear();
    }

    // Sends a message and returns a promise that resolves with the response
    query(type, data = {}, timeout = 60000) {

        // Get the query id and increment the current id
        let id = this.query_id++;

        // Construct the query payload
        let query = {
            "type" : type,
            "data" : data,
            "id" : id
        };

        return new Promise((resolve, reject) => {

            // define callback
            this.callbacks.set(id, {
                "resolve" : resolve,
                "reject"  : reject
            });

            // Start timer
            window.setTimeout(() => {
                this.callbacks.delete(id);
                reject("timeout");
            }, timeout);

            // Try to send query
            try {
                this.socket.send(JSON.stringify(query));
            } catch(error) {
                reject(error);
            }
        });
    }

    // Asks the server to record a log message for the client
    log(message) {
        return this.query("log", {"message" : message});
    }

    // Reports a client side error to the server and redirects the client
    error(error) {
        return this.query("error", {"message" : error});
    }

    // Report that the experiment is finished
    complete() {
        return this.query("complete");
    }

    // Close the server
    close() {
        this.on_close();
        this.socket.close();
    }
}
