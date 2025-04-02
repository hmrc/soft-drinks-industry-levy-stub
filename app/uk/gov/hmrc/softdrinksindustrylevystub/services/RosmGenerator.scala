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

import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.*
import org.scalacheck.Gen

object RosmGenerator {

  private def variableLengthString(min: Int, max: Int) =
    Gen.choose(min, max).flatMap(len => Gen.listOfN(len, Gen.alphaLowerChar)).map(_.mkString)

  private def addressLine = variableLengthString(0, 35)

  private def genRosmResponseAddress: Gen[RosmResponseAddress] =
    for {
      line1       <- Gen.oneOf("The house", "50")
      line2       <- Gen.oneOf("The Street", "The Road", "The Lane").almostAlways
      line3       <- addressLine.almostAlways
      line4       <- addressLine.rarely
      countryCode <- Gen.const("GB")
      postcode    <- Gen.postcode
    } yield RosmResponseAddress(line1, line2, line3, line4, countryCode, postcode)

  private def genEmail =
    for {
      username <- variableLengthString(5, 8)
      at       <- Gen.const("@")
      domain   <- variableLengthString(7, 9)
      ending   <- Gen.const(".com")
    } yield s"$username$at$domain$ending"

  private def genSafeId =
    for {
      a <- Gen.const("X")
      b <- Gen.alphaUpperChar
      c <- Gen.const("000")
      d <- Gen.listOfN(10, Gen.numChar).map(_.mkString)
    } yield s"$a$b$c$d"

  private def genRosmResponseContactDetails: Gen[RosmResponseContactDetails] =
    for {
      primaryPhoneNumber   <- Gen.ukPhoneNumber.almostAlways
      secondaryPhoneNumber <- Gen.ukPhoneNumber.sometimes
      faxNumber            <- Gen.ukPhoneNumber.rarely
      emailAddress         <- genEmail.almostAlways
    } yield RosmResponseContactDetails(
      primaryPhoneNumber,
      secondaryPhoneNumber,
      faxNumber,
      emailAddress
    )

  private def shouldGenOrg(utr: String): OrganisationResponse = {
    import RosmOrganisationType._
    OrganisationResponse(
      Gen.alphaStr.seeded(utr).get, // TODO use company when there's a new release of smartstub
      Gen.boolean.seeded(utr).get,
      Gen.oneOf(CorporateBody, LLP, UnincorporatedBody, Unknown).seeded(utr).get
    )
  }

  private def genIndividual(utr: String): Gen[Individual] =
    Individual(
      Gen.forename().seeded(utr).get,
      Gen.forename().rarely.seeded(utr).get,
      Gen.surname.seeded(utr).get,
      Gen.date.seeded(utr)
    )

  private def shouldGenAgentRef(isAnAgent: Boolean, utr: String): Option[String] =
    if (isAnAgent) Gen.alphaNumStr.seeded(utr) else None

  def genRosmRegisterResponse(rosmRequest: RosmRegisterRequest, utr: String): Gen[Option[RosmRegisterResponse]] =
    (for {
      safeId <- genSafeId
      agentReferenceNumber = shouldGenAgentRef(rosmRequest.isAnAgent, utr)
      isEditable <- Gen.boolean
      isAnAgent = rosmRequest.isAnAgent
      isAnIndividual <- Gen.const(rosmRequest.individual.isDefined)
      individual     <- genIndividual(utr).sometimes
      organisation   <- Gen.const(shouldGenOrg(utr)).sometimes
      address        <- genRosmResponseAddress
      contactDetails <- genRosmResponseContactDetails
    } yield RosmRegisterResponse(
      safeId,
      agentReferenceNumber,
      isEditable,
      isAnAgent,
      isAnIndividual,
      individual,
      organisation,
      address,
      contactDetails
    )).usually
}
