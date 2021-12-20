package visdom.fetchers.gitlab.queries

object Constants {
    final val FalseString = "false"
    final val TrueString = "true"
    final val BooleanStrings: Set[String] = Set(FalseString, TrueString)

    final val StatusOkCode = "200"
    final val StatusAcceptedCode = "202"
    final val StatusInvalidCode = "400"
    final val StatusUnauthorizedCode = "401"
    final val StatusNotFoundCode = "404"
    final val StatusErrorCode = "500"

    final val QueryAcceptedStatus: String = "Accepted"
    final val QueryInvalidStatus: String = "BadRequest"
    final val QueryUnauthorizedStatus: String = "Unauthorized"
    final val QueryNotFoundStatus: String = "NotFound"
    final val QueryErrorStatus: String = "InternalServerError"

    final val DateFormat = "date"
    final val DateTimeFormat = "date-time"

    final val StatusInvalidDescription = "The request contained invalid or missing parameters"
    final val StatusUnauthorizedDescription = "No access allowed to the wanted GitLab project"
    final val StatusNotFoundDescription = "The asked project or reference was not found"
    final val StatusErrorDescription = "Internal server error"

    final val ParameterActionType = "actionType"
    final val ParameterDateAfter = "dateAfter"
    final val ParameterDateBefore = "dateBefore"
    final val ParameterProjectId = "projectId"
    final val ParameterProjectName = "projectName"
    final val ParameterProjectNames = "projectNames"
    final val ParameterReference = "reference"
    final val ParameterStartDate = "startDate"
    final val ParameterEndDate = "endDate"
    final val ParameterFilePath = "filePath"
    final val ParameterIncludeStatistics = "includeStatistics"
    final val ParameterIncludeFileLinks = "includeFileLinks"
    final val ParameterIncludeReferenceLinks = "includeReferenceLinks"
    final val ParameterIncludeCommitLinks = "includeCommitLinks"
    final val ParameterIncludeJobs = "includeJobs"
    final val ParameterIncludeJobLogs = "includeJobLogs"
    final val ParameterIncludeReports = "includeReports"
    final val ParameterRecursive = "recursive"
    final val ParameterTargetType = "targetType"
    final val ParameterUseAnonymization = "useAnonymization"
    final val ParameterUserId = "userId"

    final val ParameterDescriptionActionType = "include only events of a particular action type, https://docs.gitlab.com/ee/api/events.html#actions"
    final val ParameterDescriptionProjectId = "the GitLab project id"
    final val ParameterDescriptionProjectName = "the GitLab project name"
    final val ParameterDescriptionProjectNames = "a comma-separated list for GitLab project names"
    final val ParameterDescriptionReference = "the reference (branch or tag) for the project"
    final val ParameterDescriptionFilePath = "the path for a file or folder to fetch commits for"
    final val ParameterDescriptionFilePathForFiles = "the path inside repository to allow getting content of subdirectories"
    final val ParameterDescriptionIncludeStatistics = "whether statistics information is included or not"
    final val ParameterDescriptionIncludeFileLinks = "whether file links information is included or not"
    final val ParameterDescriptionIncludeReferenceLinks = "whether reference links information is included or not"
    final val ParameterDescriptionIncludeCommitLinks = "whether commit links information is included or not"
    final val ParameterDescriptionIncludeJobs = "whether to fetch related job data or not"
    final val ParameterDescriptionIncludeJobLogs = "whether job logs are included or not (only applicable when includeJobs is true)"
    final val ParameterDescriptionIncludeReports = "whether to include the pipeline test reports or not"
    final val ParameterDescriptionRecursive = "whether to use recursive search or not"
    final val ParameterDescriptionTargetType = "include only events of a particular target type, https://docs.gitlab.com/ee/api/events.html#target-types"
    final val ParameterDescriptionUseAnonymization = "whether to anonymize the user information"
    final val ParameterDescriptionUserId = "the ID or Username of the GitLab user "

    final val ParameterDefaultReference = "master"
    final val ParameterDefaultIncludeStatisticsString = FalseString
    final val ParameterDefaultIncludeFileLinksString = FalseString
    final val ParameterDefaultIncludeReferenceLinksString = FalseString
    final val ParameterDefaultIncludeCommitLinksString = FalseString
    final val ParameterDefaultIncludeJobsString = TrueString
    final val ParameterDefaultIncludeJobLogsString = FalseString
    final val ParameterDefaultIncludeReportsString = TrueString
    final val ParameterDefaultRecursiveString = TrueString
    final val ParameterDefaultUseAnonymization = TrueString
    final val ParameterExampleProjectId = "1234"
    final val ParameterExampleProjectName = "group/my-project-name"
    final val ParameterExampleProjectNames = "group/project-name1,group/project-name2"
    final val ParameterExampleUserId = "username"

    final val ActionTypeApproved = "approved"
    final val ActionTypeClosed = "closed"
    final val ActionTypeCommented = "commented"
    final val ActionTypeCreated = "created"
    final val ActionTypeDestroyed = "destroyed"
    final val ActionTypeExpired = "expired"
    final val ActionTypeJoined = "joined"
    final val ActionTypeLeft = "left"
    final val ActionTypeMerged = "merged"
    final val ActionTypePushed = "pushed"
    final val ActionTypeReopened = "reopened"
    final val ActionTypeUpdated = "updated"
    final val ActionTypes: Set[String] = Set(
        ActionTypeApproved, ActionTypeClosed, ActionTypeCommented, ActionTypeCreated, ActionTypeDestroyed,
        ActionTypeExpired, ActionTypeJoined, ActionTypeLeft, ActionTypeMerged, ActionTypePushed,
        ActionTypeReopened, ActionTypeUpdated
    )

    final val TargetTypeIssue = "issue"
    final val TargetTypeMilestone = "milestone"
    final val TargetTypeMergeRequest = "merge_request"
    final val TargetTypeNote = "note"
    final val TargetTypeProject = "project"
    final val TargetTypeSnippet = "snippet"
    final val TargetTypeUser = "user"
    final val TargetTypes: Set[String] = Set(
        TargetTypeIssue, TargetTypeMilestone, TargetTypeMergeRequest, TargetTypeNote,
        TargetTypeProject, TargetTypeSnippet, TargetTypeUser
    )

    // the example responses and their names for that can common for the various endpoints
    final val ResponseExampleAcceptedName = "Successful response example"

    final val ResponseExampleInvalidName1 = "Missing project name example"
    final val ResponseExampleInvalid1 = """{
        "status": "BadRequest",
        "description": "''' is not a valid project name"
    }"""
    final val ResponseExampleInvalidName2 = "Invalid start time example"
    final val ResponseExampleInvalid2 = """{
        "status": "BadRequest",
        "description": "'2020-13-13T00:00' is not valid datetime in ISO 8601 format with timezone"
    }"""
    final val ResponseExampleInvalidName3 = "Invalid project name list"
    final val ResponseExampleInvalid3 = """{
        "status": "BadRequest",
        "description": "'name1,,name3'' is not a valid comma-separated list of project names"
    }"""
    final val ResponseExampleInvalidName4 = "Invalid action type"
    final val ResponseExampleInvalid4 = """{
        "status": "BadRequest",
        "description": "'committed'' is not a valid value for actionType"
    }"""

    final val ResponseExampleUnauthorizedName = "Unauthorized response example"
    final val ResponseExampleUnauthorized = """{
        "status": "Unauthorized",
        "description": "Access to project 'example-project' not allowed"
    }"""
    final val ResponseExampleUnauthorizedName2 = "Unauthorized response example"
    final val ResponseExampleUnauthorized2 = """{
        "status": "Unauthorized",
        "description": "No available projects found. Access to projects 'group/secret' not allowed"
    }"""

    final val ResponseExampleNotFoundName = "No project found example"
    final val ResponseExampleNotFound = """{
        "status": "NotFound",
        "description": "Project 'example-project' not found"
    }"""
    final val ResponseExampleNotFoundName2 = "No projects found example"
    final val ResponseExampleNotFound2 = """{
        "status": "NotFound",
        "description": "No available projects found"
    }"""

    final val ResponseExampleErrorName = "Timeout response example"
    final val ResponseExampleError = """{
        "status": "InternalServerError",
        "description": "Futures timed out after [10 seconds]"
    }"""
}
