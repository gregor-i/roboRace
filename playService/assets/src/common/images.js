function image(url){
    const img = new Image
    img.src = url
    return img
}

const player1 = image('/assets/gem1.png')
const player2 = image('/assets/gem2.png')
const player3 = image('/assets/gem3.png')
const player4 = image('/assets/gem4.png')
const player5 = image('/assets/gem5.png')
const player6 = image('/assets/gem6.png')

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
    player
}