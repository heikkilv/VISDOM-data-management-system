package visdom.http

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration


object HttpConstants {
    // constants for HTTP status codes
    val StatusCodeOk: Int = 200
    val StatusCodeAccepted: Int = 202
    val StatusCodeUnauthorized: Int = 401
    val StatusCodeNotFound: Int = 404
    val StatusCodeInternalServerError: Int = 500
    val StatusCodeUnknown: Int = 0

    final val StatusOkCode = "200"
    final val StatusAcceptedCode = "202"
    final val StatusInvalidCode = "400"
    final val StatusUnauthorizedCode = "401"
    final val StatusNotFoundCode = "404"
    final val StatusErrorCode = "500"

    val CharacterSlash: String = "/"
    val Localhost: String = "localhost"

    // constants for constructing query paths
    val PathCommits: String = "commits"
    val PathDiff: String = "diff"
    val PathInfo: String = "info"
    val PathMulti: String = "multi"
    val PathProjects: String = "projects"
    val PathRefs: String = "refs"
    val PathRepository: String = "repository"
    val PathTree: String = "tree"

    final val HttpPrefix: String = "http://"
    final val HttpsPrefix: String = "https://"

    // the default wait time for HTTP queries
    val DefaultWaitDurationSeconds: Int = 30
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)

    val MaxUriLength: Int = 1024
    val FutureTaskDelayMs: Int = 3000
}
