package visdom.adapters.general.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.options.QueryWithPageAndTokenOptions
import visdom.adapters.queries.BaseQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.model.TestEvent
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.results.Result
import visdom.adapters.utils.AdapterUtils
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.utils.QueryUtils.EnrichedDataSet
import visdom.utils.SnakeCaseConstants


class TestQuery(queryOptions: QueryWithPageAndTokenOptions, sparkSession: SparkSession)
extends BaseQuery(queryOptions, sparkSession) {
    import sparkSession.implicits.newProductEncoder

    def getEvents(): Dataset[TestEvent] = {
        MongoSpark
            .load[CommitSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionCommits
                )
            )
            .applyContainsFilter(SnakeCaseConstants.Message, queryOptions.token)
            .flatMap(row => CommitSchema.fromRow(row))
            .map(commitSchema => TestEvent.fromCommitSchema(commitSchema))
    }

    def getResults(): Result = {
        val events: Dataset[TestEvent] = getEvents()
        AdapterUtils.getResult(events, queryOptions, SnakeCaseConstants.Id)
    }
}

object TestQuery extends IncludesQueryCode {
    val queryCode: Int = 101
}
