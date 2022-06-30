// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.database.mongodb.MongoConnection
import visdom.http.server.response.ComponentInfoResponse


trait Metadata {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def getMetadataDocument(): BsonDocument
    def getIdentifyingAttributes(): Array[String]
    def getInfoResponse(): ComponentInfoResponse

    def metadataDocumentWithTimestamp(): Document = {
        Document(
            getMetadataDocument()
                .append(
                    MetadataConstants.AttributeTimestamp,
                    BsonDateTime(Instant.now().toEpochMilli())
                )
        )
    }

    def storeMetadata(): Unit = {
        MongoConnection.storeDocument(
            MongoConnection.getMainMetadataCollection(),
            metadataDocumentWithTimestamp(),
            getIdentifyingAttributes()
        )
    }

    private val metadataTimer: Timer = new Timer()

    private val metadataTask: TimerTask = new TimerTask {
        def run() = {
            val metadataTask: Future[Unit] = Future(storeMetadata())
        }
    }

    def startMetadataTask(): Unit = {
        metadataTimer.schedule(
            metadataTask,
            MetadataConstants.MetadataInitialDelay,
            MetadataConstants.MetadataUpdateInterval
        )
    }

    def stopMetadataTask(): Unit = {
        val _ = metadataTask.cancel()
    }
}
