/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub._

@Singleton
class DesSubmissionService {

  val store: PersistentGen[String, CreateSubscriptionRequest] = Generator.store

  def createSubscriptionResponse(data: CreateSubscriptionRequest): CreateSubscriptionResponse = {
    store(data.customerIdentificationNumber) = data
    Generator.genCreateSubscriptionResponse.seeded(data.customerIdentificationNumber).get
  }

  def retrieveSubscriptionDetails(utr: String): Option[CreateSubscriptionRequest] = {
    store.get(utr)
  }

}

