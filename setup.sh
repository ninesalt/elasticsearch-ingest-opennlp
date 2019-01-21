#! /bin/bash

# CORS and model settings
echo "http.cors.enabled: true
http.cors.allow-origin: \"*\" 
http.cors.allow-methods: OPTIONS, HEAD, GET, POST, PUT, DELETE
http.cors.allow-headers: X-Requested-With,X-Auth-Token,Content-Type, Content-Length

ingest.opennlp.model.file.persons: en-ner-person.bin
ingest.opennlp.model.file.dates: en-ner-date.bin
ingest.opennlp.model.file.locations: en-ner-location.bin
ingest.opennlp.model.file.organizations: en-ner-organization.bin
ingest.opennlp.model.file.pos: en-pos-maxent.bin" >> /usr/share/elasticsearch/config/elasticsearch.yml