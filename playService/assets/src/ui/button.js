var h = require('snabbdom/h').default

function button(actionHandler, classes, text, action) {
    return h(classes, {on: {click: [actionHandler, action]}}, text)
}

function primary(actionHandler, text, action) {
    return button(actionHandler, 'button.button.is-primary', text, action)
}

function normal(actionHandler, text, action) {
    return button(actionHandler, 'button.button', text, action)
}
function danger(actionHandler, text, action) {
    return button(actionHandler, 'button.button.is-danger', text, action)
}

module.exports = {
    primary, normal, danger
}