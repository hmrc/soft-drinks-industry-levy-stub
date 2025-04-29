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
import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.sdilRefEnum
import uk.gov.hmrc.smartstub.ToLong

import javax.inject.{Inject, Singleton}

import scala.collection.mutable

@Singleton
class DesSubmissionService @Inject() () {

  private lazy val returnStore: mutable.Map[String, Return] = mutable.Map.empty
  given ToLong[String] = sdilRefEnum

  extension [A](a: A)(using tl: ToLong[A])
    def asLong: Long =
      tl.asLong(a)

  def createSubscriptionResponse(idNumber: String, data: Subscription): CreateSubscriptionResponse = {
    import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.tolerantUtr
    val sdilRef = Store.unusedSdilRefs.head
    Store.add(data.copy(sdilRef = sdilRef))
    SubscriptionGenerator.genCreateSubscriptionResponse.seeded(idNumber)(tolerantUtr).get
  }

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
    val seed: Long = sdilRef.asLong

    ReturnGenerator.genCreateReturnResponse
      .seeded(sdilRef)
      .get
  }

  def checkForExistingReturn(sdilRefAndPeriodKey: String): Boolean =
    returnStore.contains(sdilRefAndPeriodKey)

  // TODO smart stub should override `clear()` to only clear the state changes
  def resetSubscriptions(): Unit = {
    println("wwwwwwww")
    SubscriptionGenerator.store.clear()
  }

  def resetReturns(): Unit = {
    println("yyyyyyyyyyyy")
    returnStore.clear()
  }

  def currentReturnKeys = returnStore.keys.toList

}
