package visdom.json

import spray.json.JsObject


trait JsonObjectConvertible {
    def toJsObject(): JsObject
}
