// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.usecases

import org.mongodb.scala.model.Filters
import visdom.adapters.general.AdapterValues
import visdom.adapters.options.AttributeFilterTrait
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.adapters.options.ObjectTypesTrait
import visdom.adapters.queries.BaseCacheQuery
import visdom.adapters.queries.BaseSparkQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.results.BaseResultValue
import visdom.adapters.utils.GeneralQueryUtils
import visdom.adapters.utils.ModelUtils
import visdom.adapters.utils.ModelUtilsTrait


class MultiQuery(queryOptions: MultiQueryOptions)
extends BaseCacheQuery(queryOptions) {
    val objectTypes: ObjectTypesTrait = ObjectTypes
    val modelUtilsObject: ModelUtilsTrait = ModelUtils
    val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[CacheUpdater]
    val generalQueryUtils: GeneralQueryUtils = AdapterValues.generalQueryUtils

    private val dataAttributes: Option[Seq[String]] = queryOptions.dataAttributes
    private val extraAttributes: Seq[String] =
        queryOptions.includedLinks.linkAttributes
            .filter({case (_, isIncluded) => !isIncluded})
            .map({case (attributeName, _) => attributeName})
            .toSeq

    def cacheCheck(): Boolean = {
        modelUtilsObject.isTargetCacheUpdated(queryOptions.targetType)
    }

    def updateCache(): (Class[_ <: BaseSparkQuery], BaseQueryOptions) = {
        (cacheUpdaterClass, CacheQueryOptions(queryOptions.targetType))
    }

    def getResults(): Option[BaseResultValue] = {
        val consideredObjects: Seq[String] = queryOptions.objectType match {
            case Some(objectType: String) => Seq(objectType)
            case None => objectTypes.objectTypes.get(queryOptions.targetType) match {
                case Some(objectTypes: Set[String]) => objectTypes.toSeq
                case None => Seq.empty
            }
        }

        consideredObjects.nonEmpty match {
            case true => Some(
                queryOptions.query match {
                    case Some(filters: Seq[AttributeFilterTrait]) => generalQueryUtils.getCacheResults(
                        objectTypes = consideredObjects,
                        pageOptions = queryOptions,
                        dataAttributes = dataAttributes,
                        extraAttributes = extraAttributes,
                        filter = Filters.and(filters.map(filter => filter.getFilter(consideredObjects)):_*)
                    )
                    case None => generalQueryUtils.getCacheResults(
                        objectTypes = consideredObjects,
                        pageOptions = queryOptions,
                        dataAttributes = dataAttributes,
                        extraAttributes = extraAttributes
                    )
                }
            )
            case false => None
        }
    }
}

object MultiQuery extends IncludesQueryCode {
    val queryCode: Int = 103
}
