FROM openjdk:8-jre
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>

RUN apt-get update -y &&\
    apt-get install texlive-full -y

ADD ./target/app.jar /app/
CMD ["java", "-Xmx128m", "-Xms64m", "-jar", "/app/app.jar"]

EXPOSE 8080
