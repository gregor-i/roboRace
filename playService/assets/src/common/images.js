function image(url){
    const img = new Image
    img.src = url
    return img
}

const iconClose = image('/assets/ic-close.svg')
const iconGamerlist = image('/assets/ic-gamerlist.svg')
const iconReplayAnimation = image('/assets/ic-replay-animation.svg')

const tile = image('/assets/tile.svg')

const wallDown = image('/assets/wall-down.svg')
const wallDownRight = image('/assets/wall-down-right.svg')
const wallUpRight = image('/assets/wall-up-right.svg')

const player1 = image('/assets/player1.svg')
const player2 = image('/assets/player2.svg')
const player3 = image('/assets/player3.svg')
const player4 = image('/assets/player4.svg')
const player5 = image('/assets/player5.svg')
const player6 = image('/assets/player6.svg')

// https://materialdesignicons.com/icon/flag-variant-outline
const target = image('/assets/target.svg')


function player(index){
  switch (index % 6) {
    case 0: return player1
    case 1: return player2
    case 2: return player3
    case 3: return player4
    case 4: return player5
    case 5: return player6
  }
}

const MoveForward = image('/assets/action-move-forward.svg')
const MoveBackward = image('/assets/action-move-backward.svg')
// const StepRight = image('/assets/action_StepRight.svg')
// const StepLeft = image('/assets/action_StepLeft.svg')
const MoveTwiceForward = image('/assets/action-move-forward-twice.svg')
const TurnRight = image('/assets/action-turn-right-60.svg')
const TurnLeft = image('/assets/action-turn-left-60.svg')
const UTurn = image('/assets/action-turn-left-180.svg')
const Sleep = image('/assets/action-sleep.svg')

function action(name) {
  switch (name){
    case 'MoveForward':      return MoveForward
    case 'MoveBackward':     return MoveBackward
    // case 'StepRight':        return StepRight
    // case 'StepLeft':         return StepLeft
    case 'MoveTwiceForward': return MoveTwiceForward
    case 'TurnRight':        return TurnRight
    case 'TurnLeft':         return TurnLeft
    case 'UTurn':            return UTurn
    case 'Sleep':            return Sleep
  }
}

module.exports = {
  player, target, action,
  iconClose, iconGamerlist, iconReplayAnimation,
  tile, wallDown, wallUpRight, wallDownRight
}
