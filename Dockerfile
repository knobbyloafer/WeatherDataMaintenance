FROM anapsix/alpine-java
MAINTAINER Tom Knobloch 
COPY ./target/WeatherDataMaintenance-1.0-SNAPSHOT-jar-with-dependencies.jar /home/WeatherDataMaintenance-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY ./weatherdata.properties /home/weatherdata.properties
WORKDIR /home
CMD ["java", "-jar", "/home/WeatherDataMaintenance-1.0-SNAPSHOT-jar-with-dependencies.jar"]