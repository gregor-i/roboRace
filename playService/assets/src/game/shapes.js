function shapes(k, th, deltaTop, deltaLeft, wall) {
  const cornerLeft_x = -th
  const cornerLeft_y = 0
  const cornerUpLeft_x = -0.5 * th
  const cornerUpLeft_y = -k * th
  const cornerUpRight_x = 0.5 * th
  const cornerUpRight_y = -k * th
  const cornerRight_x = th
  const cornerRight_y = 0
  const cornerDownRight_x = 0.5 * th
  const cornerDownRight_y = k * th
  const cornerDownLeft_x = -0.5 * th
  const cornerDownLeft_y = k * th

  const moveUpRight_x = deltaLeft
  const moveUpRight_y = -deltaTop / 2
  const moveDownRight_x = deltaLeft
  const moveDownRight_y = deltaTop / 2
  const moveDown_x = 0
  const moveDown_y = deltaTop

  const wallCenterRight_x = cornerRight_x +  wall / Math.sqrt(3)
  const wallCenterRight_y = cornerRight_y
  const wallCenterDownRight_x = cornerDownRight_x +  wall * k/ 3
  const wallCenterDownRight_y = cornerDownRight_y +  wall /2
  const wallCenterDownLeft_x = cornerDownLeft_x -  wall * k/ 3
  const wallCenterDownLeft_y = cornerDownLeft_y +  wall /2
  const wallCenterUpRight_x = cornerUpRight_x +  wall * k/ 3
  const wallCenterUpRight_y = cornerUpRight_y -  wall /2


  const hex = new Path2D()
  hex.moveTo(cornerLeft_x, cornerLeft_y)
  hex.lineTo(cornerUpLeft_x, cornerUpLeft_y)
  hex.lineTo(cornerUpRight_x, cornerUpRight_y)
  hex.lineTo(cornerRight_x, cornerRight_y)
  hex.lineTo(cornerDownRight_x, cornerDownRight_y)
  hex.lineTo(cornerDownLeft_x, cornerDownLeft_y)
  hex.closePath()

  const wallDown = new Path2D()
  wallDown.moveTo(cornerDownRight_x, cornerDownRight_y)
  wallDown.lineTo(cornerDownLeft_x, cornerDownLeft_y)
  wallDown.lineTo(wallCenterDownLeft_x, wallCenterDownLeft_y)
  wallDown.lineTo(cornerUpLeft_x + moveDown_x, cornerUpLeft_y + moveDown_y)
  wallDown.lineTo(cornerUpRight_x + moveDown_x, cornerUpRight_y + moveDown_y)
  wallDown.lineTo(wallCenterDownRight_x, wallCenterDownRight_y)
  wallDown.closePath()

  const wallDownRight = new Path2D()
  wallDownRight.moveTo(cornerDownRight_x, cornerDownRight_y)
  wallDownRight.lineTo(cornerRight_x, cornerRight_y)
  wallDownRight.lineTo(wallCenterRight_x, wallCenterRight_y)
  wallDownRight.lineTo(cornerUpLeft_x + moveDownRight_x, cornerUpLeft_y + moveDownRight_y)
  wallDownRight.lineTo(cornerLeft_x + moveDownRight_x, cornerLeft_y + moveDownRight_y)
  wallDownRight.lineTo(wallCenterDownRight_x, wallCenterDownRight_y)
  wallDownRight.closePath()

  const wallUpRight = new Path2D()
  wallUpRight.moveTo(cornerRight_x, cornerRight_y)
  wallUpRight.lineTo(cornerDownRight_x, cornerUpRight_y)
  wallUpRight.lineTo(wallCenterUpRight_x, wallCenterUpRight_y)
  wallUpRight.lineTo(cornerLeft_x + moveUpRight_x, cornerLeft_y + moveUpRight_y)
  wallUpRight.lineTo(cornerDownLeft_x + moveUpRight_x, cornerDownLeft_y + moveUpRight_y)
  wallUpRight.lineTo(wallCenterRight_x, wallCenterRight_y)
  wallUpRight.closePath()

  return {hex, wallDown, wallDownRight, wallUpRight}
}

module.exports = shapes