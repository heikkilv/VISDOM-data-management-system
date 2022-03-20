package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import visdom.adapters.general.model.artifacts.ExercisePointsArtifact
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.model.events.data.SubmissionData
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.SubmissionSchema
import visdom.adapters.general.schemas.SubmissionLinksSchema
import visdom.adapters.general.schemas.SubmissionUserSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.TimeUtils


class SubmissionEvent(
    submissionSchema: SubmissionSchema,
    relatedConstructs: Seq[LinkTrait],
    relatedEvents: Seq[LinkTrait]
)
extends Event {
    def getType: String = SubmissionEvent.SubmissionEventType

    val origin: ItemLink =
        new AplusOrigin(
            submissionSchema.host_name,
            submissionSchema._links match {
                case Some(links: SubmissionLinksSchema) => links.courses.getOrElse(0)
                case None => 0
            },
            None
        ).link

    // use the first submitter in the submitter list as the author
    val author: ItemLink = ItemLink(
        AplusAuthor.getId(
            AplusOrigin.getId(submissionSchema.host_name),
            submissionSchema.submitters.headOption match {
                case Some(user: SubmissionUserSchema) => user.id
                case None => 0
            }
        ),
        AplusAuthor.AplusAuthorType
    )

    val data: SubmissionData = SubmissionData.fromSubmissionSchema(submissionSchema)
    val time: ZonedDateTime = TimeUtils.toZonedDateTimeWithDefault(submissionSchema.submission_time)
    val duration: Double = data.grading_time match {
        case Some(gradingTime: String) =>
            TimeUtils.getDifference(TimeUtils.toZonedDateTimeWithDefault(gradingTime), time)
        case None => 0.0
    }

    val message: String = s"Submission ${submissionSchema.id} for exercise ${submissionSchema.exercise.id}"

    val id: String = SubmissionEvent.getId(origin.id, data.submission_id)

    // add links to submitters and the grader
    addRelatedConstructs(
        (
            data.submitters ++
            (
                data.grader match {
                    case Some(graderId: Int) => Seq(graderId)
                    case None => Seq.empty
                }
            )
        ).map(userId => ItemLink(
            AplusAuthor.getId(AplusOrigin.getId(submissionSchema.host_name), userId),
            AplusAuthor.AplusAuthorType
        ))
    )

    // add links to the exercise points artifacts and exercise metadata
    addRelatedConstructs(
        data.submitters.map(
            userId => ItemLink(
                ExercisePointsArtifact.getId(origin.id, data.exercise_id, userId),
                ExercisePointsArtifact.ExercisePointsArtifactType
            )
        ) :+
        ItemLink(
            ExerciseMetadata.getId(origin.id, data.exercise_id),
            ExerciseMetadata.ExerciseMetadataType
        )
    )

    addRelatedConstructs(relatedConstructs)
    addRelatedEvents(relatedEvents)
}

object SubmissionEvent {
    final val SubmissionEventType: String = "submission"

    def fromSubmissionSchema(
        submissionSchema: SubmissionSchema,
        relatedConstructs: Seq[LinkTrait],
        relatedEvents: Seq[LinkTrait]
    ): SubmissionEvent = {
        new SubmissionEvent(submissionSchema, relatedConstructs, relatedEvents)
    }

    def getId(originId: String, submissionId: Int): String = {
        GeneralUtils.getUuid(originId, SubmissionEventType, submissionId.toString())
    }

    def getId(hostName: String, courseId: Int, submissionId: Int): String = {
        getId(
            originId = AplusOrigin.getId(hostName, courseId),
            submissionId = submissionId
        )
    }
}
