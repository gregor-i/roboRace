package helper

import org.scalatest.matchers.should.Matchers

trait GameUpdateHelper extends AssertionHelper with DeconstructHelper with UpdateChainHelper with TestDataHelper {
  _: Matchers =>
}
