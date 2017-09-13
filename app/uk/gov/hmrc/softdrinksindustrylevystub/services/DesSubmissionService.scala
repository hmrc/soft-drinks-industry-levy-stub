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

import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter._

import akka.japi.Option.Some
import cats.implicits._
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import com.google.inject.Singleton
import uk.gov.hmrc.softdrinksindustrylevystub.models.DesSubmissionResult
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub.{CreateSubscriptionRequest, CreateSubscriptionResponse}

@Singleton
class DesSubmissionService {

  def createSubscriptionResponse(data: CreateSubscriptionRequest): CreateSubscriptionResponse = {

//    data.


    CreateSubscriptionResponse("foo", "bar")
  }


  def createSubscriptionResponse() = ???

  def buildResponse(): DesSubmissionResult = DesSubmissionResult(true)
}
