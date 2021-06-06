package visdom.fetchers.gitlab.queries.commits


object CommitConstants {
    final val FalseString = "false"
    final val TrueString = "true"
    final val BooleanStrings: Set[String] = Set(FalseString, TrueString)

    final val CommitStatusAcceptedCode = "202"
    final val CommitStatusInvalidCode = "400"
    final val CommitStatusUnauthorizedCode = "401"
    final val CommitStatusNotFoundCode = "404"
    final val CommitStatusErrorCode = "500"

    final val CommitQueryAcceptedStatus: String = "Accepted"
    final val CommitQueryInvalidStatus: String = "BadRequest"
    final val CommitQueryUnauthorizedStatus: String = "Unauthorized"
    final val CommitQueryNotFoundStatus: String = "NotFound"
    final val CommitQueryErrorStatus: String = "InternalServerError"

    final val CommitRootPath = "/commits"
    final val CommitPath = "commits"

    final val DateTimeFormat = "date-time"

    final val CommitEndpointDescription = "Starts a fetching process for commit data from a GitLab repository."
    final val CommitEndpointSummary = "Fetch commit data from a GitLab repository."

    final val CommitStatusAcceptedDescription = "The fetching of the commit data has been started"
    final val CommitStatusInvalidDescription = "The request contained invalid or missing parameters"
    final val CommitStatusUnauthorizedDescription = "No access allowed to the wanted GitLab project"
    final val CommitStatusNotFoundDescription = "The asked project or reference was not found"
    final val CommitStatusErrorDescription = "Internal server error"

    final val ParameterProjectName = "projectName"
    final val ParameterReference = "reference"
    final val ParameterStartDate = "startDate"
    final val ParameterEndDate = "endDate"
    final val ParameterFilePath = "filePath"
    final val ParameterIncludeStatistics = "includeStatistics"
    final val ParameterIncludeFileLinks = "includeFileLinks"
    final val ParameterIncludeReferenceLinks = "includeReferenceLinks"

    final val ParameterDescriptionProjectName = "the GitLab project name"
    final val ParameterDescriptionReference = "the reference (branch or tag) for the project"
    final val ParameterDescriptionStartDate = "the earliest timestamp for the fetched commits given in ISO 8601 format with timezone"
    final val ParameterDescriptionEndDate = "the latest timestamp for the fetched commits given in ISO 8601 format with timezone"
    final val ParameterDescriptionFilePath = "the path for a file or folder to fetch commits for"
    final val ParameterDescriptionIncludeStatistics = "whether statistics information is included or not"
    final val ParameterDescriptionIncludeFileLinks = "whether file links information is included or not"
    final val ParameterDescriptionIncludeReferenceLinks = "whether reference links information is included or not"

    final val ParameterDefaultReference = "master"
    final val ParameterDefaultIncludeStatisticsString = FalseString
    final val ParameterDefaultIncludeFileLinksString = FalseString
    final val ParameterDefaultIncludeReferenceLinksString = FalseString

    final val ParameterExampleProjectName = "group/my-project-name"

    // the example responses and their names for the commits endpoint
    final val CommitResponseExampleAcceptedName = "Successful response example"
    final val CommitResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the commit data has been started",
        "options": {
            "projectName": "group/my-project-name",
            "reference": "master",
            "startDate": "2020-01-01T00:00:00.000Z",
            "endDate": "2021-01-01T00:00:00.000Z",
            "includeStatistics": "true",
            "includeFileLinks": "true",
            "includeReferenceLinks": "false"
        }
    }"""

    final val CommitResponseExampleInvalidName1 = "Missing project name example"
    final val CommitResponseExampleInvalid1 = """{
        "status": "BadRequest",
        "description": "''' is not a valid project name"
    }"""
    final val CommitResponseExampleInvalidName2 = "Invalid start time example"
    final val CommitResponseExampleInvalid2 = """{
        "status": "BadRequest",
        "description": "'2020-13-13T00:00' is not valid datetime in ISO 8601 format with timezone"
    }"""

    final val CommitResponseExampleUnauthorizedName = "Unauthorized response example"
    final val CommitResponseExampleUnauthorized = """{
        "status": "Unauthorized",
        "description": "Access to project 'example-project' not allowed"
    }"""

    final val CommitResponseExampleNotFoundName = "No project found example"
    final val CommitResponseExampleNotFound = """{
        "status": "NotFound",
        "description": "Project 'example-project' not found"
    }"""

    final val CommitResponseExampleErrorName = "Timeout response example"
    final val CommitResponseExampleError = """{
        "status": "InternalServerError",
        "description": "Futures timed out after [10 seconds]"
    }"""
}
