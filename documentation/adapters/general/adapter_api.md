# API for the general data model adapter

<!-- no toc -->
- [Query parameters](#query-parameters)
    - [Query parameters for multi type query](#query-parameters-for-multi-type-query)
    - [Query parameters for single type query](#query-parameters-for-single-type-query)
    - [Query parameters for other queries](#query-parameters-for-other-queries)
    - [Filter query syntax](#filter-query-syntax)
- [Example responses](#example-responses)
    - [Example response for multi query](#example-response-for-multi-query)
    - [Example responses for single query](#example-responses-for-single-query)
        - [Example GitLab Origin](#example-gitlab-origin)
        - [Example commit event](#example-commit-event)
        - [Example pipeline event](#example-pipeline-event)
        - [Example pipeline job event](#example-pipeline-job-event)
        - [Example file artifact](#example-file-artifact)
        - [Example pipeline report artifact](#example-pipeline-report-artifact)
        - [Example committer author](#example-committer-author)
        - [Example gitlab user author](#example-gitlab-user-author)
    - [Example info endpoint response](#example-info-endpoint-response)
- [API usage examples](#api-usage-examples)

For getting data from the adapter there are several endpoints that can output multiple objects with a single query. Each type of object has its own endpoint, `origins`, `events`, `artifacts`, `authors`, and `metadata`. The `single` endpoint is for getting a single object based on its id and type.

The update query can be made to force the adapter to update its object cache in order to make other queries faster. If the adapter's cache is not up-to-date when making a multi or single query, the cache will automatically be updated before the response is returned. Since, it can take a significant amount of time for the cache updating process to finish, the update query can be used to force the cache calculations after the raw data has been updated and before any other queries are made to the adapter.

| Endpoint     | Type   | Description |
| ------------ | ------ | ----------- |
| `/origins`   | multi  | Returns origin objects |
| `/events`    | multi  | Returns events objects |
| `/authors`   | multi  | Returns author objects |
| `/artifacts` | multi  | Returns non-author artifact objects |
| `/metadata`  | multi  | Returns metadata objects |
| `/single`    | single | Returns a single object |
| `/update`    | cache  | Updates the cache to make other queries faster |
| `/info`      | info   | Returns general adapter information |

In addition, the `/swagger` endpoint provides a Swagger UI interface to test the adapter API. It also contains some examples about possible responses in different problem cases. The corresponding [swagger.json](./swagger.json) definition can also be found out in this folder.

## Query parameters

The API will only accept GET queries to any of the endpoints.

### Query parameters for multi type query

Returns the origin objects corresponding to the query parameters.

| Query parameter | Type | Default | Description |
| --------------- | ---- | ------- | ----------- |
| `page`     | optional | 1   | The page number used in filtering the results. |
| `pageSize` | optional | 100 | The page size used in filtering the results. Maximum allowed value is 10000. |
| `type`     | optional |     | The type of the considered objects. Empty value indicates that all supported types (i.e., all events, or all origins, ...) are considered. |
| `query`    | optional |     | Filter query to be applied. See section [Filter query syntax](#filter-query-syntax) for the allowed syntax. |
| `data`     | optional |     | A comma-separated list of data attributes included in the response. Empty value indicates that all data attributes will be included. |
| `links`    | optional |     | Indicates what type object links are included in the results. Empty value indicates that all links are included. Allowed values: all, events, constructs, none |

The links parameter is ignored for the `/origins` endpoint.

### Query parameters for single type query

The single query returns the corresponding object with all possible attributes included as a JSON object. The query parameters correspond to the general object links.

| Query parameter | Type     | Description                  |
| --------------- | ----     | ---------------------------- |
| `type`          | required | The type of the object.      |
| `uuid`          | required | The unique id of the object. |

NOTE: for consistency, should `uuid` be changed to `id`?

### Query parameters for other queries

Neither info nor update queries take any any parameters.

### Filter query syntax

A simplified version of the Simple Query Language used in [FIWARE-NGSI v2](http://telefonicaid.github.io/fiware-orion/api/v2/stable/) (Introduction -> Specification -> Simple Query Language)

- Format for a single filter: `<attribute_name><filter_operation><target_value>`
- Attribute name corresponds to the attribute values in the full object (Origin, Event, or Construct).
    - Subattributes can be referred to by using a dot (`.`). For example, `data.commit_id` or `data.stats.additions`.
- Supported filter operations:
    - `==` (equality)
    - `!=` (non-equality)
    - `<` (less than)
    - `<=` (less or equal than)
    - `>` (greater than)
    - `>=` (greater or equal than)
    - `~=` (regex).
- Multiple filters can be given by using a semicolon as a separator (`;`). Only results matching all given filters are returned.
    - For example, `<filter_1>;<filter_2>;<filter_3>`.

TODO: add a full description of the allowed syntax

## Example responses

### Example response for multi query

Successful query returns a JSON response with a status code 200. The format of the response is the following:

```json
{
    "count": 10,
    "page": 1,
    "page_size": 10,
    "next_page": 2,
    "previous_page": null,
    "total_count": 25,
    "results": [
        // 10 objects given as a list
    ]
}
```

where `count` is the number of results in the response, `page` and `page_size` are the used pagination options, `next_page` and `previous_page` are the page numbers for the next and previous set of results (with a value null if there are no more results in that particular direction), and `total_count` is the total number results matching the query parameters.

### Example responses for single query

If an object matching the query parameters was found, then a response with status code 200 and the corresponding object represented in JSON is returned. One example for each supported object type is given below:

#### Example GitLab Origin

```json
{
    "id": "237d53a6-90b3-5e22-976d-d36fe5e0ee95",
    "type": "gitlab",
    "source": "https://gitlab.tuni.fi",
    "context": "cs/VISDOM/data-management-system",
    "data": {
        "project_id": 386,
        "group_name": "cs/VISDOM"
    }
}
```

#### Example commit event

```json
{
    "id": "b20c6cb4-cdea-5249-332c-6a3620d1769b",
    "type": "commit",
    "time": "2021-11-27T22:55:45+02:00",
    "duration": 0,
    "message": "Small comment and style change\n",
    "origin": {
        "id": "2aa8c5a1-f84e-5990-2d21-d31f0a93270c",
        "type": "gitlab"
    },
    "author": {
        "id": "8b9e99c7-5b43-5942-85a9-7ceecc63734c",
        "type": "committer"
    },
    "data": {
        "commit_id": "bc9c896399a701f86cc713a1956460a30834a56f",
        "short_id": "bc9c8963",
        "stats": {
            "additions": 3,
            "deletions": 2,
            "total": 5
        },
        "committer_email": "<committer email>",
        "committer_name": "<committer name>",
        "author_name": "<commit author name>",
        "author_email": "<commit author email>",
        "authored_date": "2021-11-27T22:55:45.000+02:00",
        "parent_ids": [
            "b7a30a20754e98d44bc49a99e12d9e99d9c47815"
        ],
        "title": "Small comment and style change",
        "files": [
            "scala/core/src/main/scala/visdom/json/JsonUtils.scala"
        ],
        "refs": [
            {
                "type": "branch",
                "name": "event-adapter"
            },
            {
                "type": "branch",
                "name": "master"
            }
        ],
        "web_url": "<GitLab URL for the commit>"
    },
    "related_constructs": [
        {
            "id": "88212df4-859c-5056-9aab-6622b1130614",
            "type": "file"
        },
        {
            "id": "782ef8e6-6958-5640-0a63-3ce29ffcf428",
            "type": "gitlab_user"
        },
        {
            "id": "8b9e99c7-5b43-5942-85a9-7ceecc63734c",
            "type": "committer"
        }
    ],
    "related_events": [
        {
            "id": "bd4bc54c-1217-56b1-f1e5-23dba1aac1bf",
            "type": "commit"
        }
    ]
}
```

#### Example pipeline event

```json
TODO
```

#### Example pipeline job event

```json
TODO
```

#### Example file artifact

```json
TODO
```

#### Example pipeline report artifact

```json
TODO
```

#### Example committer author

```json
TODO
```

#### Example gitlab user author

```json
TODO
```

TODO: add examples for other types of supported object types (from A+ data)

### Example info endpoint response

The `/info` endpoint does not take any query parameters and it returns a status code 200 and JSON formatted object containing information about the general data model adapter.

Example output:

```json
{
    "adapterType": "GeneralModel",
    "apiAddress": "http://visdom-adapter-general:8080",
    "componentName": "adapter",
    "componentType": "visdom-adapter-general",
    "startTime": "2022-03-14T09:26:28.980Z",
    "swaggerDefinition": "/api-docs/swagger.json",
    "version": "0.1"
}
```

## API usage examples

TODO
