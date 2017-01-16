FROM maven:3.3.9-jdk-8
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>

WORKDIR /app
CMD ["mvn","-Pdev","-Dfork=true", "spring-boot:run"]

EXPOSE 8080
