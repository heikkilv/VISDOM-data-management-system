# General data model

<!-- no toc -->
- [General data model description](#general-data-model-description)
- [Supported raw data](#supported-raw-data)
- [Implementation details](#implementation-details)
- [JSON representation](#json-representation)

## General data model description

The main objects of the data model are events, constructs and origins. A construct is an aspect of software engineering that can be either metadata (i.e. static data) or an artifact that can have different states. An event represents some kind of action that has happened to one or more constructs. Each event has an author that can be represented as an artifact. Both events and construct have an origin which represents the source and context for the events and constructs.

A general diagram describing the used data model can be seen below:
![Diagram of general software data model](general-data-model.png)

## Supported raw data

TODO

## Implementation details

TODO

## JSON representation

See [Example responses for single query](adapter_api.md#example-responses-for-single-query) for examples of the different supported object types using the JSON representation of the API.
