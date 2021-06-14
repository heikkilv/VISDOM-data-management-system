package visdom.adapter.gitlab.queries

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration


object Constants {
    final val FalseString = "false"
    final val TrueString = "true"
    final val BooleanStrings: Set[String] = Set(FalseString, TrueString)

    final val DateFormat = "date"

    final val StatusOkCode = "200"
    final val StatusInvalidCode = "400"
    final val StatusNotFoundCode = "404"
    final val StatusErrorCode = "500"

    final val QueryOk: String = "Ok"
    final val QueryInvalidStatus: String = "BadRequest"
    final val QueryNotFoundStatus: String = "NotFound"
    final val QueryErrorStatus: String = "InternalServerError"

    final val StatusInvalidDescription = "The request contained invalid or missing parameters"
    final val StatusNotFoundDescription = "No results found for the query"
    final val StatusErrorDescription = "Internal server error"

    final val ParameterProjectName = "projectName"
    final val ParameterUserName = "userName"
    final val ParameterStartDate = "startDate"
    final val ParameterEndDate = "endDate"

    final val ParameterDescriptionProjectName = "the GitLab project name"
    final val ParameterDescriptionUserName = "the user name"
    final val ParameterDescriptionStartDate = "the earliest date for the results given in ISO 8601 format"
    final val ParameterDescriptionEndDate = "the latest date for the results given in ISO 8601 format"

    final val ParameterExampleProjectName = "group/my-project-name"
    final val ParameterExampleUserName = "Example Developer"

    val DefaultWaitDurationSeconds: Int = 30
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)

    final val ResponseExampleInvalidName = "Invalid start time example"
    final val ResponseExampleInvalid = """{
        "status": "BadRequest",
        "description": "'2020-13-01' is not valid date in ISO 8601 format"
    }"""

    final val ResponseExampleNotFoundName = "No results found example"
    final val ResponseDefaultNotFound = "No results found for the query"
    final val ResponseExampleNotFound = """{
        "status": "NotFound",
        "description": "No results found for the query"
    }"""

    final val ResponseExampleErrorName = "Timeout response example"
    final val ResponseExampleError = """{
        "status": "InternalServerError",
        "description": "Futures timed out after [30 seconds]"
    }"""
}
