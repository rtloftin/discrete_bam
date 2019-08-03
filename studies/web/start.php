<?php

    //Check for consent form agreement
    if(!isset($_POST["consent"]) || $_POST["consent"] != "yes") {
        header("Location: noconsent.html");
        exit(0);
    }

    // Start session
    session_start();

    // Set session consent flag
    $_SESSION["consent"] = TRUE;
?>

<!--The final page the user will see, right now we won't allow for replays-->
<!DOCTYPE html>

<html>
    <head>
        <meta charset="UTF-8">
        <link rel="icon" type="image/png" href="/studies/common/images/favicon.png" />
        <link rel="stylesheet" type="text/css" href="/studies/common/stylesheet.css" />
        <title>Web Browsers</title>

        <script src="/studies/common/browser_test.js"></script>
        <script type="text/javascript">
            function run_tests() {
                if(BrowserTests.pass()) {
                    document.getElementById("good").classList.remove("hidden");
                } else {
                    document.getElementById("bad").classList.remove("hidden");
                }
            }
        </script>
    </head>
    <body onload="run_tests();">
        <div id="bad" class="message hidden" style="text-align: center;">
            <b>
                Unfortunately, your browser does not support all the features needed<br />
                for this study to function.  If you would like to participate in<br />
                this study, you will need to upgrade your browser to the latest version<br />
                or switch to another browser.<br /><br />

                Please note that this study is not compatible with any version of<br />
                Internet Explorer or the Opera Browser.
            </b>
        </div>
        <div id="good" class="message hidden" style="text-align: center;">
            <b>
                Please be aware that this study requires a keyboard, and so cannot be<br />
                completed using a mobile device or tablet.<br /><br />

                You may not be able to complete this study using Internet Explorer<br />
                or the Opera browser.<br /><br />

                <div class="button" onclick="location.href='experiment.php';">I understand, take me to the study</div>
            </b>
        </div>
    </body>
</html>
