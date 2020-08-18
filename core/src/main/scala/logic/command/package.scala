package logic

import entities.{Game, RejectionReason}

package object command {
  type CommandResponse = Either[RejectionReason, Game]
}
