package visdom.utils.metadata

import org.mongodb.scala.bson.BsonDocument
import visdom.fetchers.aplus.FetcherValues
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


object APlusMetadata {
    val courseMetadata: Map[Int, CourseMetadata] =
        JsonUtils.getBsonDocumentFromFile(FetcherValues.AdditionalMetadataFilename) match {
            case Some(metadataDocument: BsonDocument) =>
                metadataDocument.getDocumentOption(AttributeConstants.Courses) match {
                    case Some(coursesDocument: BsonDocument) => {
                        coursesDocument.toIntDocumentMap()
                            .mapValues(courseDocumentValue => CourseMetadata.toCourseMetadata(courseDocumentValue))
                    }
                    case None => Map.empty  // no "courses" attribute found in the document
                }
            case None => Map.empty  // no JSON document could be parsed from the file
        }

    val moduleMetadata: Map[(Int, Int), ModuleMetadata] = {
        courseMetadata
            .mapValues(metadata => metadata.moduleMetadata.toSeq)
            .toSeq
            .map({
                case (courseId, moduleMetadataMap) => moduleMetadataMap.map({
                    case (moduleId, moduleMetadata) => ((courseId, moduleId), moduleMetadata)
                })
            })
            .flatten
            .toMap
    }

    val exerciseGitLocation: Map[(Int, Int), ExerciseGitLocation] =
        // mapping from courseId-exerciseId pairing to the Git location
        moduleMetadata
            .mapValues(metadata => metadata.exerciseGitLocations.toSeq)
            .toSeq
            .map({
                case ((courseId, _), locationSeq) =>
                    locationSeq.map({
                        case (exerciseId, location) => ((courseId, exerciseId), location)
                    })
            })
            .flatten
            .toMap
}
