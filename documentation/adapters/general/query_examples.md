# API query examples

<!-- no toc -->
- [Fetch the first 1000 events](#fetch-the-first-1000-events)
- [Fetch origins 4-6](#fetch-origins-4-6)
- [Fetch the first 50 file artifacts](#fetch-the-first-50-file-artifacts)
- [Fetch the first committer](#fetch-the-first-committer)
- [Fetch the first 100 commits](#fetch-the-first-100-commits)
- [Fetch the commits made in 2022 or later](#fetch-the-commits-made-in-2022-or-later)
- [Fetch the commits made before 2022 and have exactly 123 additions](#fetch-the-commits-made-before-2022-and-have-exactly-123-additions)
- [Fetch the events that happened on March 14 2022](#fetch-the-events-that-happened-on-march-14-2022)
- [Fetch a single pipeline event](#fetch-a-single-pipeline-event)
- [Fetch a single committer](#fetch-a-single-committer)

In all examples `<ADAPTER_URI>` should be replaced with the actual address to the adapter.

All given queries use the GET request method.

To see the full list of attribute different types of objects can have, see the examples at [Example responses for single query](adapter_api.md#example-responses-for-single-query).

The result objects included in the response are decided based on alphabetical ordering with respect to the id attribute and the pagination parameters. The same alphabetical ordering is also used to order the result objects withing the response.

## Fetch the first 1000 events

Including full data on all events in the response.

- Query:
    `<ADAPTER_URI>/events?page=1&pageSize=1000`

- Example Response:

    ```json
    {
        "count": 1000,
        "page": 1,
        "page_size": 1000,
        "next_page": 2,
        "previous_page": null,
        "total_count": 12345,
        "results": [
            {
                "id": "120c6cb4-cdea-5249-332c-6a3620d1769b",
                "type": "commit",
                // all other attributes for a commit event
            },
            // 999 other event objects (can be any event types)
        ]
    }
    ```

    In this case there were a total of 12345 events available and the first 1000 of them are included in the response.

## Fetch origins 4-6

Including full data on all objects in the response.

- Query:
    `<ADAPTER_URI>/origins?page=2&pageSize=3`

- Example Response:

    ```json
    {
        "count": 3,
        "page": 2,
        "page_size": 3,
        "next_page": 3,
        "previous_page": 1,
        "total_count": 8,
        "results": [
            {
                "id": "237d53a6-90b3-5e22-976d-d36fe5e0ee95",
                "type": "gitlab",
                "source": "https://gitlab.tuni.fi",
                "context": "cs/VISDOM/data-management-system",
                "data": {
                    "project_id": 386,
                    "group_name": "cs/VISDOM"
                }
            },
            // 2 other origin objects (can be any origin types)
        ]
    }
    ```

    In this case there were a total of 8 origins available of which the 4th, 5th, and 6th are included in the response.

## Fetch the first 50 file artifacts

Including full data on all objects in the response.

- Query:
    `<ADAPTER_URI>/artifacts?page=1&pageSize=50&type=file`

- Example Response:

    ```json
    {
        "count": 50,
        "page": 1,
        "page_size": 50,
        "next_page": 2,
        "previous_page": null,
        "total_count": 224,
        "results": [
            {
                "id": "3685bdd9-c94c-5649-3562-81ef757d31cb",
                "type": "file",
                // all other attributes for a file artifact
            },
            // 49 other file artifact objects
        ]
    }
    ```

    In this case there were a total of 224 file artifacts available of which the first 50 are included in the response.

## Fetch the first committer

Including no data attributes and only including event link information in the the response.

- Query:
    `<ADAPTER_URI>/authors?page=1&pageSize=1&type=committer&data=none&links=events`

- Example Response:

    ```json
    {
        "count": 1,
        "page": 1,
        "page_size": 1,
        "next_page": 2,
        "previous_page": null,
        "total_count": 12,
        "results": [
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
                "data": {},
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
        ]
    }
    ```

    In this case there were a total of 12 committer type authors available with only the first committer is included in the response.

## Fetch the first 100 commits

Including only the commit ids in the data and not including any link information in the the response. Using the default pagination values (1 and 100 for page and pageSize respectively).

- Query:
    `<ADAPTER_URI>/events?type=commit&data=commit_id&links=none`

- Example Response:

    ```json
    {
        "count": 75,
        "page": 1,
        "page_size": 100,
        "next_page": null,
        "previous_page": null,
        "total_count": 75,
        "results": [
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
                    "commit_id": "bc9c896399a701f86cc713a1956460a30834a56f"
                }
            },
            // 74 other commit events given in similar format
        ]
    }
    ```

    In this case there were a total of 75 commit events available and all of them are included in the response.

## Fetch the commits made in 2022 or later

Including full data on all commit events in the response. Using the default pagination values (1 and 100 for page and pageSize respectively).

- Query:
    `<ADAPTER_URI>/events?type=commit&query=time>=2022-01-01T00:00:00`

- Example Response:

    ```json
    {
        "count": 73,
        "page": 1,
        "page_size": 100,
        "next_page": null,
        "previous_page": null,
        "total_count": 73,
        "results": [
            {
                "id": "0059c0e9-6681-54fe-0d2c-c00aa1820a12",
                "type": "commit",
                "time": "2022-02-14T17:45:43+02:00",
                // all other attributes for a commit event
            },
            // 72 other commit events given in similar format
        ]
    }
    ```

    In this case there were a total of 73 commit events that were made in 2022 available and all of them are included in the response.

## Fetch the commits made before 2022 and have exactly 123 additions

Including full data but no link information on all commit events in the response. Using the default pagination values (1 and 100 for page and pageSize respectively).

- Query:
    `<ADAPTER_URI>/events?type=commit&links=none&query=time<2022-01-01T00:00:00;data.stats.additions==123`

- Example Response:

    ```json
    {
        "count": 3,
        "page": 1,
        "page_size": 100,
        "next_page": null,
        "previous_page": null,
        "total_count": 3,
        "results": [
            {
                "id": "95616b06-e86c-541f-e26a-39cc590fddae",
                "type": "commit",
                "time": "2021-10-05T16:09:06+03:00",
                "duration": 0,
                "message": "revert not sync instances for progress/cumulative\n",
                "origin": {
                    "id": "a0a9a28a-4b9a-58fc-3eaa-89247da4753a",
                    "type": "gitlab"
                },
                "author": {
                    "id": "ed4b20d1-ee19-500b-1064-9e7aab03f884",
                    "type": "committer"
                },
                "data": {
                    "commit_id": "948d1e2845967b7a3dfaeefccfaad8c228344133",
                    "stats": {
                        "additions": 123,
                        "deletions": 89,
                        "total": 212
                    },
                    // all other data attributes for a commit event
                }
            },
            // 2 other commit events given in similar format
        ]
    }
    ```

    In this case there were a total of 3 commit events, that were made before 2022 and that had exactly 123 additions included, available and all of them are included in the response.

## Fetch the events that happened on March 14 2022

Including full data on all events in the response. Using the default pagination values (1 and 100 for page and pageSize respectively).

- Query:
    `<ADAPTER_URI>/events?query=time~=2022-03-14`

- Example Response:

    ```json
    {
        "count": 6,
        "page": 1,
        "page_size": 100,
        "next_page": null,
        "previous_page": null,
        "total_count": 6,
        "results": [
            {
                "id": "63eb0cfd-6376-594f-e9ef-97de475e5215",
                "type": "pipeline",
                "time": "2022-03-14T10:49:51.067+02:00",
                // all other attributes for a pipeline event
            },
            // 5 other events with full data included
        ]
    }
    ```

    In this case there were a total of 6 events that were made in on March 14 2022 available and all of them are included in the response.

## Fetch a single pipeline event

Using the single query to fetch the full data on a pipeline event with a known id.

- Query:
    `<ADAPTER_URI>/single?type=pipeline&uuid=6b64e62a-d881-5a09-9728-9ac7d2f5718f`

- Example Response:

    ```json
    {
        "id": "6b64e62a-d881-5a09-9728-9ac7d2f5718f",
        "type": "pipeline",
        "time": "2021-08-31T16:28:02.030+03:00",
        "message": "success",
        "duration": 1135,
        // all other attributes for a pipeline event
    }
    ```

## Fetch a single committer

Using the single query to fetch the full data on a committer author object with a known id.

- Query:
    `<ADAPTER_URI>/single?type=committer&uuid=8b9e99c7-5b43-5942-85a9-7ceecc63734c`

- Example Response:

    ```json
    {
        "id": "8b9e99c7-5b43-5942-85a9-7ceecc63734c",
        "type": "committer",
        "name": "<committer name>",
        "description": "",
        "state": "active",
        // all other attributes for a committer author object
    }
    ```
