package visdom.fetchers.gitlab

import java.time.ZonedDateTime
import org.mongodb.scala.bson.BsonDocument
import scalaj.http.Http
import scalaj.http.HttpRequest
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.CommonConstants


class GitlabEventHandler(options: GitlabEventOptions)
    extends GitlabDataHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypeEvents
    def getCollectionName(): String = MongoConstants.CollectionEvents

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            GitlabConstants.AttributeUserId -> options.userId,
            GitlabConstants.AttributeUseAnonymization -> options.useAnonymization
        )
        .appendOption(
            GitlabConstants.AttributeActionType,
            options.actionType.map(stringValue => toBsonValue(stringValue))
        )
        .appendOption(
            GitlabConstants.AttributeTargetType,
            options.targetType.map(stringValue => toBsonValue(stringValue))
        )
        .appendOption(
            GitlabConstants.AttributeStartDate,
            options.startDate.map(stringValue => toBsonValue(stringValue))
        )
        .appendOption(
            GitlabConstants.AttributeEndDate,
            options.endDate.map(stringValue => toBsonValue(stringValue))
        )
    }

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/events.html#get-user-contribution-events
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathUsers,
            options.userId,
            GitlabConstants.PathEvents
        ).mkString(CommonConstants.Slash)

        val eventRequest: HttpRequest = Http(uri)
            .params({
                Seq(
                    (GitlabConstants.ParamAction, options.actionType.map(value => Left(value))),
                    (GitlabConstants.ParamTargetType, options.targetType.map(value => Left(value))),
                    (GitlabConstants.ParamAfter, options.startDate.map(value => Right(value))),
                    (GitlabConstants.ParamBefore, options.endDate.map(value => Right(value)))
                )
                    .map({
                        case (paramName, optionValue) => optionValue match {
                            case Some(paramValue: Either[String, ZonedDateTime]) => Some(
                                (
                                    paramName,
                                    paramValue match {
                                        case Left(stringValue: String) => stringValue
                                        case Right(dateValue: ZonedDateTime) => dateValue.toLocalDate().toString()
                                    }
                                )
                            )
                            case None => None
                        }
                    })
                    .flatten
                    .toMap
            })

        options.hostServer.modifyRequest(eventRequest)
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributeId,
            GitlabConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(GitlabConstants.AttributeAuthorUsername),
                    Seq(GitlabConstants.AttributeAuthor, GitlabConstants.AttributeUsername),
                    Seq(GitlabConstants.AttributeAuthor, GitlabConstants.AttributeName),
                    Seq(GitlabConstants.AttributeAuthor, GitlabConstants.AttributeAvatarUrl),
                    Seq(GitlabConstants.AttributeAuthor, GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeNote, GitlabConstants.AttributeAuthor, GitlabConstants.AttributeUsername),
                    Seq(GitlabConstants.AttributeNote, GitlabConstants.AttributeAuthor, GitlabConstants.AttributeName),
                    Seq(GitlabConstants.AttributeNote, GitlabConstants.AttributeAuthor, GitlabConstants.AttributeAvatarUrl),
                    Seq(GitlabConstants.AttributeNote, GitlabConstants.AttributeAuthor, GitlabConstants.AttributeWebUrl)
                )
            )
            case false => None
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        addIdentifierAttributes(document)
            .append(GitlabConstants.AttributeMetadata, getMetadata())
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(GitlabConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
    }
}
