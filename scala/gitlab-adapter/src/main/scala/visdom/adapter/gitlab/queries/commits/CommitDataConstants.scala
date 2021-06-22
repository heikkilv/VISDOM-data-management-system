package visdom.adapter.gitlab.queries.commits


object CommitDataConstants {
    final val CommitDataRootPath = "/commits"
    final val CommitDataPath = "commits"

    final val CommitDataEndpointDescription =
        """Return the number of commits for each day grouped by GitLab project and the committer."""
    final val CommitDataEndpointSummary = "Return the number of commits per day per user per project."

    final val CommitDataStatusOkDescription = "The data successfully fetched"

    // the example responses for the commits endpoint
    final val ResponseExampleOkName = "Example response"
    final val CommitDataResponseExampleOk = """{
        "project-name-1": {
            "user-a": {
                "2021-05-11": 3,
                "2021-05-13": 5,
                "2021-05-17": 3
            },
            "user-b": {
                "2021-05-16": 2
            }
        },
        "project-name-2": {
            "user-c": {
                "2021-05-17": 1
            }
        }
    }"""
}
