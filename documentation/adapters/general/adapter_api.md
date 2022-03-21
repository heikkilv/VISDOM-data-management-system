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

For getting data from the adapter there are several endpoints that can output multiple objects with a single query. Each type of object has its own endpoint, `origins`, `events`, `artifacts`, `authors`, and `metadata`. The `single` endpoint is for getting a single object based on its id and type. See separate page [API usage examples](query_examples.md) for example queries and corresponding responses.

The update query can be made to force the adapter to update its object cache in order to make other queries faster. If the adapter's cache is not up-to-date when making a multi or single query, the cache will automatically be updated before the response is returned. Since, it can take a significant amount of time for the cache updating process to finish, the update query can be used to force the cache calculations after the raw data has been updated and before any other queries are made to the adapter. A description of the cache system used in the adapter can be found at [Adapter cache system](adapter_cache.md).

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

NOTE: the name of the parameter `query` might be changed to `filter` in the implementation

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

See the following section for examples of the actual objects given in the results. The ordering of the result objects is done using the id attribute.

### Example responses for single query

If no matching object is found, then a response with a status code 404 and the following content is returned:

```json
{
  "description": "No results found",
  "status": "NotFound"
}
```

If an object matching the query parameters is found, then a response with a status code 200 and the corresponding object represented in JSON will be returned.

One example for successful query for each supported object type is given below:

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
{
    "id": "4abc0f01-c508-5751-e123-48ab507a21b7",
    "type": "pipeline",
    "time": "2022-02-21T01:07:05.967+02:00",
    "duration": 1288,
    "message": "success",
    "origin": {
        "id": "2aa8c5a1-f84e-5990-2d21-d31f0a93270c",
        "type": "gitlab"
    },
    "author": {
        "id": "782ef8e6-6958-5640-0a63-3ce29ffcf428",
        "type": "gitlab_user"
    },
    "data": {
        "pipeline_id": 11472,
        "project_id": 318,
        "source": "push",
        "ref": "event-adapter",
        "queued_duration": 1,
        "created_at": "2022-02-21T01:07:04.058+02:00",
        "updated_at": "2022-02-21T01:28:39.363+02:00",
        "finished_at": "2022-02-21T01:28:39.355+02:00",
        "sha": "51103add1dfce74268029bcc766e076080bab4e2",
        "tag": false,
        "detailed_status": {
            "text": "passed",
            "label": "passed",
            "group": "success"
        },
        "jobs": [
            94378,
            94377
        ]
    },
    "related_constructs": [
        {
            "id": "1a445497-f5c5-5b99-9aeb-d594bdc3266d",
            "type": "pipeline_report"
        },
        {
            "id": "782ef8e6-6958-5640-0a63-3ce29ffcf428",
            "type": "gitlab_user"
        }
    ],
    "related_events": [
        {
            "id": "22e93825-b16a-5fe3-b7a3-68205de376bc",
            "type": "pipeline_job"
        },
        {
            "id": "82b47df3-c1d7-5389-c8a8-df7cdc38b075",
            "type": "pipeline_job"
        }
    ]
}
```

#### Example pipeline job event

```json
{
    "id": "28670faf-bdf1-5d26-60e5-4e8692bb8957",
    "type": "pipeline_job",
    "time": "2022-03-04T18:49:37.084+02:00",
    "duration": 55.178818,
    "message": "success",
    "origin": {
        "id": "2aa8c5a1-f84e-5990-2d21-d31f0a93270c",
        "type": "gitlab"
    },
    "author": {
        "id": "782ef8e6-6958-5640-0a63-3ce29ffcf428",
        "type": "gitlab_user"
    },
    "data": {
        "job_id": 94971,
        "pipeline_id": 11561,
        "user_id": 1234,
        "queued_duration": 2.669902,
        "name": "scalastyle",
        "stage": "Static Analysis",
        "created_at": "2022-03-04T18:49:34.061+02:00",
        "finished_at": "2022-03-04T18:50:32.263+02:00",
        "commit_id": "29b2c1d2b8d95ae671acda1eaaaa96b4430dd5e0",
        "ref": "event-adapter",
        "tag": false
    },
    "related_constructs": [
        {
            "id": "782ef8e6-6958-5640-0a63-3ce29ffcf428",
            "type": "gitlab_user"
        }
    ],
    "related_events": [
        {
            "id": "b93ed364-9173-503d-e8a9-41da4d85d3ac",
            "type": "pipeline"
        },
        {
            "id": "600380d9-e04d-5c64-26a8-16f3b690f491",
            "type": "commit"
        }
    ]
}
```

#### Example file artifact

```json
{
    "id": "3685bdd9-c94c-5649-3562-81ef757d31cb",
    "type": "file",
    "name": "adapter",
    "description": "scala/gitlab-adapter/src/main/scala/visdom/http/server/adapter",
    "state": "exists",
    "origin": {
        "id": "2aa8c5a1-f84e-5990-2d21-d31f0a93270c",
        "type": "gitlab"
    },
    "data": {
        "file_id": "19b4646c43b430d8cf1660e5c1568479339dbe5f",
        "type": "tree",
        "mode": "040000",
        "commits": [
            "53a724ad563da681db04d199bda04a7e35f7d937",
            "84fa48f3154b7e4ee648c40fa140d922791cd674",
            "a9aae6f7f0a5666a8f317b4f852777e29d912e71"
        ]
    },
    "related_constructs": [
        {
            "id": "15a0d241-c1b0-5301-fa68-0ea932226bc6",
            "type": "file"
        },
        {
            "id": "c27b9b17-3c3d-5080-df2c-bd39eb7dddcb",
            "type": "file"
        }
    ],
    "related_events": [
        {
            "id": "74daef02-57f8-5ce9-2aaf-e726d8d4f084",
            "type": "commit"
        },
        {
            "id": "d9e4660c-49a2-5078-dea5-0c3f2536326c",
            "type": "commit"
        },
        {
            "id": "21eb450a-89c3-5a17-04a4-1a500522017e",
            "type": "commit"
        }
    ]
}
```

#### Example pipeline report artifact

```json
{
    "id": "a329bdf1-2b4e-557a-1123-029552809842",
    "type": "pipeline_report",
    "name": "11347",
    "description": "Report for pipeline 11347",
    "state": "complete",
    "origin": {
        "id": "2aa8c5a1-f84e-5990-2d21-d31f0a93270c",
        "type": "gitlab"
    },
    "data": {
        "total_time": 0,
        "success_count": 0,
        "error_count": 0,
        "skipped_count": 0,
        "total_count": 0,
        "failed_count": 0
    },
    "related_constructs": [],
    "related_events": [
        {
            "id": "5a025d2d-38df-5ce8-f3a3-19c70d0de10b",
            "type": "pipeline"
        }
    ]
}
```

TODO: add an example with some actual pipeline report content

#### Example committer author

```json
{
    "id": "8b9e99c7-5b43-5942-85a9-7ceecc63734c",
    "type": "committer",
    "name": "<committer name>",
    "description": "",
    "state": "active",
    "origin": {
        "id": "4e16463f-8219-5563-25ad-f32be8f820bc",
        "type": "gitlab"
    },
    "data": {
        "email": "<committer email>"
    },
    "related_constructs": [
        {
            "id": "2601cc80-1862-5f8a-41e6-835c5607f0cd",
            "type": "gitlab_user"
        }
    ],
    "related_events": [
        {
            "id": "600380d9-e04d-5c64-26a8-16f3b690f491",
            "type": "commit"
        },
        {
            "id": "d5020543-898d-5336-23e8-7237e6dde06a",
            "type": "commit"
        },
        {
            "id": "cfc1390b-d40e-52ba-b3e5-4c2aec8405b3",
            "type": "commit"
        }
    ]
}
```

#### Example gitlab user author

```json
{
    "id": "cb933e6d-17ac-50ed-ee20-d467c5b8aea8",
    "type": "gitlab_user",
    "name": "<gitlab user name>",
    "description": "",
    "state": "active",
    "origin": {
        "id": "4e16463f-8219-5563-25ad-f32be8f820bc",
        "type": "gitlab"
    },
    "data": {
        "user_id": 1234,
        "username": "<gitlab username>"
    },
    "related_constructs": [
        {
            "id": "ed4b20d1-ee19-500b-1064-9e7aab03f884",
            "type": "committer"
        }
    ],
    "related_events": [
        {
            "id": "deaae4d7-aa4f-592e-62ea-f09bae9c6705",
            "type": "pipeline_job"
        },
        {
            "id": "45572d80-7031-5ec6-34a8-fea5a13c1365",
            "type": "pipeline_job"
        },
        {
            "id": "75faaf4d-fea7-5f24-7225-40375b6fda6a",
            "type": "pipeline"
        },
        {
            "id": "5f7b4f55-78bd-51be-0929-9ffbf9cdc384",
            "type": "commit"
        },
        {
            "id": "ed68e45e-50a3-5aea-a222-bf5aae357052",
            "type": "commit"
        },
        {
            "id": "a7ab894a-95ba-5808-2727-3d7902682123",
            "type": "commit"
        }
    ]
}
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
