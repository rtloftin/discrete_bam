<?php

    //Check for consent
    session_start();
    if(!isset($_SESSION["consent"]) || !$_SESSION["consent"]) {
        header("Location: noconsent.html");
        exit(0);
    }
?>

<!DOCTYPE html>

<html>
    <head>
        <meta charset="UTF-8" />
        <link rel="icon" type="image/png" href="/studies/common/images/favicon.png" />
        <link rel="stylesheet" type="text/css" href="/studies/common/stylesheet.css" />

        <script src="/studies/common/common.js"></script>
        <script src="/studies/common/server.js"></script>
        <script src="/studies/common/session.js"></script>
        <script src="/studies/common/tutorial.js"></script>
        <script src="/studies/common/interfaces.js"></script>
        <script src="/studies/common/grid_world.js"></script>
        <script src="/studies/common/farm_world.js"></script>
        <script type="text/javascript">

            async function main() {
                experiment();
                // test();
            }

            async function experiment() {
                try {

                    // Set title
                    Common.title("Robot Learning");

                    // Lock the page
                    Common.freeze();

                    // Connect to the server
                    let server = await Server.connect("ws://" + location.host + "/studies/web/server");

                    // Select a domain and run the experiment
                    if(Math.random() >= 0.5) {
                        await GridWorld.run(server);
                    } else {
                        await FarmWorld.run(server);
                    }

                    // Close the user's connection
                    server.complete();

                    // Take the user to the 'thank you' page
                    Common.redirect("thank_you.php");

                } catch(error) {
                    if("busy" == error) {
                        Common.redirect("busy.html");
                    } else {
                        console.log(error);
                        Common.redirect("error.html");
                    }
                }
            }

            async function test() {

                // Set title
                Common.title("Robot Learning");

                // Connect to the server
                let server = await Server.connect("ws://" + location.host + "/studies/web/server");

                // Select a domain and run the experiment
                if(Math.random() >= 0.5) {
                    await GridWorld.run(server);
                } else {
                    await FarmWorld.run(server);
                }

                // Close the user's connection
                server.complete();

                // Take the user to the 'thank you' page
                Common.redirect("thank_you.php");
            }

        </script>
    </head>
    <body onload="main();">

        <!--Holds the main experimental interface-->
        <div id="interface" class="center"></div>

        <!--An overlay div used to display alert messages-->
        <div id="alert-overlay" class="overlay shadow hidden">
            <div class="message">
                <div id="alert-message" style="max-width: 600px;"></div>
                <div id="alert-button" class="button highlight" style="float: right"></div>
            </div>
        </div>

        <!--An overlay div to display while the server is working-->
        <div id="pause-overlay" class="overlay shadow hidden">
            <div id="pause-message" class="flashing">Waiting</div>
        </div>
    </body>
</html>
