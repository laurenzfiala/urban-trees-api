# Urban Trees API

## Build

Execute `mvn clean install` to package and test the project. The `.jar` file is then located at `/target`.

To create a tag on github, use `mvn clean deploy` . Don't forget to increase and push a new version number before that.

For the API to build correctly, the following parameters are important:

- spring.profiles.active=dev (or prod)
- db.user=*
- db.password=*

So, add ` "-Dspring.profiles.active=dev" "-Ddb.user=dbuser" "-Ddb.password=dbpassword"` to the end of install and deploy commands.

_Note: Don't forget the double quotes around every parameter!_



## Execution

Execute the jar file using `java -jar` . 

So your command could look like the following:

`java -Dspring.profiles.active=dev -Ddb.user=dbuser -Ddb.password=dbpassword -jar urbantrees-api.jar`.



## Integration Testing

Test integration tests using JUnit by executing `mvn test` . The command line arguments from above are also needed, since the tests go against the actual database.



## Unit Testing

Since this project contains almost no business logic, there is no sense in implementing any unit tests.