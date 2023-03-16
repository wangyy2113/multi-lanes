#!/bin/bash

echo "
----------stopping all ...------------"

docker-compose -f docker-compose.yml down

echo "
----------stopped------------------------------"