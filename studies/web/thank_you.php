<?php

    // Clear the current session
    session_unset();
?>

<!--The final page the user will see-->
<!DOCTYPE html>

<html>
    <head>
        <meta charset="UTF-8">
        <link rel="icon" type="image/png" href="/studies/common/images/favicon.png" />
        <link rel="stylesheet" type="text/css" href="/studies/common/stylesheet.css" />
        <title>Thank You</title>
    </head>
    <body>
        <div style="text-align: center;"class="message">
            <a href="ciigar.csc.ncsu.edu"><img height="200px" src="/studies/common/images/ciigar_banner.gif" /></a><br /><br />
            Congratulations, you have successfully complete our study.<br /><br />
            Thank you for your participation.<br /><br />
            <?php

                // Check if completion code was provided in query string
                if(isset($_GET['code'])) {
                    echo 'Your unique verification code is:
                        <div style="font-weight: bold; user-select: all;">'.$_GET['code'].'</div>';
                }
            ?>
        </div>
    </body>
</html>
