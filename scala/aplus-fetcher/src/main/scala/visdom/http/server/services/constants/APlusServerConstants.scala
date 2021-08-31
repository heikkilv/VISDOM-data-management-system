package visdom.http.server.services.constants

import visdom.http.server.ServerConstants


object APlusServerConstants {
    final val CourseId = "courseId"
    final val ExerciseId = "exerciseId"
    final val GDPRAcceptedAnswer = "gdprAcceptedAnswer"
    final val GDPRExerciseId = "gdprExerciseId"
    final val GDPRFieldName = "gdprFieldName"
    final val IncludeExercises = "includeExercises"
    final val IncludeModules = "includeModules"
    final val IncludeSubmissions = "includeSubmissions"
    final val ModuleId = "moduleId"
    final val ParseNames = "parseNames"
    final val UseAnonymization = "useAnonymization"

    final val DefaultGDPRAcceptedAnswer = "a"
    final val DefaultGDPRFieldName = "field_0"
    final val DefaultIncludeExercises = ServerConstants.TrueString
    final val DefaultIncludeModules = ServerConstants.TrueString
    final val DefaultIncludeSubmissions = ServerConstants.FalseString
    final val DefaultParseNames = ServerConstants.TrueString
    final val DefaultUseAnonymization = ServerConstants.TrueString

    final val ParameterDescriptionCourseId = "The id number for the course instance. Must be a positive integer."
    final val ParameterDescriptionExerciseId =
        "The id number for the exercise in the chosen course module. Must be a positive integer."
    final val ParameterDescriptionGDPRAcceptedAnswer =
        "The only accepted answer for the GDPR question in order to be allowed to handle the users data."
    final val ParameterDescriptionGDPRExerciseId =
        "The id number for the exercise that contains the GDPR question. " +
        "Must be -1 if no GDPR question should be checked, otherwise it must be a positive integer.\n" +
        "If the parameter is not given, no submission data will be fetched regardless of other options."
    final val ParameterDescriptionGDPRFieldName =
        "The field name for the GDPR question that contains the users answer."
    final val ParameterDescriptionIncludeExercises = "Whether to also fetch detailed exercise metadata."
    final val ParameterDescriptionIncludeModules = "Whether to also fetch module metadata."
    final val ParameterDescriptionIncludeSubmissions = "Whether to also fetch all the submissions for the exercises."
    final val ParameterDescriptionModuleId =
        "The id number for the module in the chosen course instance. Must be a positive integer."
    final val ParameterDescriptionParseNames = "Whether to parse the module or exercise names."
    final val ParameterDescriptionUseAnonymization = "Whether to anonymize the user information."
}
