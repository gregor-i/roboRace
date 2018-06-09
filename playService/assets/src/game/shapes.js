// left = 0
function x(angle, size){
  return -Math.cos(degree(angle)) *size
}
function y(angle, size){
  return -Math.sin(degree(angle)) * size
}
function degree(a) {
  return a * Math.PI / 180
}

function shapes(tile, wallFactor) {
  const th = tile / 2
  const wall = wallFactor * th * 2
  const wallCenter = wall / Math.sqrt(3)

  function wallShape(angle) {
    const w = new Path2D()
    w.moveTo(x(angle - 30, th), y(angle - 30, th))
    w.lineTo(x(angle + 30, th), y(angle + 30, th))
    w.lineTo(x(angle + 30, th + wallCenter), y(angle + 30, th + wallCenter))
    w.lineTo(x(angle + 30, th) + x(angle, wall), y(angle + 30, th) + y(angle, wall))
    w.lineTo(x(angle - 30, th) + x(angle, wall), y(angle - 30, th) + y(angle, wall))
    w.lineTo(x(angle - 30, th + wallCenter), y(angle - 30, th + wallCenter))
    w.closePath()
    return w
  }

  const hex = new Path2D()
  hex.moveTo(x(0, th), y(0, th))
  hex.lineTo(x(60, th), y(60, th))
  hex.lineTo(x(120, th), y(120, th))
  hex.lineTo(x(180, th), y(180, th))
  hex.lineTo(x(240, th), y(240, th))
  hex.lineTo(x(300, th), y(300, th))
  hex.closePath()

  return {
    hex,
    wallUpRight: wallShape(150),
    wallDownRight: wallShape(210),
    wallDown: wallShape(270)
  }
}

module.exports = shapes
