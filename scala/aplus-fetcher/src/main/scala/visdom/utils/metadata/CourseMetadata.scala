// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils.metadata

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import visdom.fetchers.aplus.APlusConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


final case class CourseMetadata(
    val lateSubmissionCoefficient: Option[Double],
    val gitBranch: Option[String],
    val moduleMetadata: Map[Int, ModuleMetadata]
) {
    def toBsonValue(): BsonValue = {
        BsonDocument()
        .appendOption(
            APlusConstants.AttributeLateSubmissionCoefficient,
            lateSubmissionCoefficient.map(doubleValue => JsonUtils.toBsonValue(doubleValue))
        )
        .appendOption(
            APlusConstants.AttributeGitBranch,
            gitBranch.map(stringValue => JsonUtils.toBsonValue(stringValue))
        )
    }
}

object CourseMetadata {
    def toCourseMetadata(courseDocument: BsonDocument): CourseMetadata = {
        CourseMetadata(
            lateSubmissionCoefficient = courseDocument.getDoubleOption(AttributeConstants.LateSubmissionCoefficient),
            gitBranch = courseDocument.getStringOption(AttributeConstants.GitBranch),
            moduleMetadata = courseDocument.getDocumentOption(AttributeConstants.Modules) match {
                case Some(moduleDocuments: BsonDocument) => {
                    moduleDocuments.toIntDocumentMap()
                        .mapValues(moduleDocumentValue => ModuleMetadata.toModuleMetadata(moduleDocumentValue))
                }
                case None => Map.empty
            }
        )
    }
}
