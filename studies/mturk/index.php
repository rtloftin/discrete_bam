<?php
  /***************************************************
  * Participants won't normally access this page, but
  * Having this here ensures that a user visiting
  * the directory in which the experiment is stored
  * will always be directed to the consent page.
  ****************************************************/

   header("Location: consent.php");
?>
