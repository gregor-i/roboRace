import {h} from 'snabbdom'

export function button(actionHandler, action, text) {
  return h('button.button.is-light', {on: {click: () => actionHandler(action)}}, text)
}

export function group(...buttons) {
  return h('div.field.has-addons', buttons.map((button) => h('span.control', button)))
}