Transient Cloud Server
======================

Summary
-------
Ephemeral Cloud Project built by me under the supervision of Matthias Geel, Globis, ETH Zurich

Requirements
------------
1. Java 8
2. Eclipse with Maven Plugin

Setup
-----

1. From eclipse, right click on pom and run a maven install
2. Run the server as a Java application

Notes on using the admin tool (Bundled in hsqldb.jar)
--------------------------------------------------------------------

1. Go to the directory where the library is located
	* If you are on branch Master, this will be somewhere in your M2 folder
	* If you are on branch Demo, this will be in the libraries folder

2. Execute ` java -cp hsqldb.jar org.hsqldb.util.DatabaseManagerSwing `
in your terminal/console.

3.  The connection settings for the demo are:
       * Driver: org.hsqldb.jdbcDriver
       * URL: jdbc:hsqldb:hsql://localhost/TransientCloudServerDb
       * User: SA

4. Master is the branch you should clone. If you would like to see the version I used for demonstration, The 'demo' branch should be cloned

Presentation Slides
-------------------

To view the slides for the demonstration, open up the docs folder and open index.html in your browser.

Troubleshooting
---------------
After running maven install, the Eclipse project may throw Java Build Path Errors. Pointing your JRE and JDK to 8, 1.8 respectively should fix all these issues.

Contact
----------

For bug reports/queries, please email mukherjeerohit93@gmail.com

