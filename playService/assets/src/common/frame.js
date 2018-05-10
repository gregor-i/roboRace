var h = require('snabbdom/h').default

function frame(header, body, footer, modal) {
    return h('div.frame', [
        modal,
        h('div.frame-header', h('div.content', header)),
        h('div.frame-body', body),
        h('div.frame-footer', h('div.content', footer))
    ])
}

module.exports = frame