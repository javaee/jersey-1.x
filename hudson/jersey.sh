#!/bin/bash -xe
# beta-testing Maven 2.0.9 on Hudson slaves --- this will become the default Maven version soon (Kohsuke)
export M2_HOME=/files/hudson/tools/maven-2.0.9
export PATH=$M2_HOME/bin:$PATH
export MAVEN_OPTS="-Xmx1024m"
mvn -version
java -version

# mvn dependency:resolve
# clean install will test as well
JERSEY_HTTP_SLEEP=2000 JERSEY_HTTP_STOPSEC=2 mvn -e clean install
# jersey-bundle needs to be tested as well
# JERSEY_HTTP_SLEEP=2000 JERSEY_HTTP_STOPSEC=2 mvn -e clean test -Pbundle-dependency
