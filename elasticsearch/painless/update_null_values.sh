#!/bin/bash

ELASTIC_HOSTNAME="http://192.168.50.14:9200/"
INDEX="news_data-000001"
ENDPOINT="_update_by_query"

curl -X POST -H "Content-Type: application/json" -d @./update_null_values.json "${ELASTIC_HOSTNAME}${INDEX}/${ENDPOINT}"
