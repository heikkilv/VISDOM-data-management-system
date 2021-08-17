package visdom.fetchers.aplus

import org.mongodb.scala.MongoDatabase
import visdom.fetchers.FetchOptions


abstract class APlusFetchOptions
extends FetchOptions {
    val hostServer: APlusServer
}

abstract class CourseSpecificFetchOptions {
    val courseId: Option[Int]
}

final case class CourseSpecificFetchParameters(
    courseId: Option[Int]
)
extends CourseSpecificFetchOptions

final case class APlusCourseOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Option[Int]
) extends APlusFetchOptions
