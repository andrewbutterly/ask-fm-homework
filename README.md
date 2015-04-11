## Project definition

This project is an attempt at the ASK.fm Java homework problem on GITHub
 - https://github.com/ask-fm/homework-java
 
 This code is a pretty simple Spring Boot application that exposes a REST API with the following calls:
 
 - Request to add questions
    - By making a HTTP GET request to /ask/{question text}
    - By making a HTTP POST request to /ask with the question text as the HTTP POST Body content, in a JSON object.
        - With Content-type: "application/json"
        - Sample JSON POST message: {"question":"how are you feeling today?"}
 
 - Request to list accepted questions
    - By making a HTTP GET request to /list
    - By making a HTTP GET request to /list/{countryCode}, where {countryCode} is the ISO_3166-1_alpha-2 two letter code for a country
    
    - List of questions returned as a JSON array of question objects


The Application executes from the command line and uses the embedded web server and database that Spring Boot provides. 

### Requirements
 - Java 1.8
 - Maven (built using 3.2.1, and Spring Boot requires 3.* I think)

### Configuration 
The file application.properties in ./src/main/resources/ can be modified to change the various runtime and database settings for the application. 

The embedded web server will run on port 8080 by default. If you would like to change this, please change the "server.port" value in the application.properties file. 

If you want to change the MAX rate as which questions can be asked for a particular country, chance the value for "general.maxQPerSecondPerCountry" in the application.properties file.

**Persistent store**
The service uses a relational database for storing Questions, etc. By default it will use the embedded HSQL database provided with Spring Boot. So no database configuration is required 
to run the application once it has been compiled with Maven. 

**PLEASE NOTE:**
 - Executing and running this application as is will generate a folder called "ask.tmp" in the root project folder. This (and the other files ask.* files) are the HSQL database.
 - The app has also been tested against MySQL 5.1. If you wish to use it with MySQL, please edit the "spring.datasource.*" parameters in the application.properties file accordingly.
    - You'll have to manually create the user + database to store the tables in, but Spring will create the tables for you. 
 - There are schema-*.sql creation files for HSQL and MySQL only in the ./src/main/resources/ folder. 
 - If you want to use other database types you will have to:
    - Define table creation files for the other database type
    - Change the spring.datasource.* settings in application.properties accordingly
    - Modify the pom.xml dependencies to include drivers for the new database type

### Building / testing the code

This is a standalone Spring Boot application built using Maven, so it can be directly run from the root project folder:
 - mvn spring-boot:run
Or you can package it by running:
 - mvn package
This will run the unit tests (including tests that execute the application and do a few basic actions on the embedded database).
These tests can also be manually re-run using:
 - mvn test


If you wish to test this app as a WAR file, please send me a message and I'll change the pom.xml

**Notes:**

The file "/src/main/resources/data.sql" inserts four simple Blacklist words into the database on startup that can be used to test. 
These are: poo, wee, socialist, socialism.
There is a much larger list in the file /src/test/resources/ab/asktest/lib/testCurseWordsList.txt that could also be inserted. One the of unit tests
uses this file for a stress test of the functionality, but they are not inserted by default into the main application.  

In addition to the two required constraints on asking questions, there is also a check that prevents duplicate questions being posted from the same IP. 
It is turned off by default ("general.allowQuestionReposting=true" in the application.properties file). If set to false, the service will not allow 
the same IP address to post the same question more than once every 30 seconds. It seemed like a sensible thing to have in there.

The unit tests insert various values into the configured database. I think this is fine for the current use case of the application. Extending the tests to 
use profiles/create and destroy mock databases would probably an idea for further development. 

If testing locally, the service will probably submit your localhost/127.0.0.1 IP address to the GEO-IP service. The GEO-IP service will reject these with an
error. To prevent this, the application will just use the default countryCode (set in application.properties) in these cases.   

Allowing a GET to create a question is in violation of REST principles, but it makes casual testing this app much easier :)

### How to test this all works!?!

The easiest way is to run the executable, fire up a web browser and visit:
 http://localhost:8080/ask/Some nice question
and then try: 
 http://localhost:8080/ask/Some nice question about socialism
and then visit:
 http://localhost:8080/list
to see the results. 
If you have a HTTP Headers plugin in your browser, you can see the different HTTP status codes that are returned depending on the success of the call.   

Alternatively: 

 - Send some HTTP GETs and POSTs to the running service using a REST client. 
    - There is a nice one for Chrome here
       - https://chrome.google.com/webstore/detail/advanced-rest-client/hgmloofddffdnphfgcellkdfbfbjeloo
    - Sample JSON POST format: {"question":"how are you feeling today?"}

 - Look at the unit tests in ./src/test/java/ab/asktest/controller/* and execute those as standalone applications. 
    - You will get a better view of the internals this way.

### Whats next ?
This application meets the requirements set out in the homework assignment. It will not scale without some changes and a bit of plumbing though:

Queries for listings of Questions will have to paginate/partition by default.
Most users will only want most the recent X Questions rather than all of them! 
Possible partition points for queries, data storage and server instances are date of posting, country, locale, and the alphabet used.

The Service could be split into read-only and write instances. At a guess, most user traffic will be read only, and can be load balanced off to a set of read only API calls executing against a set of replicated slave databases.

Internally the application uses SQL based queries. These could be replaced with some form of PreparedStatements to speed up query time.

Internally the application locally persists and also in memory caches the results of lookups against the GEO-IP service. This caching is basic but could be extended to use more robust techniques/3rd party tools. Questions could also be cached using the same in-memory caching tools and techniques. This would speed up response time for users, but more importantly allow the service to scale.   
