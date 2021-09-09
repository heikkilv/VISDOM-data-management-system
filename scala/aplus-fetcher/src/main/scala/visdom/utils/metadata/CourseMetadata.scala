package visdom.utils.metadata

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import visdom.fetchers.aplus.APlusConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


final case class CourseMetadata(
    val lateSubmissionCoefficient: Option[Double],
    val moduleMetadata: Map[Int, ModuleMetadata]
) {
    def toBsonValue(): BsonValue = {
        BsonDocument()
        .appendOption(
            APlusConstants.AttributeLateSubmissionCoefficient,
            lateSubmissionCoefficient.map(doubleValue => JsonUtils.toBsonValue(doubleValue))
        )
    }
}

object CourseMetadata {
    def toCourseMetadata(courseDocument: BsonDocument): CourseMetadata = {
        CourseMetadata(
            lateSubmissionCoefficient = courseDocument.getDoubleOption(AttributeConstants.LateSubmissionCoefficient),
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
