var h = require('snabbdom/h').default

function frame(header, body, footer, modal) {
    return h('div.frame', [
        modal,
        h('div.content.frame-header', header),
        h('div.content.frame-body', body),
        h('div.content.frame-footer', footer)
    ])
}

module.exports = frame