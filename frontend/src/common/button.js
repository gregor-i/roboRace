var h = require('snabbdom/h').default
var _ = require('lodash')

function builder(props) {
    var f = function (actionHandler, action, text) {
        return h('button.button.is-small', _.merge({}, props, {on: {click: [actionHandler, action]}}), text)
    }
    f.addProperty = function (p) {
        return builder(_.merge({}, props, p))
    }
    f.primary = function (bool) {
        return f.addProperty({class: {'is-primary': bool === undefined ? true : bool}})
    }
    f.disabled = function (bool) {
        return f.addProperty({props: {disabled: bool === undefined ? true : bool}})
    }
    return f
}

function group() {
    var args = Array.prototype.slice.call(arguments)
    var wrappedInControl = args.map((button) => h('span.control', button))
    return h('div.field.has-addons', wrappedInControl)
}

module.exports = {
    group: group,
    builder: builder(),
    primary: builder().primary(true)
}
