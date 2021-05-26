package visdom.adapter.gitlab.results

import org.bson.BsonDocument
import org.bson.BsonInt64
import org.bson.BsonNull
import org.apache.spark.sql.Row


case class CommitResult(
    projectName: String,
    committer: String,
    date: String,
    commitCount: Long
) {
    def toBson(): BsonDocument = {
        new BsonDocument(
            projectName,
            new BsonDocument(
                committer,
                new BsonDocument(
                    date,
                    new BsonInt64(commitCount)
                )
            )
        )
    }
}

object CommitResult {
    def fromRow(dataRow: Row): CommitResult = {
        CommitResult(
            dataRow.getString(0),
            dataRow.getString(1),
            dataRow.getString(2),
            dataRow.getLong(3)
        )
    }

    def addToBson(document: BsonDocument, result: CommitResult): BsonDocument = {
        val projectElement = document.get(result.projectName, new BsonNull) match {
            case projectDocument: BsonDocument => projectDocument
            case _ => new BsonDocument()
        }
        val committerElement = projectElement.get(result.committer, new BsonNull) match {
            case committerDocument: BsonDocument => committerDocument
            case _ => new BsonDocument()
        }
        document.append(
            result.projectName,
            projectElement.append(
                result.committer,
                committerElement.append(
                    result.date,
                    new BsonInt64(result.commitCount)
                )
            )
        )
    }

    def fromArrayToBson(resultArray: Array[CommitResult]): BsonDocument = {
        def fromArrayToBsonInternal(
            resultArrayInternal: Array[CommitResult],
            documentInternal: BsonDocument
        ): BsonDocument = {
            resultArrayInternal.headOption match {
                case Some(firstResult: CommitResult) => fromArrayToBsonInternal(
                    resultArrayInternal.tail, addToBson(documentInternal, firstResult)
                )
                case None => documentInternal
            }
        }

        fromArrayToBsonInternal(resultArray, new BsonDocument())
    }
}
