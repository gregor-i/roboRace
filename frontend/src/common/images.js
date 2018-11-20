function image(url) {
  const img = new Image
  img.src = url
  return img
}

const iconClose = image('/assets/ic-close.svg')
const iconGamerlist = image('/assets/ic-gamerlist.svg')
const iconReplayAnimation = image('/assets/ic-replay-animation.svg')

const players = [
  image('/assets/player1.svg'),
  image('/assets/player2.svg'),
  image('/assets/player3.svg'),
  image('/assets/player4.svg'),
  image('/assets/player5.svg'),
  image('/assets/player6.svg')
]

const playerStarts = [
  image('/assets/player1-start.svg'),
  image('/assets/player2-start.svg'),
  image('/assets/player3-start.svg'),
  image('/assets/player4-start.svg'),
  image('/assets/player5-start.svg'),
  image('/assets/player6-start.svg')
]

function player(index) {
  return players[index % 6]
}

function playerStart(index){
  return playerStarts[index % 6];
}

// https://materialdesignicons.com/icon/flag-variant-outline
const target = image('/assets/target.svg')

const tile = image('/assets/tile.svg')

const wallDown = image('/assets/wall-down.svg')
const wallDownRight = image('/assets/wall-down-right.svg')
const wallUpRight = image('/assets/wall-up-right.svg')

function wall(direction){
  switch (Object.keys(direction)[0]) {
    case 'Down' : return wallDown
    case 'UpRight' : return wallUpRight
    case 'DownRight' : return wallDownRight
  }
}

const actions = {
  MoveForward: image('/assets/action-move-forward.svg'),
  MoveBackward: image('/assets/action-move-backward.svg'),
// StepRight : image('/assets/action_StepRight.svg'),
// StepLeft : image('/assets/action_StepLeft.svg'),
  MoveTwiceForward: image('/assets/action-move-forward-twice.svg'),
  TurnRight: image('/assets/action-turn-right-60.svg'),
  TurnLeft: image('/assets/action-turn-left-60.svg'),
  UTurn: image('/assets/action-turn-left-180.svg'),
  Sleep: image('/assets/action-sleep.svg'),
}

function action(name) {
  return actions[name]
}

const trapStun = image('/assets/trap-stun.svg')
const trapTurnRight = image('/assets/trap-turn-right.svg')
const trapTurnLeft = image('/assets/trap-turn-left.svg')

module.exports = {
  player, playerStart, action,
  iconClose, iconGamerlist, iconReplayAnimation,
  trapStun, trapTurnLeft, trapTurnRight,
  target, tile,
  wallDown, wallDownRight, wallUpRight, wall
}
