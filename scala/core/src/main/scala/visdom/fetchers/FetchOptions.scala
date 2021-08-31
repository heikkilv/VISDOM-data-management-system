package visdom.fetchers

import org.mongodb.scala.MongoDatabase


abstract class FetchOptions {
    val hostServer: HostServer
    val mongoDatabase: Option[MongoDatabase]
}
