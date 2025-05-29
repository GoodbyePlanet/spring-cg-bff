#!/bin/sh

echo "-------------------------------------------"
echo "Building all modules..."
echo "-------------------------------------------"
./mvnw clean install

echo "-------------------------------------------"
echo "Building and starting docker containers..."
echo "-------------------------------------------"
docker-compose up --build
