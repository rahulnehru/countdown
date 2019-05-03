#!/usr/bin/env bash
curl -vX POST http://localhost:9000/standups -d@standUp.json \
--header "Content-Type: application/json"