#!/usr/bin/env bash
curl -X DELETE \
  http://localhost:9000/teams \
  -H 'content-type: application/json' \
  -d '["Fes", "CI"]'