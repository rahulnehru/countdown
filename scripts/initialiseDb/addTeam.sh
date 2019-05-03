#!/usr/bin/env bash
curl -X PUT \
  http://localhost:9000/standups/main/teams \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 62637594-41b6-3617-1dda-a0d1f1bc4338' \
  -d '[
        {
      "id": 4,
      "name": "Out of country",
      "speaker": "Victor",
      "allocationInSeconds": 45
    },
    {
      "id": 5,
      "name": "CI",
      "speaker": "Katie",
      "allocationInSeconds": 45
    }
  ]'