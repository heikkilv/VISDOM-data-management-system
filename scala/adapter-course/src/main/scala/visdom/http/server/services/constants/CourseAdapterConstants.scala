package visdom.http.server.services.constants


object CourseAdapterConstants {
    final val Username = "username"
    final val DescriptionUsername = "the username of the student"

    final val CourseId = "courseId"
    final val DescriptionCourseId = "the course id (must be a positive integer)"

    final val ExerciseId = "exerciseId"
    final val DescriptionExerciseId = "the exercise id (must be a positive integer)"

    final val IncludeFuture = "includeFuture"
    final val DescriptionIncludeFuture = "whether to include data from modules that have not been opened yet"
    final val DefaultIncludeFuture = "false"
}
