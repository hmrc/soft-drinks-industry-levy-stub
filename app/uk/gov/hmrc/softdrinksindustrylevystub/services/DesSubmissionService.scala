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

package uk.gov.hmrc.softdrinksindustrylevystub.services

import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.*
import uk.gov.hmrc.softdrinksindustrylevystub.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.Subscription
import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.toLongFromTolerantUtr

import scala.collection.mutable

class DesSubmissionService {

  private lazy val returnStore: mutable.Map[String, Return] = mutable.Map.empty

  def createSubscriptionResponse(idNumber: String, data: Subscription): CreateSubscriptionResponse = {
    val sdilRef = Store.unusedSdilRefs.head
    Store.add(data.copy(sdilRef = sdilRef))
    SubscriptionGenerator.genCreateSubscriptionResponse.seeded(idNumber)(using toLongFromTolerantUtr).get
  }

  val sdilRefPattern: Enumerable[String] =
    pattern"ZZ9999999994".imap(i => i.take(2) ++ "SDIL" ++ i.substring(2, 7) ++ "C" ++ i.takeRight(1))(b =>
      b.take(2) ++ b.takeRight(9)
    )

  given toLongFromSdilRefEnum: ToLong[String] = sdilRefPattern

  def retrieveSubscriptionDetails(idType: String, idNumber: String): Option[Subscription] =
    for {
      utr <- idType match {
               case "utr"  => Some(idNumber)
               case "sdil" => SdilNumberTransformer.sdilToUtr(idNumber)
             }
      subscription <- Store.fromUtr(utr)
    } yield subscription.copy(utr = utr)

  def createReturnResponse(payload: Return, sdilRef: String): ReturnSuccessResponse = {

    returnStore(sdilRef ++ payload.periodKey) = payload
    ReturnGenerator.genCreateReturnResponse.seeded(sdilRef)(using toLongFromSdilRefEnum).get
  }

  def checkForExistingReturn(sdilRefAndPeriodKey: String): Boolean =
    returnStore.get(sdilRefAndPeriodKey).nonEmpty

  // TODO smart stub should override `clear()` to only clear the state changes
  def resetSubscriptions(): Unit =
    SubscriptionGenerator.store.state.clear()

  def resetReturns(): Unit =
    returnStore.clear()

}
