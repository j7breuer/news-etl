#!/bin/bash

ELASTIC_HOSTNAME="http://192.168.50.14:9200/"
ENDPOINT="_template"
TEMPLATE_NAME="news_data_template"

curl -X PUT -H "Content-Type: application/json" -d @../templates/news_data_schema.JSON "${ELASTIC_HOSTNAME}${ENDPOINT}/${TEMPLATE_NAME}"