Transient Cloud Server
======================

Requirements
------------
1. Java 8
2. Eclipse with Maven Plugin

Setup
-----

1. From eclipse, right click on pom and run a maven install
2. Run the server

Inspecting the database using the admin tool (Bundled in hsqldb.jar)
--------------------------------------------------------------------

1. Go to the directory where the library is located
	* If you are on branch Maven, this will be somewhere in your M2 folder
	* If you are on branch Demo, this will be in the libraries folder

2. Execute ` java -cp hsqldb.jar org.hsqldb.util.DatabaseManagerSwing `
in your terminal/console.

3.  The connection settings for the demo are:
       * Driver: org.hsqldb.jdbcDriver
       * URL: jdbc:hsqldb:hsql://localhost/TransientCloudServerDb
       * User: SA

Contact
----------

For bug reports/queries, please email mukherjeerohit93@gmail.com
