const iconClose = '/assets/ic-close.svg'
const iconGamerlist = '/assets/ic-gamerlist.svg'
const iconReplayAnimation = '/assets/ic-replay-animation.svg'

const tile = '/assets/tile.svg'

// https://materialdesignicons.com/icon/flag-variant-outline
const target = '/assets/target.svg'

const walls = {
  'Down': '/assets/wall-down.svg',
  'UpRight': '/assets/wall-up-right.svg',
  'DownRight': '/assets/wall-down-right.svg'
}

function wall(direction) {
  return walls[Object.keys(direction)[0]]
}

const players = [
  '/assets/player1.svg',
  '/assets/player2.svg',
  '/assets/player3.svg',
  '/assets/player4.svg',
  '/assets/player5.svg',
  '/assets/player6.svg'
]

function player(index) {
  return players[index % 6]
}

const playerStarts = [
  '/assets/player1-start.svg',
  '/assets/player2-start.svg',
  '/assets/player3-start.svg',
  '/assets/player4-start.svg',
  '/assets/player5-start.svg',
  '/assets/player6-start.svg'
]

function playerStart(index){
  return playerStarts[index % 6];
}

const actions = {
  MoveForward: '/assets/action-move-forward.svg',
  MoveBackward: '/assets/action-move-backward.svg',
// StepRight : '/assets/action_StepRight.svg',
// StepLeft : '/assets/action_StepLeft.svg',
  MoveTwiceForward: '/assets/action-move-forward-twice.svg',
  TurnRight: '/assets/action-turn-right-60.svg',
  TurnLeft: '/assets/action-turn-left-60.svg',
  UTurn: '/assets/action-turn-left-180.svg',
  Sleep: '/assets/action-sleep.svg',
}

function action(name) {
  return actions[name]
}

const trapStun = '/assets/trap-stun.svg'
const trapTurnRight = '/assets/trap-turn-right.svg'
const trapTurnLeft = '/assets/trap-turn-left.svg'

export const images = {
  player, playerStart, action, wall,
  iconClose, iconGamerlist, iconReplayAnimation,
  trapStun, trapTurnLeft, trapTurnRight,
  target, tile
}
