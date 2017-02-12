#!/usr/bin/env bash
echo "Test starts, building docker image"
docker build -t pingpong .

echo "Building pingpong server"
docker run -idt --name serverinstance pingpong /bin/bash -c "cd /proj1; java pingpong.Server"
host=$(docker inspect --format='{{json .NetworkSettings.Networks.bridge.IPAddress}}' serverinstance)

echo "Building pingpong client"
docker run -idt --name clientinstance pingpong /bin/bash -c "cd /proj1; java pingpong.Client $host 8000"

echo "Checking output"
docker logs -f clientinstance

echo "Test finishes"