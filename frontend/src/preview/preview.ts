import {actions} from './preview-actions'
import {render} from './preview-ui'
import {PreviewState} from "../state";
import {ScenarioRow} from "../models";

import {patch} from '../common/snabbdom'

export function Preview(element, scenarioRow: ScenarioRow) {
  let node = element

  function renderState(state: PreviewState) {
    node = patch(node, render(state, actionHandler(state)))
  }

  function actionHandler(state: PreviewState) {
    return function (action) {
      const promise = actions(state, action)
      if (promise && promise.then)
        promise.then(renderState)
    }
  }

  renderState({
    scenarioRow: scenarioRow
  })

  return this
}