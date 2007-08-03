#!/bin/sh
echo -- Get the system properties of the server JVM
curl -v http://localhost:9998/properties

echo -- Echo properties
curl -v -H "Content-Type: text/plain" -d "key=value" http://localhost:9998/properties
