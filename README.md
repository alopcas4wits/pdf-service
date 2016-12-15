### Seed for new Microservice

#### To Change

Richtigen Service Namen setzen in

    /src/resources/bootstrap.yml
    /pom.xml

Package umbennen

Application Class umbennen

#### Aktualiseren

Einmalig:
    
    git remote add upstream git@4wits.githost.io:CORE/microservice-java-seed.git
    
Updated:
    
    git fetch upstream
    git merge remotes/upstream/master