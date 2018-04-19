/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Singleton
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.Subscription

import scala.collection.mutable

@Singleton
class DesSubmissionService {

  private lazy val store: mutable.Map[String, Option[Subscription]] = SubscriptionGenerator.store
  private lazy val returnStore: mutable.Map[String, Return] = mutable.Map.empty

  def createSubscriptionResponse(idNumber: String, data: Subscription): CreateSubscriptionResponse = {
    import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
    store(idNumber) = Some(data)
    SubscriptionGenerator.genCreateSubscriptionResponse.seeded(idNumber).get
  }

  def retrieveSubscriptionDetails(idType: String, idNumber: String): Option[Subscription] = {
    idType match {
      case "utr" => store.get(idNumber).flatten
      case "sdil" => SdilNumberTransformer.sdilToUtr(idNumber) flatMap { store.get(_).flatten }
    }
  }

  def createReturnResponse(payload: Return, sdilRef: String): ReturnSuccessResponse = {
    import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.sdilRefEnum
    implicit val sdilRefToLong: Enumerable[String] = sdilRefEnum
    returnStore(sdilRef ++ payload.periodKey) = payload
    ReturnGenerator.genCreateReturnResponse.seeded(sdilRef).get
  }

  def checkForExistingReturn(sdilRefAndPeriodKey: String): Boolean = {
    returnStore.get(sdilRefAndPeriodKey).nonEmpty
  }

  // TODO smart stub should override `clear()` to only clear the state changes
  def resetSubscriptions(): Unit = {
    SubscriptionGenerator.store.state.clear()
  }

  def resetReturns(): Unit = {
    returnStore.clear()
  }

}

