package visdom.adapters.results


final case class ResultCounts(
    count: Int,
    totalCount: Int,
    page: Int,
    pageSize: Int
) {
    val maxPage: Int = pageSize == 0 match {
        case true => 1
        case false => (totalCount - 1) / pageSize + 1
    }

    val previousPage: Option[Int] = page > 1 match {
        case true => Some(Math.min(page - 1, maxPage))
        case false => None
    }
    val nextPage: Option[Int] = page < maxPage match {
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

    def getEmpty(): ResultCounts = {
        ResultCounts(0, 1, 1, DefaultPageSize)
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
