var h = require('snabbdom/h').default

function modal(content, onClose) {
        return h('div.modal.is-active', [
            h('div.modal-background', onClose ? {on: {click: onClose}}: null),
            h('div.modal-content', [
                h('div.box.content', content)
            ]),
             onClose ? h('button.modal-close.is-large', {on: {click: onClose}}) : null
        ])
}

module.exports = modal