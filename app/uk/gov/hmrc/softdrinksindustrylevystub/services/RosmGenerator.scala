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
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._


object RosmGenerator {

  private def variableLengthString(min: Int, max: Int) = {
    Gen.choose(min, max).flatMap(len => Gen.listOfN(len, Gen.alphaLowerChar)).map(_.mkString)
  }

  private def addressLine = variableLengthString(0, 35)

  private def genRosmResponseAddress: Gen[RosmResponseAddress] = {
    Gen.oneOf("The house", "50") |@| //addressLine1
      Gen.oneOf("The Street", "The Road", "The Lane").almostAlways |@| //addressLine2
      addressLine.almostAlways |@| //addressLine3
      addressLine.rarely |@| //addressLine4
      Gen.const("GB") |@| //countryCode
      Gen.postcode //postalCode
  }.map(RosmResponseAddress.apply)

  private def genEmail = {
    for {
      username <- variableLengthString(5, 8)
      at <- Gen.const("@")
      domain <- variableLengthString(7, 9)
      ending <- Gen.const(".com")
    } yield s"$username$at$domain$ending"
  }

  private def genRosmResponseContactDetails: Gen[RosmResponseContactDetails] = {
    Gen.ukPhoneNumber.almostAlways |@| //primaryPhoneNumber
      Gen.ukPhoneNumber.sometimes |@| //secondaryPhoneNumber
      Gen.ukPhoneNumber.rarely |@| //faxNumber
      genEmail.almostAlways //emailAddress
  }.map(RosmResponseContactDetails.apply)

  private def shouldGenOrg(utr: String): OrganisationResponse = {
    import RosmOrganisationType._
    OrganisationResponse(
      Gen.alphaStr.seeded(utr).get, // TODO use company when there's a new release of smartstub
      Gen.boolean.seeded(utr).get,
      Gen.oneOf(CorporateBody, LLP, UnincorporatedBody, Unknown).seeded(utr).get)
  }

  private def shouldGenAgentRef(isAnAgent: Boolean, utr: String): Option[String] = {
    if (isAnAgent) Gen.alphaNumStr.seeded(utr) else None
  }

  def genRosmRegisterResponse(rosmRequest: RosmRegisterRequest, utr: String): Gen[Option[RosmRegisterResponse]] = {
    Gen.alphaNumStr |@| //safeId
      shouldGenAgentRef(rosmRequest.isAnAgent, utr) |@| //agentReferenceNumber
      Gen.boolean |@| //isEditable
      rosmRequest.isAnAgent |@| //isAnAgent
      Gen.const(rosmRequest.individual.isDefined) |@| //isAnIndividual
      Gen.const(rosmRequest.individual) |@| //individual
      Gen.const(shouldGenOrg(utr)).sometimes |@| //organisation
      genRosmResponseAddress |@| //address
      genRosmResponseContactDetails //contactDetails
  }.map(RosmRegisterResponse.apply).sometimes

}
