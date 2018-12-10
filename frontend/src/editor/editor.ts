import {init} from 'snabbdom'

import {classModule} from 'snabbdom/modules/class'
import {propsModule} from 'snabbdom/modules/props'
import {styleModule} from 'snabbdom/modules/style'
import {attributesModule} from 'snabbdom/modules/attributes'
import {eventListenersModule} from 'snabbdom/modules/eventlisteners'

import {render} from './editor-ui'

import {actions} from './editor-actions'
import {loadSingleScenario} from './editor-service'

const patch = init([
  classModule,
  propsModule,
  styleModule,
  eventListenersModule,
  attributesModule
])

export function Editor(element, scenarioId) {
  let node = element

  function renderState(state) {
    node = patch(node, render(state, actionHandler(state)))
  }

  function actionHandler(state) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  loadSingleScenario(scenarioId).then((scenario) =>
    renderState({
      scenario: scenario.scenario,
      description: scenario.description,
      scenarioId: scenario.id,
      scenarioOwner: scenario.owner
    })
  ).catch(err => console.error(err))

  return this
}
