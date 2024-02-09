/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.softdrinksindustrylevystub

import org.scalacheck._
import uk.gov.hmrc.softdrinksindustrylevystub.services._
import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import org.scalatest.flatspec.AnyFlatSpec

class StoreSpec extends AnyFlatSpec {
  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny

  "The Store" should "return records against a UTR or SDIL ref" in {
    forAll(sdilRefEnum) { sdil =>
      sdilToUtr(sdil).map { utr =>
        whenever("12345".toList.contains(utr.last)) {
          Store.fromUtr(utr) shouldBe defined
          Store.fromUtr(utr) shouldBe Store.fromSdilRef(sdil)
        }
      }
    }
  }

  it should "allow overriding of records" in {
    implicit val g = Arbitrary { SubscriptionGenerator.genSubscription(None) }
    forAll { subscription: Subscription =>
      Store.add(subscription)
      Store.fromUtr(subscription.utr) shouldBe Some(subscription)
      Store.fromSdilRef(subscription.sdilRef) shouldBe Some(subscription)
    }
  }

}
