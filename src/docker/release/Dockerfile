FROM alopcas4wits/full-pdflatex:1.0
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>
RUN mkdir /temporal

ADD ./target/app.jar /app/
CMD ["java", "-Xmx1024m", "-Xms64m", "-jar", "/app/app.jar"]

EXPOSE 8080
