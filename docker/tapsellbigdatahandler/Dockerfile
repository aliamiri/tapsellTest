FROM java:openjdk-8-alpine
ADD tapsellbigdatahandler-1.0.0-SNAPSHOT.jar /opt/tapsellbigdatahandler-1.0.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod" , "-jar", "/opt/tapsellbigdatahandler-1.0.0-SNAPSHOT.jar"]
