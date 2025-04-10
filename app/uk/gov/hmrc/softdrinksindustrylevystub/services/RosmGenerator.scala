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
import uk.gov.hmrc.softdrinksindustrylevystub.models.RosmOrganisationType.*

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
      secondaryPhoneNumber <- Gen.ukPhoneNumber
      faxNumber            <- Gen.ukPhoneNumber.rarely
      emailAddress         <- genEmail.almostAlways
    } yield RosmResponseContactDetails(
      primaryPhoneNumber,
      Some(secondaryPhoneNumber),
      faxNumber,
      emailAddress
    )

  import java.util.Random

  def deterministicAlphaStr(utr: String, desiredLength: Int = 24): String = {
    // Use a stable seed derived from the UTR
    val seed = utr.hashCode
    val rnd = new Random(seed)
    val sb = new StringBuilder
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    for (_ <- 1 to desiredLength)
      sb.append(chars.charAt(rnd.nextInt(chars.length)))
    sb.toString()
  }

  private def deterministicOrg(utr: String): OrganisationResponse = {
    val hash = utr.hashCode.abs
    val name = deterministicAlphaStr(utr)
    val isGroup = hash % 2 == 0
    val orgType = (hash % 4) match {
      case 0 => LLP
      case 1 => CorporateBody
      case 2 => UnincorporatedBody
      case _ => Unknown
    }
    OrganisationResponse(name, isGroup, orgType)
  }

  private def shouldGenOrg(utr: String): Gen[OrganisationResponse] =
    Gen.const(deterministicOrg(utr))

  private def genIndividual(isAnIndividual: Boolean, utr: String): Gen[Option[Individual]] =
    if (isAnIndividual) {
      Some(
        Individual(
          Gen.forename().seeded(utr).get,
          Gen.forename().rarely.seeded(utr).get,
          Gen.surname.seeded(utr).get,
          Gen.date.seeded(utr)
        )
      )
    } else {
      Gen.const(None)
    }

  private def shouldGenAgentRef(isAnAgent: Boolean, utr: String): Option[String] =
    if (isAnAgent) Gen.alphaNumStr.seeded(utr) else None

  def genRosmRegisterResponse(rosmRequest: RosmRegisterRequest, utr: String): Gen[RosmRegisterResponse] =
    for {
      safeId <- genSafeId
      agentReferenceNumber = shouldGenAgentRef(rosmRequest.isAnAgent, utr)
      isEditable <- Gen.boolean
      isAnAgent = rosmRequest.isAnAgent
      isAnIndividual <- Gen.const(rosmRequest.individual.isDefined)
      individual     <- genIndividual(rosmRequest.individual.isDefined, utr)
      organisation   <- shouldGenOrg(utr)
      address        <- genRosmResponseAddress
      contactDetails <- genRosmResponseContactDetails
    } yield RosmRegisterResponse(
      safeId,
      agentReferenceNumber,
      isEditable,
      isAnAgent,
      isAnIndividual,
      individual,
      Some(organisation),
      address,
      contactDetails
    )
}
