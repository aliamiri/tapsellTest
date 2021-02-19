FROM java:openjdk-8-alpine
ADD generator-1.0.0-SNAPSHOT.jar /opt/generator-1.0.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/opt/generator-1.0.0-SNAPSHOT.jar"]
