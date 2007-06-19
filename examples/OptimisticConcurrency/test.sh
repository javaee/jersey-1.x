#!/bin/sh
echo -- Get the item meta-data
java -jar dist/OptimisticConcurrency.jar GET http://127.0.0.1:9998/occ/item
echo

echo -- Get the item content
java -jar dist/OptimisticConcurrency.jar GET http://127.0.0.1:9998/occ/item/content
echo

echo -- Update the item content
echo "All play and no REST makes me a dull boy" | java -jar dist/OptimisticConcurrency.jar PUT http://127.0.0.1:9998/occ/item/content/0 text/plain
echo

echo -- Get the item meta-data
java -jar dist/OptimisticConcurrency.jar GET http://127.0.0.1:9998/occ/item
echo

echo -- Get the item content
java -jar dist/OptimisticConcurrency.jar GET http://127.0.0.1:9998/occ/item/content
echo

echo -- Update the item content using an old version
echo "All play and no REST makes me a dull boy" | java -jar dist/OptimisticConcurrency.jar PUT http://127.0.0.1:9998/occ/item/content/0 text/plain
echo
