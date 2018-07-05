const _ = require('lodash')

const defaultHeader = {
  credentials: "same-origin"
}

function header(additional){
  return _.merge({}, defaultHeader, additional)
}

module.exports = header