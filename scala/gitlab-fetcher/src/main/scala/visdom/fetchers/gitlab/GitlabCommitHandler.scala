// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.SECONDS
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonArray
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.GeneralUtils


abstract class GitlabCommitLinkHandler(options: GitlabCommitLinkOptions) extends GitlabDataHandler(options)

class GitlabCommitHandler(options: GitlabCommitOptions)
    extends GitlabDataHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypeCommits
    def getCollectionName(): String = MongoConstants.CollectionCommits

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            GitlabConstants.AttributeReference -> options.reference,
            GitlabConstants.AttributeIncludeStatistics -> options.includeStatistics,
            GitlabConstants.AttributeIncludeLinksFiles -> options.includeFileLinks,
            GitlabConstants.AttributeIncludeLinksRefs -> options.includeReferenceLinks,
            GitlabConstants.AttributeUseAnonymization -> options.useAnonymization
        )
        .appendOption(
            GitlabConstants.AttributeStartDate,
            options.startDate.map(dateValue => toBsonValue(dateValue))
        )
        .appendOption(
            GitlabConstants.AttributeEndDate,
            options.endDate.map(dateValue => toBsonValue(dateValue))
        )
        .appendOption(
            GitlabConstants.AttributeFilePath,
            options.filePath.map(stringValue => toBsonValue(stringValue))
        )
    }

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits
        ).mkString("/")

        val commitRequest: HttpRequest = processOptionalParameters(
            Http(uri)
                .param(GitlabConstants.ParamRefName, options.reference)
                .param(GitlabConstants.ParamWithStats, options.includeStatistics.toString())
        )
        options.hostServer.modifyRequest(commitRequest)
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributeId,
            GitlabConstants.AttributeProjectName,
            GitlabConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(GitlabConstants.AttributeAuthorName),
                    Seq(GitlabConstants.AttributeAuthorEmail),
                    Seq(GitlabConstants.AttributeCommitterName),
                    Seq(GitlabConstants.AttributeCommitterEmail),
                    Seq(GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeProjectName)
                )
            )
            case false => None
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val documentWithMetadata: BsonDocument = addIdentifierAttributes(document).append(
            GitlabConstants.AttributeMetadata, getMetadata()
        )
        getLinkData(document) match {
            case Some(linkDocument: BsonDocument) => documentWithMetadata.append(
                GitlabConstants.AttributeLinks, linkDocument
            )
            case None => documentWithMetadata
        }
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(GitlabConstants.AttributeIncludeStatistics, toBsonValue(options.includeStatistics))
            .append(GitlabConstants.AttributeIncludeLinksFiles, toBsonValue(options.includeFileLinks))
            .append(GitlabConstants.AttributeIncludeLinksRefs, toBsonValue(options.includeReferenceLinks))
            .append(GitlabConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
    }

    def getLinkData(document: BsonDocument): Option[BsonDocument] = {
        document.getStringOption(GitlabConstants.AttributeId) match {
            case Some(commitId: String) => collectData(Seq(
                (GitlabConstants.AttributeFiles, options.includeFileLinks match {
                    case true => fetchLinkData(GitlabCommitDiff, commitId)
                    case false => None
                }),
                (GitlabConstants.AttributeRefs, options.includeReferenceLinks match {
                    case true => fetchLinkData(GitlabCommitRefs, commitId)
                    case false => None
                })
            ))
            case None => None
        }
    }

    def collectData(documentData: Seq[(String, Option[Array[Document]])]): Option[BsonDocument] = {
        def collectDataInternal(
            documentInternal: Option[BsonDocument],
            dataInternal: Seq[(String, Option[BsonArray])]
        ): Option[BsonDocument] = {
            dataInternal.headOption match {
                case Some(dataElement: (String, Option[BsonArray])) => collectDataInternal(
                    (dataElement._2 match {
                        case Some(actualData: BsonArray) => documentInternal match {
                            case Some(internalDocument: BsonDocument) =>
                                Some(internalDocument.append(dataElement._1, actualData))
                            case None =>
                                Some(new BsonDocument(dataElement._1, actualData))
                        }
                        case None => documentInternal
                    }),
                    dataInternal.drop(1)
                )
                case None => documentInternal
            }
        }

        collectDataInternal(
            None,
            documentData.map(
                documentElement => (
                    documentElement._1,
                    documentElement._2 match {
                        case Some(documentArray) => Some(
                            toBsonArray(documentArray.map(document => document.toBsonDocument))
                        )
                        case None => None
                    }
                )
            )
        )
    }

    private def processOptionalParameters(request: HttpRequest): HttpRequest = {
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var paramMap: Seq[(String, String)] = Seq.empty

        options.startDate match {
            case Some(startDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamSince,
                    startDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        options.endDate match {
            case Some(endDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamUntil,
                    endDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        options.filePath match {
            case Some(filePath: String) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamPath, filePath
                ))
            }
            case None =>
        }

        request.params(paramMap)
    }

    private def fetchLinkData(
        linkType: GitlabCommitLinkType,
        commitId: String
    ): Option[Array[Document]] = {
        val commitLinkOptions: GitlabCommitLinkOptions = GitlabCommitLinkOptions(
            hostServer = options.hostServer,
            mongoDatabase = None,
            projectName = options.projectName,
            commitId = commitId
        )
        val commitLinkFetcher: GitlabCommitLinkHandler = linkType match {
            case GitlabCommitDiff => new GitlabCommitDiffHandler(commitLinkOptions)
            case GitlabCommitRefs => new GitlabCommitRefsHandler(commitLinkOptions)
        }
        commitLinkFetcher.process()
    }
}
