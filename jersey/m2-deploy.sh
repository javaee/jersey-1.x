#!/bin/sh
mvn deploy:deploy-file \
 -f maven/deploy.pom \
 -Dfile=../dist/jersey.jar \
 -DpomFile=../build/maven/jersey-pom.xml \
 -DrepositoryId=dev.java.net.m2 \
 -Durl=java-net:/maven2-repository/trunk/www/repository/ \
 -DgeneratePom=false \
 -DuniqueVersion=false
