<?php
  /**
  * This page is currently static, but
  * may contain server side script in
  * the future.  Since this will be the
  * page linked to in recruitment material,
  * we retain the option to add such script
  * without editing the file path. We will
  * prevent users from accesing the study
  * without going through this page.
  **/
?>
<!DOCTYPE html>

<html>
    <head>
        <meta charset="UTF-8">
        <link rel="icon" type="image/png" href="/studies/common/images/favicon.png" />
        <title>Informed Consent Form</title>
    </head>
    <body>

        <div style="font-weight: bold; text-align: center">
            <a href="ciigar.csc.ncsu.edu"><img height="150px" src="/studies/common/images/ciigar_banner.gif" /></a><br /><br />
            North Carolina State University<br />
            INFORMED CONSENT FORM for RESEARCH
        </div>

        <br />
        <br />

        <b>Learning Environment Maps with Tele-operated Robots</b><br /><br />

        Robert Loftin&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Dr. David L. Roberts

        <br /><br />

        <b><u>What are some general things you should know about research studies?</u></b><br />
        You are being asked to take part in a research study.  Your participation in this study is voluntary. You have the right to not be a part of this study, to choose not to participate or to stop participating at any time without penalty.  The purpose of research studies is to gain a better understanding of a certain topic or issue. You are not guaranteed any personal benefits from being in a study. Research studies also may pose risks to those that participate. In this consent form you will find specific details about the research in which you are being asked to participate. If you do not understand something in this form it is your right to ask the researcher for clarification or more information. You are encouraged to print and retain a copy of this consent information for your records. If at any time you have questions about your participation, do not hesitate to contact the researcher(s) named above. <br /><br />

        <b><u>What is the purpose of this study?</u></b><br />
        This study will evaluate the performance of various machine learning algorithms that learn from demonstrations provided by a human teacher.<br /><br />

        <b><u>What will happen if you take part in the study?</u></b><br />
        If you agree to participate in this study, you will be asked to teach a group of virtual robots to perform a series of simple tasks, by completing the tasks yourself using your browser. This study should take between approximately 30 minutes to complete, though you may continue teaching to robots for as long as you like.<br /><br />

        <b><u>Risks</u></b><br />
        You will participate in the study using your web browser and computer, and therefore will be exposed to the typical risks involved with normal computer use (e.g. fatigue, RSI, etc.).<br /><br />

        <b><u>Confidentiality</u></b><br />
        All information in the study records will be anonymous and will be kept confidential to the full extent allowed by law. Data will be stored securely on a secure server that can only be accessed by the principal investigators. No reference will be made in oral or written reports which could link you to the study. You will NOT be asked to provide your name so that no one can match your identity to the answers that you provide.<br /><br />

        <b><u>Compensation</u></b><br />
        You will receive 1.50 US Dollars for completing this study.  You must attempt to teach each of six robots to perform each of the tasks specified to receive compensation.  Your compensation will not depend on how well the robots learn to perform these tasks.  Once you have finished training all of the robots, you will receive a code that you can use to claim your compensation through Mechanical Turk.  You will not receive any compensation if you choose not to complete the entire study.  If however you are unable to reach the end of the study for any reason, please contact the researchers at (email: rtloftin@ncsu.edu) and let them know what happened, and you will receive the full compensation of $1.50 USD.<br /><br />

        <b><u>What if you have questions about this study?</u></b><br />
        If you have questions at any time about the study or the procedures, you may contact the researcher, Robert Loftin (email: rtloftin@ncsu.edu).<br /><br />

        <b><u>What if you have questions about your rights as a research participant?</u></b><br />
        If you feel you have not been treated according to the descriptions in this form, or your rights as a participant in research have been violated during the course of this project, you may contact Deb Paxton, Regulatory Compliance Administrator, Box 7514, NCSU Campus (919/515-4514).<br /><br />

        <b><u>Consent To Participate</u></b><br />
        “I have read and understand the above information.  I have received a copy of this form.  I agree to participate in this study with the understanding that I may choose not to participate or to stop participating at any time without penalty or loss of benefits to which I am otherwise entitled. Additionally, I certify that I am at least eighteen (18) years of age”<br /><br />

        <form action="start.php" method="POST">
            (Check One) <input type="radio" name="consent" value="yes">I Agree</input>  <input type="radio" name="consent" value="no">I Disagree</input><br /><br />
            <input type="submit" value="Submit"></input>
        </form>

    </body>
</html>
