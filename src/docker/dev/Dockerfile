FROM alopcas4wits/full-pdflatex-dev:1.0
MAINTAINER Jonas Holtorf <Jonas.Holtorf@4wits.de>

WORKDIR /app
CMD ["mvn","-Pdev","-Dfork=true", "-Drun.jvmArguments=-Xmx128m -Xms64m", "spring-boot:run"]

EXPOSE 8080
