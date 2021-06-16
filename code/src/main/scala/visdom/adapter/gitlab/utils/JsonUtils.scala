package visdom.adapter.gitlab.utils

import scala.collection.immutable.SortedMap
import spray.json.JsObject
import spray.json.JsValue


object JsonUtils {
    def toJsObject(data: Array[(String, JsValue)]): JsObject = {
        JsObject(SortedMap(data:_*))
    }

    def toJsObject(data: Array[(String, String, JsValue)]): JsObject = {
        JsObject(
            SortedMap(data.groupBy(elements => elements._1)
                .mapValues(valueArray => toJsObject(
                    valueArray.map(elements => (elements._2, elements._3))
                ))
                .toSeq:_*
            )
        )
    }

    def toJsObject(data: Array[(String, String, String, JsValue)]): JsObject = {
        JsObject(
            SortedMap(data.groupBy(elements => elements._1)
                .mapValues(valueArray => toJsObject(
                    valueArray.map(elements => (elements._2, elements._3, elements._4))
                ))
                .toSeq:_*
            )
        )
    }
}
