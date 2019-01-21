FROM docker.elastic.co/elasticsearch/elasticsearch:6.5.4

#TO DO move all this to a bash script
ENV discovery.type=single-node
EXPOSE 9200
EXPOSE 9300

COPY /build/distributions/ /plugins/

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install \
        file:///plugins/ingest-opennlp-6.5.4.1-SNAPSHOT.zip
        #https://github.com/ninesalt/elasticsearch-ingest-opennlp/releases/download/6.5.4.1-pos/ingest-opennlp-wpos-6.5.4.1.zip
        
#RUN /usr/share/elasticsearch/bin/ingest-opennlp/download-models
COPY models/  /usr/share/elasticsearch/config/ingest-opennlp/
COPY setup.sh /setup.sh

RUN bash /setup.sh