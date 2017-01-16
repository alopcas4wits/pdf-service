FROM openjdk:8-jre
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>

ADD ./target/app.jar /app/
CMD ["java", "-Xmx256m", "-Xms192m", "-jar", "/app/app.jar"]gi

EXPOSE 8080
