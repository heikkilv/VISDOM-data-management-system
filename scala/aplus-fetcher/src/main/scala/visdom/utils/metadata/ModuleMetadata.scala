package visdom.utils.metadata

import java.time.ZonedDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import visdom.fetchers.aplus.APlusConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


final case class ModuleMetadata(
    val startDate: Option[ZonedDateTime],
    val endDate: Option[ZonedDateTime],
    val lateSubmissionDate: Option[ZonedDateTime],
    val exerciseGitLocations: Map[Int, ExerciseGitLocation]
) {
    def toBsonValue(): BsonValue = {
        BsonDocument()
        .appendOption(
            APlusConstants.AttributeStartDate,
            startDate.map(dateValue => JsonUtils.toBsonValue(dateValue))
        )
        .appendOption(
            APlusConstants.AttributeEndDate,
            endDate.map(dateValue => JsonUtils.toBsonValue(dateValue))
        )
        .appendOption(
            APlusConstants.AttributeLateSubmissionDate,
            lateSubmissionDate.map(dateValue => JsonUtils.toBsonValue(dateValue))
        )
    }
}

object ModuleMetadata {
    def toModuleMetadata(moduleDocument: BsonDocument): ModuleMetadata = {
        ModuleMetadata(
            startDate = moduleDocument.getZonedDateTimeOption(AttributeConstants.StartDate),
            endDate = moduleDocument.getZonedDateTimeOption(AttributeConstants.EndDate),
            lateSubmissionDate = moduleDocument.getZonedDateTimeOption(AttributeConstants.LateSubmissionDate),
            exerciseGitLocations = moduleDocument.getDocumentOption(AttributeConstants.ExerciseGitLocations) match {
                case Some(exercisesDocument: BsonDocument) => {
                    exercisesDocument.toIntStringMap()
                        .mapValues(locationString => ExerciseGitLocation.toExerciseGitLocation(locationString))
                }
                case None => Map.empty
            }
        )
    }
}
