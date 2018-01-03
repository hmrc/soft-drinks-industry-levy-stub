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
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.Subscription

import scala.collection.mutable

@Singleton
class DesSubmissionService {

  lazy val store: mutable.Map[String, Subscription] = mutable.Map.empty

  def createSubscriptionResponse(idNumber: String, data: Subscription): CreateSubscriptionResponse = {
    store(idNumber) = data
    SubscriptionGenerator.genCreateSubscriptionResponse.seeded(idNumber).get
  }

  def retrieveSubscriptionDetails(idNumber: String): Option[Subscription] = {
    store.get(idNumber)
  }

}

