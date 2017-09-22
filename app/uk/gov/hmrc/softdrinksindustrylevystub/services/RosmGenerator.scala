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

import cats.implicits._
import org.scalacheck.Gen
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._

object RosmGenerator {

  private def genRosmResponseAddress: Gen[RosmResponseAddress] = {
    Gen.oneOf("The house", "50") |@| //addressLine1
      Gen.oneOf("The Street", "The Road", "The Lane").almostAlways |@| //addressLine2
      Gen.alphaLowerStr.almostAlways |@| //addressLine3
      Gen.alphaLowerStr.rarely |@| //addressLine4
      Gen.const("GB") |@| //countryCode
      Gen.postcode //postalCode
  }.map(RosmResponseAddress.apply)

  private def genEmail = {
    for {
      length <- Gen.choose(5, 8).flatMap(len => Gen.listOfN(len, Gen.alphaLowerChar)).map(_.mkString)
      at <- Gen.const("@")
      domain <- Gen.choose(7, 9).flatMap(len => Gen.listOfN(len, Gen.alphaLowerChar)).map(_.mkString)
      ending <- Gen.const(".com")
    } yield s"$length$at$domain$ending"
  }

  private def genRosmResponseContactDetails: Gen[RosmResponseContactDetails] = {
    Gen.ukPhoneNumber.almostAlways |@| //primaryPhoneNumber
      Gen.ukPhoneNumber.sometimes |@| //secondaryPhoneNumber
      Gen.ukPhoneNumber.rarely |@| //faxNumber
      genEmail.almostAlways //emailAddress
  }.map(RosmResponseContactDetails.apply)

  private def shouldGenOrg(organisationReq: Option[OrganisationRequest], utr: String): Option[OrganisationResponse] = {
    if (organisationReq.isEmpty) None
    else {
      val organisation = organisationReq.get
      Some(OrganisationResponse(organisation.organisationName, Gen.boolean.seeded(utr).get, organisation.organisationType))
    }
  }

  private def shouldGenAgentRef(isAnAgent: Boolean, utr: String): Option[String] = {
    if (isAnAgent) Gen.alphaNumStr.seeded(utr) else None
  }

  def genRosmRegisterResponse(rosmRequest: RosmRegisterRequest, utr: String): Gen[RosmRegisterResponse] = {
    Gen.alphaNumStr |@| //safeId
      shouldGenAgentRef(rosmRequest.isAnAgent, utr) |@| //agentReferenceNumber
      Gen.boolean |@| //isEditable
      rosmRequest.isAnAgent |@| //isAnAgent
      Gen.const(rosmRequest.individual.isDefined) |@| //isAnIndividual
      Gen.const(rosmRequest.individual) |@| //organisation
      Gen.const(shouldGenOrg(rosmRequest.organisation, utr)) |@| //individual
      genRosmResponseAddress |@| //address
      genRosmResponseContactDetails //contactDetails
  }.map(RosmRegisterResponse.apply)

}
