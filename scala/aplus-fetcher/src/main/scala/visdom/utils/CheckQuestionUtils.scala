package visdom.utils

import org.mongodb.scala.bson.BsonDocument
import scalaj.http.Http
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.fetchers.aplus.APlusConstants
import visdom.fetchers.aplus.FetcherValues


class CheckQuestionUtils(courseId: Int, exerciseId: Int, fieldName: String, acceptedAnswer: String) {
    final val AttributeExerciseId: String = "ExerciseID"
    final val AttributeUserId: String = "UserID"
    final val AttributeTime: String = "Time"

    val checkedUsers: Set[Int] = getCheckedUsers()

    private def getCheckedUsers(): Set[Int] = {
        HttpUtils.getRequestBody(
            FetcherValues.targetServer.modifyRequest(
                Http(
                    List(
                        FetcherValues.targetServer.baseAddress,
                        APlusConstants.PathCourses,
                        courseId.toString(),
                        APlusConstants.PathSubmissionData
                    ).mkString(CommonConstants.Slash) + CommonConstants.Slash
                )
                .param(APlusConstants.AttributeExerciseId, exerciseId.toString())
            ),
            HttpConstants.StatusCodeOk
        ) match {
            case Some(responseString: String) => parseCheckedUsers(
                HttpUtils.responseToDocumentArrayCaseArray(responseString)
            )
            case None => Set.empty
        }
    }

    private def parseCheckedUsers(documentArray: Array[BsonDocument]): Set[Int] = {
        documentArray
            // check that only data from the correct exercise is handled
            .filter(document => document.getIntOption(AttributeExerciseId) == Some(exerciseId))
            // pick only the 3 interesting fields: (user id, submission time, and submission value)
            .map(
                document => document.getIntOption(AttributeUserId) match {
                    case Some(userId: Int) => document.getStringOption(AttributeTime) match {
                        case Some(time: String) => document.getStringOption(fieldName) match {
                            case Some(answer: String) => Some(userId, time, answer)
                            case None => None
                        }
                        case None => None
                    }
                    case None => None
                }
            )
            // remove the entries that did not have all 3 fields included
            .flatten
            // group the entries by the user id
            .groupBy(data => data._1)
            // pick only the latest submission for each user
            .mapValues(data => data.sortBy(entry => entry._2).last)
            // remove the grouping since there is now only one entry per user
            .map(data => data._2)
            // remove the entries that don't have the acceptable answer
            .filter(data => data._3 == acceptedAnswer)
            // include only the user ids for the output
            .map(data => data._1)
            .toSet
    }
}

object CheckQuestionUtils {
    final val ExerciseIdForNoGdpr: Int = -1
}
