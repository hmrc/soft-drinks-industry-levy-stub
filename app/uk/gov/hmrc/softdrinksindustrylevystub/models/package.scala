/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.softdrinksindustrylevystub

import play.api.libs.json._
import uk.gov.hmrc.smartstub._

package object models {

  //ROSM register formatters
  implicit val organisationTypeFormat: Format[RosmOrganisationType.Value] = EnumUtils.enumFormat(RosmOrganisationType)
  implicit val individualFormatter: OFormat[Individual] = Json.format[Individual]
  implicit val organisationReqFormatter: OFormat[OrganisationRequest] = Json.format[OrganisationRequest]
  implicit val rosmRequestFormatter: OFormat[RosmRegisterRequest] = Json.format[RosmRegisterRequest]

  //ROSM register response formatters
  implicit val rosmResponseAddress: OFormat[RosmResponseAddress] = Json.format[RosmResponseAddress]
  implicit val rosmResponseOrg: OFormat[OrganisationResponse] = Json.format[OrganisationResponse]
  implicit val rosmResponseContactDetails: OFormat[RosmResponseContactDetails] = Json.format[RosmResponseContactDetails]
  implicit val rosmRegisterResponse: OFormat[RosmRegisterResponse] = Json.format[RosmRegisterResponse]

  // SDIL create and retrieve subscription formatters
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
  implicit val contactDetailsFormat: OFormat[ContactDetails] = Json.format[ContactDetails]
  implicit val businessContactFormat: OFormat[BusinessContact] = Json.format[BusinessContact]
  implicit val correspondenceContactFormat: OFormat[CorrespondenceContact] = Json.format[CorrespondenceContact]
  implicit val primaryContactFormat: OFormat[PrimaryPersonContact] = Json.format[PrimaryPersonContact]
  implicit val litresProducedFormat: OFormat[LitresProduced] = Json.format[LitresProduced]
  implicit val producerDetailsFormat: OFormat[ProducerDetails] = Json.format[ProducerDetails]
  implicit val detailsFormat: OFormat[Details] = Json.format[Details]
  implicit val siteFormat: OFormat[Site] = Json.format[Site]
  implicit val registrationFormat: OFormat[Registration] = Json.format[Registration]
  implicit val entityActionFormat: OFormat[EntityAction] = Json.format[EntityAction]
  implicit val createSubscriptionRequestFormat: OFormat[CreateSubscriptionRequest] =
    Json.format[CreateSubscriptionRequest]
  implicit val createSubscriptionResponseFormat: OFormat[CreateSubscriptionResponse] =
    Json.format[CreateSubscriptionResponse]
  implicit val failureFormat: OFormat[FailureMessage] = Json.format[FailureMessage]
  implicit val failureResponseFormat: OFormat[FailureResponse] = Json.format[FailureResponse]

  // SDIL return formatters
  implicit val monetaryValuesFormat: OFormat[MonetaryValues] = Json.format[MonetaryValues]
  implicit val volumeFormat: OFormat[Volume] = Json.format[Volume]
  implicit val expoWastedFormat: OFormat[ExpoWasted] = Json.format[ExpoWasted]
  implicit val importingFormat: OFormat[Importing] = Json.format[Importing]
  implicit val itemFormat: OFormat[Item] = Json.format[Item]

  implicit val packagingFormat: OFormat[Packaging] = Json.format[Packaging]
  implicit val returnFormat: OFormat[Return] = Json.format[Return]
  implicit val returnSuccessResponseFormat: OFormat[ReturnSuccessResponse] = Json.format[ReturnSuccessResponse]
  implicit val returnFailureResponseFormat: OFormat[ReturnFailureResponse] = Json.format[ReturnFailureResponse]

  val maxL: Long = 9999999999999L

  private[models] implicit class ValidationOptionString(s: Option[String]) {
    def matches(regex: String): Boolean = s.forall(_.matches(regex))

    def length: Int = s match {
      case Some(data) => data.length
      case _          => 0
    }
  }

  private[models] implicit class ValidationOptionLong(l: Option[Long]) {
    def <=(max: Long): Boolean = l.forall(_ <= max)
  }

  private[models] implicit class ValidationOptionBigDecimal(bd: Option[BigDecimal]) {
    def <=(max: BigDecimal): Boolean = bd.forall(_ <= max)
  }

  private[models] implicit class ValidationOptionLitresProduced(lp: Option[LitresProduced]) {
    def isValid: Boolean = lp.forall(_.isValid)
  }

}
