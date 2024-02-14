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

import java.time.LocalDate

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

  it should "return a subscription with sdil ref and utr property" in {
    forAll(sdilRefEnum) { sdil =>
      sdilToUtr(sdil).map { utr =>
        whenever("12345".toList.contains(utr.last)) {
          val subscription = Store.fromUtr(utr)
          subscription shouldBe defined
          subscription.map(_.utr) shouldBe subscription.map(_.sdilRef).flatMap(sdilToUtr)
        }
      }
    }
  }

  it should "return a subscription with number of years deregistered equal to ten minus 5nd last digit for (7-9)" in {
    forAll(sdilRefEnum.retryUntil(ref => "789".toList.contains(ref.reverse(4)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.flatMap(_.deregDate).map(_.getYear) shouldBe Some(LocalDate.now().getYear + utr.reverse(4).asDigit - 10)
      }
    }
  }

  it should "return a subscription with no deregistration date when 5nd last digit is (0-6)" in {
    forAll(sdilRefEnum.retryUntil(ref => "0123456".toList.contains(ref.reverse(4)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.flatMap(_.deregDate) shouldBe None
      }
    }
  }

  it should "return a subscription with importer activity if 4th last digit is 1, 3" in {
    forAll(sdilRefEnum.retryUntil(ref => "13".toList.contains(ref.reverse(3)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isImporter) shouldBe Some(true)
      }
    }
  }

  it should "return a subscription with no importer activity if 4th last digit is 0, 2" in {
    forAll(sdilRefEnum.retryUntil(ref => "02".toList.contains(ref.reverse(3)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isImporter) shouldBe Some(false)
      }
    }
  }

  it should "return a subscription with copacker activity if 4th last digit is 2, 3" in {
    forAll(sdilRefEnum.retryUntil(ref => "23".toList.contains(ref.reverse(3)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isContractPacker) shouldBe Some(true)
      }
    }
  }

  it should "return a subscription with no copacker activity if 4th last digit is 0, 1" in {
    forAll(sdilRefEnum.retryUntil(ref => "01".toList.contains(ref.reverse(3)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isContractPacker) shouldBe Some(false)
      }
    }
  }

  it should "return a subscription with producing own brands activity if 3rd last digit is 2, 3, 5, 6" in {
    forAll(sdilRefEnum.retryUntil(ref => "2356".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.producesOwnBrands) shouldBe Some(true)
      }
    }
  }

  it should "return a subscription with no producing own brands activity if 3rd last digit is 0, 1, 4" in {
    forAll(sdilRefEnum.retryUntil(ref => "014".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.producesOwnBrands) shouldBe Some(false)
      }
    }
  }

  it should "return a subscription with copackee activity if 3rd last digit is 1, 3, 4, 6" in {
    forAll(sdilRefEnum.retryUntil(ref => "1346".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isCopackee) shouldBe Some(true)
      }
    }
  }

  it should "return a subscription with no copackee activity if 3rd last digit is 0, 2, 5" in {
    forAll(sdilRefEnum.retryUntil(ref => "025".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isCopackee) shouldBe Some(false)
      }
    }
  }

  it should "return a subscription with is large activity if 3rd last digit is 4, 5, 6" in {
    forAll(sdilRefEnum.retryUntil(ref => "456".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isLarge) shouldBe Some(true)
      }
    }
  }

  it should "return a subscription with is not large activity if 3rd last digit is 0, 1, 2, 3" in {
    forAll(sdilRefEnum.retryUntil(ref => "0123".toList.contains(ref.reverse(2)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.activity.isLarge) shouldBe Some(false)
      }
    }
  }

  it should "return a subscription with number of years of liability equal to the 2nd last digit plus one for (0-4)" in {
    forAll(sdilRefEnum.retryUntil(ref => "01234".toList.contains(ref.reverse(1)))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.liabilityDate.getYear) shouldBe Some(LocalDate.now().getYear - utr.reverse(1).asDigit - 1)
      }
    }
  }

  it should "return a subscription with 0 production sites if last digit is 0, 3, 6" in {
    forAll(sdilRefEnum.retryUntil(ref => "036".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.productionSites.size) shouldBe Some(0)
      }
    }
  }

  it should "return a subscription with 1 production site if last digit is 1, 4, 7" in {
    forAll(sdilRefEnum.retryUntil(ref => "147".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.productionSites.size) shouldBe Some(1)
      }
    }
  }

  it should "return a subscription with 2 production sites if last digit is 2, 5, 8" in {
    forAll(sdilRefEnum.retryUntil(ref => "258".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.productionSites.size) shouldBe Some(2)
      }
    }
  }

  it should "return a subscription with 0 warehouses if last digit is 0, 1, 2" in {
    forAll(sdilRefEnum.retryUntil(ref => "012".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.warehouseSites.size) shouldBe Some(0)
      }
    }
  }

  it should "return a subscription with 1 warehouse if last digit is 3, 4, 5" in {
    forAll(sdilRefEnum.retryUntil(ref => "345".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.warehouseSites.size) shouldBe Some(1)
      }
    }
  }

  it should "return a subscription with 2 warehouses if last digit is 6, 7, 8" in {
    forAll(sdilRefEnum.retryUntil(ref => "678".toList.contains(ref.last))) { sdil =>
      sdilToUtr(sdil).map { utr =>
        val subscription = Store.fromUtr(utr)
        subscription shouldBe defined
        subscription.map(_.warehouseSites.size) shouldBe Some(2)
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
