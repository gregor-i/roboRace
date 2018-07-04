function image(url){
    const img = new Image
    img.src = url
    return img
}

const iconClose = image('/assets/ic-close.png')
const iconGamerlist = image('/assets/ic-gamerlist.png')
const iconReplayAnimation = image('/assets/ic-replay-animation.png')

const player1 = image('/assets/player1.png')
const player2 = image('/assets/player2.png')
const player3 = image('/assets/player3.png')
const player4 = image('/assets/player4.png')
const player5 = image('/assets/player5.png')
const player6 = image('/assets/player6.png')

// https://materialdesignicons.com/icon/flag-variant-outline
const target = image('/assets/target.png')


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

const MoveForward = image('/assets/action-move-forward.png')
const MoveBackward = image('/assets/action-move-backward.png')
// const StepRight = image('/assets/action_StepRight.png')
// const StepLeft = image('/assets/action_StepLeft.png')
const MoveTwiceForward = image('/assets/action-move-forward-twice.png')
const TurnRight = image('/assets/action-turn-right-60.png')
const TurnLeft = image('/assets/action-turn-left-60.png')
const UTurn = image('/assets/action-turn-left-180.png')
const Sleep = image('/assets/action-sleep.png')

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
  iconClose, iconGamerlist, iconReplayAnimation
}
