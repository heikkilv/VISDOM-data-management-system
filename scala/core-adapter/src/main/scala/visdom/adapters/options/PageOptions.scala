package visdom.adapters.options


trait PageOptions {
    val page: Int
    val pageSize: Int
}

object PageOptions {
    final val DefaultPage: Int = 1
    final val DefaultPageSize: Int = 100

    final val MinPage: Int = 1
    final val MinPageSize: Int = 1
    final val MaxPageSize: Int = 10000
}
