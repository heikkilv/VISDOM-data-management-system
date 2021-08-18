package visdom.http.server.services.constants

import visdom.http.server.ServerConstants


object APlusServerConstants {
    final val CourseId = "courseId"
    final val ExerciseId = "exerciseId"
    final val IncludeExercises = "includeExercises"
    final val ModuleId = "moduleId"
    final val ParseNames = "parseNames"

    final val DefaultParseNames = ServerConstants.TrueString
    final val DefaultIncludeExercises = ServerConstants.TrueString

    final val ParameterDescriptionCourseId = "The id number for the course instance. Must be a positive integer."
    final val ParameterDescriptionModuleId =
        "The id number for the module in the chosen course instance. Must be a positive integer."
    final val ParameterDescriptionParseNames = "Whether to parse the module or exercise names."
    final val ParameterDescriptionIncludeExercises = "Whether to also fetch detailed exercise metadata."
}
