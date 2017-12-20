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

import java.time.{LocalDateTime, ZoneOffset}

import cats.implicits._
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._

object SubscriptionGenerator {

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] = {
    Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC))    |@| // processingDate
    pattern"999999999999".gen                                    // formBundleNumber
  }.map(CreateSubscriptionResponse.apply)

  def genCorrelationIdHeader: Gen[String] = {
    Gen.listOfN(
      36,
      Gen.frequency(
        (3,Gen.alphaUpperChar),
        (3,Gen.alphaLowerChar),
        (3,Gen.numChar),
        (1, Gen.const("-"))
      )
    ).map(_.mkString)                                            // correlationId
  }
}


