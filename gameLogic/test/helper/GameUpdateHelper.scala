package helper

import org.scalatest.Matchers

trait GameUpdateHelper
  extends AssertionHelper
    with DeconstructHelper
    with UpdateChainHelper
    with TestDataHelper {
  _: Matchers =>
}
