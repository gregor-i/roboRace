var h = require('snabbdom/h').default
var _ = require('lodash')

function builder(props) {
    var f = function (actionHandler, action, text) {
        return h('button.button', _.merge(props, {on: {click: [actionHandler, action]}}), text)
    }
    f.addProperty = function (p) {
        return builder(_.merge(props, p))
    }

    f.primary = function (bool) {
        return f.addProperty({class: {'is-primary': bool === undefined ? true : bool}})
    }
    f.danger = function (bool) {
        return f.addProperty({class: {'is-danger': bool === undefined ? true : bool}})
    }
    f.info = function (bool) {
        return f.addProperty({class: {'is-info': bool === undefined ? true : bool}})
    }
    f.disable = function (bool) {
        return f.addProperty({props: {disabled: bool === undefined ? true : bool}})
    }

    return f
}

function group() {
    var args = Array.prototype.slice.call(arguments);
    var wrappedInControl = args.map(function (button) {
        return h('span.control', button)
    })
    return h('div.field.has-addons', wrappedInControl)
}

module.exports = {
    group: group,
    builder: builder()
}
