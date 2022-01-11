package visdom.adapters.results


final case class ResultCounts(
    count: Int,
    totalCount: Int,
    page: Int,
    pageSize: Int
) {
    val previousPage: Option[Int] = page > 1 match {
        case true => Some(page - 1)
        case false => None
    }
    val nextPage: Option[Int] = page * pageSize < totalCount match {
        case true => Some(page + 1)
        case false => None
    }
}

object ResultCounts {
    val DefaultPageSize: Int = 100
    val MinPageSize: Int = 1
    val MaxPageSize: Int = 10000

    def getSingleResultCounts(): ResultCounts = {
        ResultCounts(1, 1, 1, DefaultPageSize)
    }

    def getResultCounts(count: Int, totalCount: Int, page: Int, pageSize: Int): Option[ResultCounts] = {
        (
            count >= 0 &&
            totalCount >= count &&
            page >= 1 &&
            pageSize >= MinPageSize &&
            pageSize <= MaxPageSize
        ) match {
            case true => Some(ResultCounts(count, totalCount, page, pageSize))
            case false => None
        }
    }
}
