// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.base

import visdom.utils.GeneralUtils


trait RelatedItemsHandler
extends RelatedItemsTrait {
    private val events: scala.collection.mutable.Map[String, Seq[String]] = scala.collection.mutable.Map.empty
    private val constructs: scala.collection.mutable.Map[String, Seq[String]] = scala.collection.mutable.Map.empty

    def relatedEvents: Seq[LinkTrait] = {
        GeneralUtils.mapOfSeqToSeq(events)
            .map({case (eventType, eventId) => ItemLink(eventId, eventType)})
    }

    def relatedConstructs: Seq[LinkTrait] = {
        GeneralUtils.mapOfSeqToSeq(constructs)
            .map({case (constructType, constructId) => ItemLink(constructId, constructType)})
    }

    def addRelatedEvent(newEvent: LinkTrait): Unit = {
        addRelatedEvents(Seq(newEvent))
    }

    def addRelatedEvents(newEvents: Seq[LinkTrait]): Unit = {
        addRelatedItems(events, newEvents)
    }

    def addRelatedConstruct(newConstruct: LinkTrait): Unit = {
        addRelatedConstructs(Seq(newConstruct))
    }

    def addRelatedConstructs(newConstructs: Seq[LinkTrait]): Unit = {
        addRelatedItems(constructs, newConstructs)
    }

    def clearRelatedEvents(): Unit = {
        events.clear()
    }

    def clearRelatedConstructs(): Unit = {
        constructs.clear()
    }

    private def addRelatedItems(
        itemMap: scala.collection.mutable.Map[String, Seq[String]],
        newItems: Seq[LinkTrait]
    ): Unit = {
        newItems.headOption match {
            case Some(newEvent: LinkTrait) => {
                GeneralUtils.addItemToMapOfSeq(itemMap, newEvent.getType, newEvent.id)
                addRelatedItems(itemMap, newItems.drop(1))
            }
            case None =>
        }
    }
}
