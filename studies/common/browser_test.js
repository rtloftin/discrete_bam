var BrowserTests = BrowserTests || {};

BrowserTests.pass = function() {
    try {

        // Test websocket support
        if(!("WebSocket" in window)) {
            throw "websockets not supported";
        }

        // Test async function support
        let async_test = async () => {
            await new Promise.resolve();
        };

        // Test class syntax and inheritance support
        let outer_class = class {
            constructor(value) {
                this.value = value;
            }
        };

        let inner_class = class extends outer_class {
            constructor(value) {
                super(value);
            }

            get_value() {
                return this.value;
            }
        }

        let instance = new outer_class("test value");

        // Test Object.entries support
        let obj = {
            "thing_one" : "Thing 1",
            "thing_two" : "Thing 2"
        };

        for(const [name, value] of Object.entries(obj)) {
            let okay = (obj[name] == value);
        }

        // Test CSS grid and flexbox support
        if(!CSS.supports("display", "flex")) {
            throw "flex-box not supported";
        }

        if(!CSS.supports("display", "grid")) {
            throw "grid layout not supported";
        }

        // Test multiline strings
        var string = `
            This is a multiline string
        `;

        // Tests complete
        return true;
    } catch(error) {
        console.log(error);
        return false
    }
};
