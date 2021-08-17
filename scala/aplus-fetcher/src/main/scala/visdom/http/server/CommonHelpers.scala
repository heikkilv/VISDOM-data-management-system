package visdom.http.server


object CommonHelpers {
    def isCourseId(courseIdOption: Option[String]): Boolean = {
        courseIdOption match {
            case Some(courseIdString: String) => {
                val courseId: Int =
                    try {
                        courseIdString.toInt
                    }
                    catch {
                        case _: NumberFormatException => -1
                    }
                courseId > 0
            }
            case None => true
        }
    }
}
