// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services.constants


object CourseAdapterExamples {
    final val CourseAdapterInfoResponseExample = """{
        "componentName": "adapter-course",
        "componentType": "adapter",
        "adapterType": "Course",
        "version": "0.1",
        "startTime": "2021-09-01T12:00:00.000Z",
        "apiAddress": "localhost:8790",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""

    final val ResponseExampleOkName = "example response"
    final val DataResponseExampleOk = """{
        "results": [
            {
                "commits": [
                    {
                        "module_name": "01",
                        "projects": [
                            {
                                "name": "first",
                                "commit_count": 1,
                                "commit_meta": [
                                    {
                                        "hash": "qwertyuiopasdfghjklzxcvbnm",
                                        "message": "example commit message",
                                        "commit_date": "2020-01-01T12:00:00.000Z",
                                        "committer_email": "8ad125d39ece9c723a4472953f831073520da8a11a67ab173417803a5c677cba"
                                    }
                                ]
                            }
                        ]
                    }
                ],
                "email": "76974fd56f61d64b67fcad19628cdb237ded5e576d7c0b88d86d2a4cdc454d95",
                "full_name": "8ab585e7025862477fd41d2b4edaad802f8197fb60c3d01a2643f18f9f863a37",
                "id": 675187842,
                "is_external": false,
                "points": {
                    "points": 100,
                    "points_by_difficulty": {
                        "": 40,
                        "G": 20,
                        "P": 40
                    },
                    "submission_count": 5,
                    "modules": [
                        {
                            "id": 1234,
                            "max_points": 40,
                            "name": {
                                "en": "First week",
                                "fi": "Ensimmäinen viikko",
                                "number": "1.",
                                "raw": "1. |en:First week|fi:Ensimmäinen viikko|"
                            },
                            "passed": true,
                            "points": 40,
                            "points_by_difficulty": {
                                "": 40
                            },
                            "points_to_pass": 0,
                            "submission_count": 1,
                            "exercises": [
                                {
                                    "best_submission": "b23440fb5c09859b537b931aa66a17a125a571d07c16cc07c164d214b0e08d1c",
                                    "difficulty": "",
                                    "id": 112233,
                                    "max_points": 10,
                                    "name": {
                                        "en": "First",
                                        "fi": "Eka",
                                        "number": "1.2.1",
                                        "raw": "1.2.1 |fi:Eka|en:First|"
                                    },
                                    "official": true,
                                    "passed": true,
                                    "points": 10,
                                    "points_to_pass": 0,
                                    "submission_count": 1,
                                    "submissions": [
                                        "19cefd7f8cdf7c6b5be1ffe2446d30143a04d17f263475a4e835858e2b6e9142"
                                    ],
                                    "submissions_with_points": [
                                        {
                                            "grade": 10,
                                            "id": 117719610,
                                            "submission_time": "2020-09-18T15:02:17.006122Z"
                                        }
                                    ],
                                    "url": "https://plus.tuni.fi/api/v2/exercises/112233/?format=json"
                                }
                            ]
                        }
                    ]
                },
                "student_id": "325bf51df0ee884cb975f7d693975f5bafea8a9a08dc7d8fa7dee5cba3fadeee",
                "url": "f9b50e17ba73484996942bf669aa79ba69addd2e886a550d59ee1be4c88b155c",
                "username": "863bdff3f9d3228183f8e271c7a924d9ae09f4d9ba32c077981fdd889e9524ea"
            }
        ]
    }"""

    final val HistoryResponseExampleOk = """{
        "data_by_grades": {
            "0": {
                "student_count": 10,
                "avg_points": [10.0, 5.0],
                "avg_exercises": [1.0, 1.5],
                "avg_submissions": [1.5, 3.5],
                "avg_commits": [1.75, 3.75],
                "avg_cum_points": [10.0, 15.0],
                "avg_cum_exercises": [1.0, 2.5],
                "avg_cum_submissions": [1.5, 5.0],
                "avg_cum_commits": [1.75, 5.5]
            },
            "1": {
                "student_count": 25,
                "avg_points": [20.0, 25.0],
                "avg_exercises": [2.0, 2.5],
                "avg_submissions": [2.5, 4.5],
                "avg_commits": [2.75, 4.75],
                "avg_cum_points": [20.0, 45.0],
                "avg_cum_exercises": [2.0, 4.5],
                "avg_cum_submissions": [2.5, 6.0],
                "avg_cum_commits": [2.75, 7.5]
            }
        },
        "data_by_weeks": {
            "01": {
                "student_counts": [10, 25],
                "avg_points": [10.0, 20.0],
                "avg_exercises": [1.0, 2.0],
                "avg_submissions": [1.5, 2.5],
                "avg_commits": [1.75, 2.75],
                "avg_cum_points": [10.0, 20.0],
                "avg_cum_exercises": [1.0, 2.0],
                "avg_cum_submissions": [1.5, 2.5],
                "avg_cum_commits": [1.75, 2.75]
            },
            "02": {
                "student_counts": [10, 25],
                "avg_points": [5.0, 25.0],
                "avg_exercises": [1.5, 2.5],
                "avg_submissions": [3.5, 4.5],
                "avg_commits": [3.75, 4.75],
                "avg_cum_points": [15.0, 45.0],
                "avg_cum_exercises": [2.5, 4.5],
                "avg_cum_submissions": [5.0, 6.0],
                "avg_cum_commits": [5.5, 7.5]
            }
        }
    }"""

    final val UsernameResponseExampleOk = """{
        "results": [
            "863bdff3f9d3228183f8e271c7a924d9ae09f4d9ba32c077981fdd889e9524ea",
            "c6dd236aa217735693b327dfe01e3436623d4b7881ea9fd182c9e0d037601901"
        ]
    }"""

    final val ResponseExampleInvalidName = "invalid course id"
    final val ResponseExampleInvalid = """{
        "description": "Some query parameters contained invalid values",
        "status": "BadRequest"
    }"""
}
