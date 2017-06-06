FROM alopcas4wits/full-pdflatex-dev:1.0
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>

RUN apt-get update -y &&\
    apt-get install texlive-full -y

WORKDIR /app
CMD ["mvn","-Pdev","-Dfork=true", "-Drun.jvmArguments=-Xmx128m -Xms64m", "spring-boot:run"]

EXPOSE 8080
