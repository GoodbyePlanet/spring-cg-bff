#!/bin/sh

echo "-------------------------------------------"
echo "Building all modules..."
echo "-------------------------------------------"
./mvnw clean install

echo "-------------------------------------------"
echo "Building and starting docker containers..."
echo "-------------------------------------------"
docker-compose up --build

echo "-------------------------------------------"
echo "Removing docker containers"
echo "-------------------------------------------"
docker-compose down
