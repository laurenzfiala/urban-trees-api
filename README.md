# Urban Trees API

## Build

Execute `mvn clean install` to package and test the project. The `.jar` file is then located at `/target`.

To create a tag on github, use `mvn clean deploy` . Don't forget to increase and push a new version number before that.



## Execution

Execute the jar file using `java -jar` . For the API to start correctly, the following parameters are important:

* spring.profiles.active=dev
* db.user=*
* db.password=*

So your command could look like the following:

`java -Dspring.profiles.active=dev -Ddb.user=dbuser -Ddb.password=dbpassword -jar urbantrees-api.jar`.