# Elasticsearch OpenNLP Ingest Processor

This repo is forked from spinscales [repo](https://github.com/spinscale/elasticsearch-ingest-opennlp). I added support for POS tagging. It tags all the words in the field and counts how many times they occur. 

The ES ingest processor is doing named/date/location/'whatever you have a model for' entity recognition and stores the output in the JSON before it is being stored.

This plugin is also intended to show you, that using gradle as a build system makes it very easy to reuse the testing facilities that elasticsearch already provides. First, you can run regular tests, but by adding a rest test, the plugin will be packaged and unzipped against elasticsearch, allowing you to execute a real end-to-end test, by just adding a java test class.

## Installation
You can install the plugin for ES v6.5.4 using this command:

`bin/elasticsearch-plugin install https://github.com/ninesalt/elasticsearch-ingest-opennlp/releases/download/6.5.4.1/ingest-opennlp-wpos-6.5.4.1.zip`

**IMPORTANT**: If you are running this plugin with Elasticsearch 6.5.2 or newer, you need to download the NER models from sourceforge after installation.

To download the models, run the following under Linux and osx

```
bin/ingest-opennlp/download-models
```

If you are using windows, please use the following command

```
bin/ingest-opennlp/download-models.bat
```


## Usage

This is how you configure a pipeline with support for opennlp

You can add the following lines to the `config/elasticsearch.yml` (as those models are shipped by default, they are easy to enable). The models are looked up in the `config/ingest-opennlp/` directory.

```
ingest.opennlp.model.file.persons: en-ner-person.bin
ingest.opennlp.model.file.dates: en-ner-date.bin
ingest.opennlp.model.file.locations: en-ner-location.bin
```

Now fire up Elasticsearch and configure a pipeline

```
PUT _ingest/pipeline/opennlp-pipeline
{
  "description": "A pipeline to do named entity extraction",
  "processors": [
    {
      "opennlp" : {
        "field" : "my_field"
      }
    }
  ]
}

PUT /my-index/my-type/1?pipeline=opennlp-pipeline
{
  "my_field" : "Kobe Bryant was one of the best basketball players of all times. Not even Michael Jordan has ever scored 81 points in one game. Munich is really an awesome city, but New York is as well. Yesterday has been the hottest day of the year."
}

GET /my-index/my-type/1
{
  "my_field" : "Kobe Bryant was one of the best basketball players of all times. Not even Michael Jordan has ever scored 81 points in one game. Munich is really an awesome city, but New York is as well. Yesterday has been the hottest day of the year.",
  "entities" : {
    "locations" : [ "Munich", "New York" ],
    "dates" : [ "Yesterday" ],
    "names" : [ "Kobe Bryant", "Michael Jordan" ]
  },
  "pos": {
      "NN": {
          "basketball": 1,
          "game": 1,
          "city": 1,
          "year": 1,
          "Yesterday": 1,
          "day": 1
      },
      "JJ": {
          "awesome": 1
      },
      "CC": {
          "but": 1
      },
      "CD": {
          "81": 1,
          "one": 2
      },
      "VBN": {
          "been": 1,
          "scored": 1
      },
      "IN": {
          "in": 1,
          "of": 3
      },
      "VBZ": {
          "is": 2,
          "has": 2
      },
      "DT": {
          "the": 3,
          "all": 1,
          "an": 1
      },
      "RB": {
          "ever": 1,
          "Not": 1,
          "as": 1,
          "even": 1,
          "well": 1,
          "really": 1
      },
      "NNP": {
          "Bryant": 1,
          "Munich": 1,
          "New": 1,
          "Kobe": 1,
          "York": 1,
          "Michael": 1,
          "Jordan": 1
      },
      "JJS": {
          "hottest": 1,
          "best": 1
      },
      "NNS": {
          "times": 1,
          "players": 1,
          "points": 1
      },
      "VBD": {
          "was": 1
      }
  },
}
```

You can also specify only certain named entities in the processor, i.e. if you only want to extract names


```
PUT _ingest/pipeline/opennlp-pipeline
{
  "description": "A pipeline to do named entity extraction",
  "processors": [
    {
      "opennlp" : {
        "field" : "my_field"
        "fields" : [ "names" ]
      }
    }
  ]
}
```

## Configuration

You can configure own models per field, the setting for this is prefixed `ingest.opennlp.model.file.`. So you can configure any model with any field name, by specifying a name and a path to file, like the three examples below:

| Parameter | Use |
| --- | --- |
| ingest.opennlp.model.file.names     | Configure the file for named entity recognition for the field name        |
| ingest.opennlp.model.file.dates     | Configure the file for date entity recognition for the field date         |
| ingest.opennlp.model.file.persons   | Configure the file for person entity recognition for the field person     |
| ingest.opennlp.model.file.WHATEVER | Configure the file for WHATEVER entity recognition for the field WHATEVER |

* Note: it doesn't matter what the field name is as long the filename is the same as the one downloaded by the plugin or added manually by you. 
For the sake of simplicity the convention here is plural for the fieldname and single for the filename (similar to the example in the Usage section).

## Development setup & running tests

In order to install this plugin, you need to create a zip distribution first by running

```bash
./gradlew clean check
```

This will produce a zip file in `build/distributions`. As part of the build, the models are packaged into the zip file, but need to be downloaded before. There is a special task in the `build.gradle` which is downloading the models, in case they dont exist.

After building the zip file, you can install it like this

```bash
bin/plugin install file:///path/to/elasticsearch-ingest-opennlp/build/distribution/ingest-opennlp-X.Y.Z-SNAPSHOT.zip
```

There is no need to configure anything, as the models art part of the zip file.

## Bugs & TODO

* A couple of groovy build mechanisms from core are disabled. See the `build.gradle` for further explanations
* Only the most basic NLP functions are exposed, please fork and add your own code to this!

