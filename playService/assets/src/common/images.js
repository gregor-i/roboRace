function image(url){
    const img = new Image
    img.src = url
    return img
}

// https://materialdesignicons.com/icon/apple-keyboard-caps
const player1 = image('/assets/player1.png')
const player2 = image('/assets/player2.png')
const player3 = image('/assets/player3.png')
const player4 = image('/assets/player4.png')
const player5 = image('/assets/player5.png')
const player6 = image('/assets/player6.png')

// https://materialdesignicons.com/icon/flag-variant-outline
const target = image('/assets/target.png')

// https://materialdesignicons.com/icon/alert-octagram
const pit = image('/assets/pit.png')

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

module.exports = {
    player, target, pit
}