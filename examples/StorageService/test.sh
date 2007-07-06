#!/bin/sh
echo -- Get the containers
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers
echo

echo -- Create the quotes container
java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes
echo

echo -- Get the containers
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers
echo

echo -- Add some quotes
echo "Something is rotten in the state of Denmark" | java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes/1 text/plain
echo "I could be bounded in a nutshell" | java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes/2 text/plain
echo "catch the conscience of the king" | java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes/3 text/plain
echo "Get thee to a nunnery" | java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes/4 text/plain
echo

echo -- Get the quotes container
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers/quotes
echo

echo -- Search the quotes container for content that contains 'king'
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers/quotes?search=king
echo

echo -- Get the 3rd quote in the quotes container
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers/quotes/3
echo

echo -- Update the 3rd quote
echo "The play's the thing Wherein I'll catch the conscience of the king" | java -jar dist/StorageService.jar PUT http://127.0.0.1:9998/storage/containers/quotes/3 text/plain
echo

echo -- Get the 3rd quote in the quotes container
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers/quotes/3
echo

echo -- Search the quotes container for content that contains 'king'
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers/quotes?search=king
echo

echo -- Delete the 3rd quote
java -jar dist/StorageService.jar DELETE http://127.0.0.1:9998/storage/containers/quotes/3
echo

echo -- Delete the quotes container
java -jar dist/StorageService.jar DELETE http://127.0.0.1:9998/storage/containers/quotes
echo

echo -- Get the containers
java -jar dist/StorageService.jar GET http://127.0.0.1:9998/storage/containers
echo


