#!/usr/bin/env bash
curl -X PUT \
  http://localhost:9000/standups \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{  "id": 1,
  "name":  "main",
  "displayName": "Access UK Main Standup",
  "teams": [
    {
      "id": 1,
      "name": "Releases",
      "speaker": "Steff",
      "allocationInSeconds": 90
    }
  ]
}'