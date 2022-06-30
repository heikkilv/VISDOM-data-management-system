// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import org.mongodb.scala.bson.BsonDocument
import scalaj.http.Http
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.fetchers.aplus.APlusConstants
import visdom.fetchers.aplus.FetcherValues
import visdom.fetchers.aplus.GdprOptions


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

    def getCheckedUsers(courseId: Int, gdprOptions: GdprOptions): Set[Int] = {
        gdprOptions.users match {
            case Some(userListSet: Set[Int]) => userListSet
            case None => gdprOptions.exerciseId match {
                // return an empty user id set for the case when no GDPR question is checked
                case CheckQuestionUtils.ExerciseIdForNoGdpr => Set.empty
                // only users that have given the acceptedAnswer will be included in the returned set
                case _ => new CheckQuestionUtils(
                    courseId = courseId,
                    exerciseId = gdprOptions.exerciseId,
                    fieldName = gdprOptions.fieldName,
                    acceptedAnswer = gdprOptions.acceptedAnswer
                ).checkedUsers
            }
        }
    }

    def getGdprOptionsDocument(gdprOptions: GdprOptions): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeExerciseId -> gdprOptions.exerciseId,
            APlusConstants.AttributeFieldName -> gdprOptions.fieldName,
            APlusConstants.AttributeAcceptedAnswer -> gdprOptions.acceptedAnswer
        )
    }

    def getUpdatedGdprOptions(gdprOptions: GdprOptions, users: Set[Int]): GdprOptions = {
        GdprOptions(
            exerciseId = gdprOptions.exerciseId,
            fieldName = gdprOptions.fieldName,
            acceptedAnswer = gdprOptions.acceptedAnswer,
            users = Some(users)
        )
    }

    def getUpdatedGdprOptions(gdprOptions: Option[GdprOptions], users: Set[Int]): Option[GdprOptions] = {
        gdprOptions match {
            case Some(options: GdprOptions) => Some(getUpdatedGdprOptions(options, users))
            case None => None
        }
    }

    implicit class EnrichedBsonDocumentWithGdpr(document: BsonDocument) {
        def appendGdprOptions(gdprOptions: GdprOptions): BsonDocument = {
            document.append(
                APlusConstants.AttributeGdprOptions,
                getGdprOptionsDocument(gdprOptions)
            )
        }

        def appendGdprOptions(gdprOptions: Option[GdprOptions]): BsonDocument = {
            document.appendOption(
                APlusConstants.AttributeGdprOptions,
                gdprOptions match {
                    case Some(gdprOptionsValue: GdprOptions) => Some(getGdprOptionsDocument(gdprOptionsValue))
                    case None => None
                }
            )
        }
    }
}
