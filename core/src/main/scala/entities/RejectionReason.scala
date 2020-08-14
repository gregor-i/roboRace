package entities

sealed trait RejectionReason
case object InvalidScenario         extends RejectionReason
case object PlayerAlreadyRegistered extends RejectionReason
case object RobotAlreadyTaken       extends RejectionReason
case object PlayerNotRegistered     extends RejectionReason
case object NoPlayersRegistered     extends RejectionReason
case object PlayerAlreadyFinished   extends RejectionReason
case object PlayerAlreadyQuitted    extends RejectionReason
case object WrongCycle              extends RejectionReason
case object PlayerNotFound          extends RejectionReason
case object WrongState              extends RejectionReason
case object InvalidActionChoice     extends RejectionReason
case object ActionAlreadyUsed       extends RejectionReason
case object InvalidSlot             extends RejectionReason
case object InvalidIndex            extends RejectionReason
