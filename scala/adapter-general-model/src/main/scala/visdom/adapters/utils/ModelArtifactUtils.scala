package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import visdom.adapters.general.model.artifacts.ModulePointsArtifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.metadata.data.ModuleData
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.CoursePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ExercisePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ModuleAverageArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ModulePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.PipelineReportArtifactResult
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.FileIdSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.ModuleAverageSchema
import visdom.adapters.general.schemas.ModuleNumbersSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.PointsDifficultySchema
import visdom.adapters.general.schemas.PointsModuleSchema
import visdom.adapters.general.schemas.PointsPerCategory
import visdom.adapters.general.schemas.SubmissionGitDataSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants


class ModelArtifactUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newIntEncoder
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder
    import sparkSession.implicits.newStringEncoder

    def getFiles(): Dataset[FileArtifactResult] = {
        val allFiles: Dataset[FileSchema] =
            modelUtils.loadMongoDataGitlab[FileSchema](MongoConstants.CollectionFiles)
                .flatMap(row => FileSchema.fromRow(row))

        val fileParentMap: Map[(String, String, String), Seq[String]] =
            allFiles
                .map(
                    fileSchema => (
                        fileSchema,
                        GeneralUtils.getUpperFolder(fileSchema.path)
                    )
                )
                .filter(schemaWithParentFolder => schemaWithParentFolder._2 != CommonConstants.EmptyString)
                .groupByKey({case (schema, parentFolder) => (schema.host_name, schema.project_name, parentFolder)})
                .mapValues({case (schema, _) => Seq(schema.path)})
                .reduceGroups((first, second) => first ++ second)
                .collect()
                .toMap

        allFiles
            .map(
                fileSchema => ArtifactResult.fromFileSchema(
                    fileSchema,
                    fileSchema.`type` == SnakeCaseConstants.Tree match {
                        case true => fileParentMap.getOrElse(
                            (fileSchema.host_name, fileSchema.project_name, fileSchema.path),
                            Seq.empty
                        )
                        case false => Seq.empty
                    }
                )
            )
    }

    def getPipelineReports(): Dataset[PipelineReportArtifactResult] = {
        val projectNames: Map[Int, String] = modelUtils.getProjectNameMap()

        modelUtils.loadMongoDataGitlab[PipelineReportSchema](MongoConstants.CollectionPipelineReports)
            .flatMap(row => PipelineReportSchema.fromRow(row))
            // include only the reports that have a known project name
            .filter(report => projectNames.keySet.contains(report.pipeline_id))
            .map(
                reportSchema =>
                    ArtifactResult.fromPipelineReportSchema(
                        reportSchema,
                        projectNames.getOrElse(reportSchema.pipeline_id, CommonConstants.EmptyString)
                    )
            )
    }

    def getCoursePoints(): Dataset[CoursePointsArtifactResult] = {
        val courseMetadata: Map[Int, CourseSchema] =
            modelUtils.getCourseSchemas()
                .map(course => (course.id, course))
                .collect()
                .toMap

        modelUtils.getPointsSchemas()
            .map(points => ArtifactResult.fromCoursePointsSchema(points, courseMetadata.get(points.course_id)))
    }

    def getModuleExerciseCountMap(): Map[(Int, Int), Int] = {
        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => (
                        (points.id, module.id),
                        module.exercises.count(exercise => exercise.submission_count > 0)
                    )
                )
            )
            .collect()
            .toMap
    }

    def getModulePointsInformation(): Dataset[(Int, String, Int, Int, PointsModuleSchema, ModuleSchema)] = {
        val moduleMetadataMap: Map[Int, ModuleSchema] =
            modelUtils.getModuleSchemas()
                .map(module => (module.id, module))
                .collect()
                .toMap

        val moduleExerciseCount: Map[(Int, Int), Int] = getModuleExerciseCountMap()
        val moduleCommitCount: Map[(Int, Int), Int] = getModuleCommitCountMap()

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => moduleMetadataMap.get(module.id).map(
                        moduleMetadata => (
                            points.id,
                            points.metadata.last_modified,
                            moduleExerciseCount.getOrElse((points.id, module.id), 0),
                            moduleCommitCount.getOrElse((points.id, module.id), 0),
                            module,
                            moduleMetadata
                        )
                    )
                )
            )
            .flatMap(option => option)
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getModuleValuesMap(): Map[(Int, Int), Map[Int, (Int, ModuleNumbersSchema)]] = {
        // returns a map from (userId, courseId, moduleId) to (moduleNumber, moduleValues)
        getModulePointsInformation()
            .map({
                case (userId, _, exerciseCount, commitCount, module, moduleMetadata) => (
                    userId,
                    moduleMetadata.course_id,
                    moduleMetadata.id,
                    ModuleData.getModuleNumber(moduleMetadata.display_name),
                    ModuleNumbersSchema(
                        point_count = module.points_by_difficulty,
                        exercise_count = exerciseCount,
                        submission_count = module.submission_count,
                        commit_count = commitCount
                    )
                )
            })
            .groupByKey({case (userId, courseId, _, _, _) => (userId, courseId)})
            .mapGroups({
                case ((userId, courseId), modules) => (
                    (userId, courseId),
                    modules.toSeq.map({
                        case (_, _, moduleId, moduleNumber, moduleValues) => (moduleId, (moduleNumber, moduleValues))
                    })
                )
            })
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
            .map({case ((userId, courseId), modules) => ((userId, courseId), modules.toMap)})
    }

    def getCumulativeValuesMap(): Map[(Int, Int, Int), ModuleNumbersSchema] = {
        // returns a map from (userId, courseId, moduleNumber) to cumulative moduleValues
        val moduleValuesMap: Map[(Int, Int),Map[Int,(Int, ModuleNumbersSchema)]] = getModuleValuesMap()

        getModulePointsInformation()
            .map({
                case (userId, _, _, _, _, moduleMetadata) => (
                    userId,
                    moduleMetadata.course_id,
                    ModuleData.getModuleNumber(moduleMetadata.display_name),
                )
            })
            .distinct()
            .map({
                case (userId, courseId, moduleNumber) => (
                    (userId, courseId, moduleNumber),
                    moduleValuesMap
                        .getOrElse((userId, courseId), Map.empty)
                        .filter({case (moduleId, (mapModuleNumber, _)) => mapModuleNumber <= moduleNumber})
                        .map({case (_, (_, moduleValues)) => moduleValues})
                        .reduceOption((first, second) => first.add(second))
                        .getOrElse(ModuleNumbersSchema.getEmpty())
                )
            })
            .collect()
            .toMap
    }

    def getModulePoints(): Dataset[ModulePointsArtifactResult] = {
        val cumulativeValuesMap: Map[(Int, Int, Int), ModuleNumbersSchema] = getCumulativeValuesMap()

        getModulePointsInformation()
            .map({
                case (userId, lastModified, exerciseCount, commitCount, module, moduleMetadata) =>
                    ArtifactResult.fromModulePointsSchema(
                        modulePointsSchema = module,
                        moduleSchema = moduleMetadata,
                        userId = userId,
                        exerciseCount = exerciseCount,
                        commitCount = commitCount,
                        cumulativeValues = cumulativeValuesMap.getOrElse(
                            (
                                userId,
                                moduleMetadata.course_id,
                                ModuleData.getModuleNumber(moduleMetadata.display_name)
                            ),
                            ModuleNumbersSchema.getEmpty()
                        ),
                        updateTime = lastModified
                    )
            })
    }

    def getSubmissionFileMap(): Map[Int, FileIdSchema] = {
        // returns a mapping from submission id to file for those submissions that have GitLab content
        val exerciseGitMap: Map[Int, String] = modelUtils.getExerciseGitMap()

        modelUtils.getSubmissionSchemas()
            .flatMap(
                submission => submission.submission_data.map(submissionData => submissionData.git).flatten match {
                    case Some(gitData: SubmissionGitDataSchema) =>
                        exerciseGitMap.get(submission.exercise.id) match {
                            case Some(gitPath: String) => Some(
                                (
                                    submission.id,
                                    FileIdSchema(
                                        path = gitPath,
                                        project_name = gitData.project_name,
                                        host_name = gitData.host_name
                                    )
                                )
                            )
                            case None => None
                        }
                    case None => None
                }
            )
            .collect()
            .toMap
    }

    def getFileCommitMap(fileIds: Seq[FileIdSchema]): Map[FileIdSchema, Seq[ItemLink]] = {
        // returns a mapping from files to a list of commit event links
        modelUtils.loadMongoDataGitlab[FileSchema](MongoConstants.CollectionFiles)
            .flatMap(row => FileSchema.fromRow(row))
            .map(
                file => (
                    FileIdSchema(
                        path = file.path,
                        project_name = file.project_name,
                        host_name = file.host_name
                    ),
                    file
                )
            )
            .filter(fileWithIdSchema => fileIds.contains(fileWithIdSchema._1))
            .map({
                case (fileIdSchema, file) => (
                    fileIdSchema,
                    file._links.map(
                        links => links.commits.map(
                            commits => commits.map(
                                commit => ItemLink(
                                    CommitEvent.getId(file.host_name, file.project_name, commit),
                                    CommitEvent.CommitEventType
                                )
                            )
                        )
                    ).flatten match {
                        case Some(commitEventIds: Seq[ItemLink]) => commitEventIds
                        case None => Seq.empty
                    }
                )
            })
            .collect()
            .toMap
    }

    def getSubmissionCommitMap(): Map[Int, Seq[ItemLink]] = {
        // returns a mapping from submission id to a list of commit event links
        val submissionFileMap: Map[Int, FileIdSchema] = getSubmissionFileMap()
        val allSubmissionFiles: Seq[FileIdSchema] = submissionFileMap.map({case (_, file) => file}).toSeq.distinct
        val fileCommitMap: Map[FileIdSchema, Seq[ItemLink]] = getFileCommitMap(allSubmissionFiles)

        submissionFileMap
            .map({
                case (submissionId, fileIdSchema) => (
                    submissionId,
                    fileCommitMap.get(fileIdSchema) match {
                        case Some(commits: Seq[ItemLink]) => commits
                        case None => Seq.empty
                    }
                )
            })
            .filter({case (_, commits) => commits.nonEmpty})
    }

    def getModuleCommitCountMap(): Map[(Int, Int), Int] = {
        // returns a mapping from (userId, moduleId) to commit count
        val submissionCommitMap: Map[Int, Seq[String]] = getSubmissionCommitMap()
            .map({case (submissionId, commits) => (submissionId, commits.map(commitLink => commitLink.id))})

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => (
                        (points.id, module.id),
                        module.exercises.flatMap(
                            exercise => exercise.submissions_with_points
                                .map(submission => submissionCommitMap.getOrElse(submission.id, Seq.empty))
                                .flatten
                                .distinct
                        )
                        .distinct
                        .size
                    )
                )
            )
            .collect()
            .toMap
    }

    def getExercisePoints(): Dataset[ExercisePointsArtifactResult] = {
        val exerciseMetadataMap: Map[Int, ExerciseSchema] =
            modelUtils.getExerciseSchemas()
                .map(exercise => (exercise.id, exercise))
                .collect()
                .toMap
        val exerciseAdditionalMap: Map[Int, ExerciseAdditionalSchema] = modelUtils.getExerciseAdditionalMap()

        val submissionCommitMap: Map[Int, Seq[ItemLink]] = getSubmissionCommitMap()

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => module.exercises.map(
                        exercise => exerciseMetadataMap.get(exercise.id).map(
                            exerciseMetadata => (
                                points.id,
                                points.metadata.last_modified,
                                module.id,
                                exercise,
                                exerciseMetadata,
                                exerciseAdditionalMap.getOrElse(exercise.id, ExerciseAdditionalSchema.getEmpty()),
                                exercise.submissions_with_points
                                    .map(submission => submissionCommitMap.getOrElse(submission.id, Seq.empty))
                                    .flatten
                                    .distinct
                            )
                        )
                    )
                )
            )
            .flatMap(sequence => sequence)
            .flatMap(option => option)
            .map({
                case (userId, lastModified, moduleId, exercise, exerciseMetadata, exerciseAdditionalData, commits) =>
                    ArtifactResult.fromExercisePointsSchema(
                        exercisePointsSchema = exercise,
                        exerciseSchema = exerciseMetadata,
                        additionalSchema = exerciseAdditionalData,
                        moduleId = moduleId,
                        userId = userId,
                        relatedCommitEventLinks = commits,
                        updateTime = lastModified
                    )
            })
    }

    def getModuleIdToNumbers(): Dataset[((Int, Int), ModuleNumbersSchema)] = {
        val moduleMetadataMap: Map[Int, ModuleSchema] =
            modelUtils.getModuleSchemas()
                .map(module => (module.id, module))
                .collect()
                .toMap

        val moduleCommitCount: Map[(Int, Int), Int] = getModuleCommitCountMap()

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => moduleMetadataMap.get(module.id).map(
                        moduleMetadata => (
                            (
                                points.id,
                                moduleMetadata.id
                            ),
                            ModuleNumbersSchema(
                                point_count = module.points_by_difficulty,
                                exercise_count = module.exercises.count(exercise => exercise.submission_count > 0),
                                submission_count = module.submission_count,
                                commit_count = moduleCommitCount.getOrElse((points.id, module.id), 0)
                            )
                        )
                    )
                )
            )
            .flatMap(option => option)
    }

    def getModuleIdToWeekMap(): Map[Int, (Int, Int)] = {
        val moduleNumberMap: Map[Int, Int] = modelUtils.getModuleSchemas()
            .map(module => (module.id, ModuleData.getModuleNumber(module.display_name)))
            .collect()
            .toMap

        modelUtils.getCourseSchemas()
            .flatMap(course =>
                course._links
                    .map(links => links.modules.getOrElse(Seq.empty))
                    .getOrElse(Seq.empty)
                    .map(moduleId => (moduleId, (course.id, moduleNumberMap.getOrElse(moduleId, 0))))
            )
            .collect()
            .toMap
    }

    def getModuleNumbers(): Dataset[((Int, Int, Int), ModuleNumbersSchema)] = {
        val moduleIdToWeekMap: Map[Int, (Int, Int)] = getModuleIdToWeekMap()

        getModuleIdToNumbers()
            .flatMap({case ((userId, moduleId), numbers) =>
                moduleIdToWeekMap.get(moduleId)
                    .map({case (courseId, moduleNumber) => (
                        courseId,
                        moduleNumber,
                        userId,
                        numbers
                    )})
            })
            .filter(data => data._2 > 0)  // only include modules with moduleNumber > 0
            .groupByKey({case (courseId, moduleNumber, userId, _) => (courseId, moduleNumber, userId)})
            .mapValues({case (_, _, _, numbers) => numbers})
            .reduceGroups((first, second) => first.add(second))
    }

    def getUserTotalPointsMap(): Map[(Int, Int), PointsPerCategory] = {
        modelUtils.getPointsSchemas()
            .map(
                points => (
                    (
                        points.course_id,
                        points.id
                    ),
                    PointsPerCategory.fromPointsDifficultySchema(points.points_by_difficulty)
                )
            )
            .collect()
            .toMap
    }

    def getCourseMaxPointsMap(): Map[Int, PointsPerCategory] = {
        modelUtils.getPointsSchemas()
            .map(points => (points.course_id, points))
            .groupByKey({case (courseId, points) => courseId})
            .mapValues({case (_, points) => points})
            .reduceGroups((first, second) => first)  // take just the first points schema for each course
            .map({
                case (courseId, points) => (
                    courseId,
                    PointsPerCategory.fromPointsDifficultySchema(
                        points.modules.flatMap(
                            module => module.exercises.map(
                                exercise => PointsDifficultySchema.getSingle(exercise.difficulty, exercise.max_points)
                            )
                                .reduceOption((first, second) => first.add(second))
                        )
                            .reduceOption((first, second) => first.add(second))
                            .getOrElse(PointsDifficultySchema.getEmpty())
                    )
                )
            })
            .collect()
            .toMap
    }

    def getCourseGradeMap(): Map[(Int, Int), Int] = {
        val courseMaxPoints: Map[Int, PointsPerCategory] = getCourseMaxPointsMap()

        getUserTotalPointsMap()
            .flatMap({
                case ((courseId, userId), points) =>
                    courseMaxPoints.get(courseId)
                        .map(
                            maxPoints => (
                                (
                                    courseId,
                                    userId
                                ),
                                CourseUtils.getPredictedGrade(maxPoints, points)
                            )
                        )
            })
    }

    def getCourseUpdateTimeMap(): Map[Int, String] = {
        modelUtils.getPointsSchemas()
            .map(points => (points.course_id, points.metadata.last_modified))
            .groupByKey({case (courseId, _) => courseId})
            .mapValues({case (_, updateTime) => updateTime})
            .reduceGroups((first, second) =>
                first < second match {
                    case true => second
                    case false => first
                }
            )
            .collect()
            .toMap
    }

    def getWeekToModuleIdMap(): Map[(Int, Int), Seq[Int]] = {
        getModuleIdToWeekMap()
            .toSeq
            .groupBy({case (moduleId, (courseId, moduleNumber)) => (courseId, moduleNumber)})
            .map({
                case (key, values) => (
                    key,
                    values.map({case (moduleId, (courseId, moduleNumber)) => moduleId})
                )
            })
    }

    def getAvailableModuleAverages(
        courseSchemas: Map[Int, CourseSchema],
        courseUpdateTimes: Map[Int, String]
    ): Dataset[(CourseSchema, String, ModuleAverageSchema)] = {
        val courseGrades: Map[(Int, Int), Int] = getCourseGradeMap()

        getModuleNumbers()
            .flatMap({
                case ((courseId, moduleNumber, userId), numbers) =>
                    courseGrades.get((courseId, userId))
                        .map(grade => ((courseId, moduleNumber, grade), numbers))
            })
            .groupByKey({case (key, _) => key})
            .mapValues({case (_, numbers) => (1, numbers)})
            .reduceGroups((first, second) => (first._1 + second._1, first._2.add(second._2)))
            .filter(item => item._2._1 > 0)  // filter out any module-grade pairs without any students
            .flatMap({
                case ((courseId, moduleNumber, grade), (count, numberSum)) =>
                    courseSchemas.get(courseId).map(
                        course => (
                            course,
                            courseUpdateTimes.getOrElse(course.id, ModulePointsArtifact.DefaultEndTime),
                            ModuleAverageSchema(
                                module_number = moduleNumber,
                                grade = grade,
                                total = count,
                                avg_points = numberSum.point_count.total().toDouble / count,
                                avg_exercises = numberSum.exercise_count.toDouble / count,
                                avg_submissions = numberSum.submission_count.toDouble / count,
                                avg_commits = numberSum.commit_count.toDouble / count
                            )
                        )
                    )
            })
    }

    def getMissingModuleAverages(
        availableModuleAverages: Dataset[(CourseSchema, String, ModuleAverageSchema)],
        courseSchemas: Map[Int, CourseSchema],
        courseUpdateTimes: Map[Int, String]
    ): Dataset[(CourseSchema, String, ModuleAverageSchema)] = {
        val availableCourseGradeModules: Dataset[(Int, Int, Int)] = availableModuleAverages
            .map({
                case (course, _, moduleAverages) => (
                    course.id,
                    moduleAverages.grade,
                    moduleAverages.module_number
                )
            })
            .distinct()

        availableCourseGradeModules
            .map({case (courseId, _, moduleNumber) => (courseId, moduleNumber)})
            .distinct
            // collect the known modules for each course
            .groupByKey({case (courseId, _) => courseId})
            .mapValues({case (_, moduleNumber) => Seq(moduleNumber)})
            .reduceGroups((first, second) => first ++ second)
            // add all possible grade combinations
            .flatMap({
                case (courseId, modules) => modules.flatMap(
                    module => (CourseUtils.MinGrade to CourseUtils.MaxGrade).map(
                        grade => (courseId, grade, module)
                    )
                )
            })
            // take out the already known combinations
            .except(availableCourseGradeModules)
            .flatMap({
                case (courseId, grade, moduleNumber) => courseSchemas.get(courseId).map(
                    course => (
                        course,
                        courseUpdateTimes.getOrElse(courseId, ModulePointsArtifact.DefaultEndTime),
                        ModuleAverageSchema.getEmpty(moduleNumber, grade)
                    )
                )
            })
    }

    def getModuleAverages(): Dataset[ModuleAverageArtifactResult] = {
        val courseUpdateTimes: Map[Int, String] = getCourseUpdateTimeMap()
        val courseSchemas: Map[Int, CourseSchema] =
            modelUtils.getCourseSchemas()
                .map(course => (course.id, course))
                .collect()
                .toMap

        val weekToModuleIds: Map[(Int, Int), Seq[Int]] = getWeekToModuleIdMap()

        val availableModuleAverages: Dataset[(CourseSchema, String, ModuleAverageSchema)] =
            getAvailableModuleAverages(courseSchemas, courseUpdateTimes)

        val missingModuleAverages: Dataset[(CourseSchema, String, ModuleAverageSchema)] =
            getMissingModuleAverages(availableModuleAverages, courseSchemas, courseUpdateTimes)

        availableModuleAverages.union(missingModuleAverages)
            .map({
                case (courseSchema, updateTime, moduleAverageSchema) =>
                    ArtifactResult.fromModuleAverageSchema(
                        moduleAverageSchema = moduleAverageSchema,
                        courseSchema = courseSchema,
                        moduleIds = weekToModuleIds.getOrElse((courseSchema.id, moduleAverageSchema.module_number), Seq.empty),
                        updateTime = updateTime
                    )
            })
    }
}
