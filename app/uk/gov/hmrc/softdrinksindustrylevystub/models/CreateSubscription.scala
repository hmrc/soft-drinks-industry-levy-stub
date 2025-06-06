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

package uk.gov.hmrc.softdrinksindustrylevystub.models

import java.time.{LocalDate => Date, OffsetDateTime}
import play.api.libs.json.{Format, Json}

case class Address(
  notUKAddress: Boolean,
  line1: String,
  line2: String,
  line3: Option[String],
  line4: Option[String],
  postCode: Option[String],
  country: Option[String]
) {
  def isValid: Boolean = {
    val linePattern: String = "^[A-Za-z0-9 \\-,.&'\\/]{1,35}$"
    Seq(
      line1.matches(linePattern),
      line2.matches(linePattern),
      line3.matches(linePattern),
      line4.matches(linePattern),
      postCode.matches("^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$|BFPO\\s?[0-9]{1,3}$"),
      country.length <= 2
    ).reduce(_ && _)
  }
}

case class ContactDetails(
  telephone: String,
  mobile: Option[String],
  fax: Option[String],
  email: String
) {
  def isValid: Boolean =
    Validation.isValidContactDetails(this)
}

case class BusinessContact(
  addressDetails: Address,
  contactDetails: ContactDetails
)

case class CorrespondenceContact(
  addressDetails: Address,
  contactDetails: ContactDetails,
  differentAddress: Option[Boolean]
)

case class PrimaryPersonContact(
  name: String,
  positionInCompany: Option[String],
  telephone: String,
  mobile: Option[String],
  email: String
) {
  def isValid: Boolean =
    Seq(
      name.matches("^[a-zA-Z &`\\-\\'\\.^]{1,40}$"),
      positionInCompany.matches("^[a-zA-Z &`\\-\\'\\.^]{1,155}$"),
      Validation.isValidPhone(telephone),
      Validation.isValidPhone(mobile)
    ) reduce (_ && _)
}

case class LitresProduced(
  litresProducedUKHigher: Option[Long],
  litresProducedUKLower: Option[Long],
  litresImportedUKHigher: Option[Long],
  litresImportedUKLower: Option[Long],
  litresPackagedUKHigher: Option[Long],
  litresPackagedUKLower: Option[Long]
) {
  def isValid: Boolean =
    Seq(
      litresProducedUKHigher <= maxL,
      litresProducedUKLower <= maxL,
      litresImportedUKHigher <= maxL,
      litresImportedUKLower <= maxL,
      litresPackagedUKHigher <= maxL,
      litresPackagedUKLower <= maxL
    ) reduce (_ && _)
}

case class ProducerDetails(
  produceMillionLitres: Boolean,
  producerClassification: String,
  smallProducerExemption: Option[Boolean],
  useContractPacker: Option[Boolean],
  voluntarilyRegistered: Option[Boolean]
)

case class Details(
  producer: Boolean,
  producerDetails: Option[ProducerDetails],
  importer: Boolean,
  contractPacker: Boolean
) {
  def isValid: Boolean =
    producerDetails match {
      case Some(a) if producer =>
        a.producerClassification.matches("^[01]{1}$")
      case _ => !producer
    }
}

object SiteAction extends Enumeration {
  val Unknown, NewSite, AmendSite, CloseSite, TransferSite = Value
  implicit val siteActionFormat: Format[SiteAction.Value] = EnumUtils.enumFormat(SiteAction)
}

object SiteType extends Enumeration {
  val Unknown, Warehouse, ProductionSite = Value
  implicit val siteTypeFormat: Format[SiteType.Value] = EnumUtils.enumFormat(SiteType)
}

case class Site(
  action: String,
  tradingName: String,
  newSiteRef: String,
  siteAddress: BusinessContact,
  siteType: String
)

case class Registration(
  organisationType: String,
  applicationDate: Date,
  taxStartDate: Date,
  cin: String,
  tradingName: String,
  businessContact: BusinessContact,
  correspondenceContact: Option[CorrespondenceContact],
  primaryPersonContact: PrimaryPersonContact,
  details: Details,
  activityQuestions: Option[LitresProduced],
  estimatedTaxAmount: BigDecimal,
  taxObligationStartDate: Date
)

case class EntityAction(
  action: String,
  entityType: String,
  organisationType: String,
  cin: String,
  tradingName: String,
  businessContact: BusinessContact
) {
  def isValid: Boolean =
    Seq(
      action.matches("^[1]{1}$"),
      entityType.matches("^4$"),
      Validation.isValidOrganisationType(organisationType),
      Validation.isValidTradingName(tradingName),
      businessContact.addressDetails.isValid,
      businessContact.contactDetails.isValid,
      Validation.isValidCin(cin)
    ) reduce (_ && _)
}

case class CreateSubscriptionRequest(
  registration: Registration,
  sites: Option[List[Site]],
  entityAction: Option[List[EntityAction]]
) {
  def isValid: Boolean =
    Seq(
      Validation.isValidCin(registration.cin),
      registration.businessContact.addressDetails.isValid,
      registration.businessContact.contactDetails.isValid,
      registration.correspondenceContact.forall(_.addressDetails.isValid),
      registration.correspondenceContact.forall(_.contactDetails.isValid),
      registration.primaryPersonContact.isValid,
      Validation.isValidSites(sites),
      Validation.isValidTradingName(registration.tradingName),
      Validation.isValidOrganisationType(registration.organisationType),
      registration.details.isValid,
      registration.activityQuestions.isValid,
      registration.estimatedTaxAmount <= BigDecimal(99999999999.99)
    ) reduce (_ && _)
}

case class CreateSubscriptionResponse(
  processingDate: OffsetDateTime,
  formBundleNumber: String
)

object Validation {

  def isValidIdNumber(idNumber: String): Option[FailureMessage] =
    idNumber match {
      case a if !isValidUtr(a) =>
        Some(FailureMessage("INVALID_UTR", "Submission has not passed validation. Invalid parameter UTR."))
      case _ => None
    }

  def isValidUtr(utr: String): Boolean =
    utr.matches("^[0-9]{10}$")

  def isValidCin(cin: String): Boolean =
    cin.matches("^[a-zA-Z0-9 ,\\.\\/\\-:]{1,15}$")

  def isValidIdType(idType: String): Option[FailureMessage] =
    idType match {
      case a if a != "utr" =>
        Some(FailureMessage("INVALID_IDTYPE", s"Submission has not passed validation. Invalid parameter IDTYPE."))
      case _ => None
    }

  def isValidSites(sites: Option[List[Site]]): Boolean =
    sites.forall(_.forall(isValidSite))

  def isValidSite(site: Site): Boolean =
    Seq(
      site.siteAddress.addressDetails.isValid,
      site.action.matches("^[1]{1}$"),
      isValidTradingName(site.tradingName),
      site.newSiteRef.matches("^[a-zA-Z0-9 ,.\\/]{1,20}$")
    ) reduce (_ && _)

  def isValidContactDetails(cd: ContactDetails): Boolean =
    Seq(
      isValidPhone(cd.telephone),
      isValidPhone(cd.mobile),
      isValidPhone(cd.fax),
      cd.email.length <= 132
    ) reduce (_ && _)

  val phonePattern = "^[A-Z0-9 )/(\\-*#+]{1,24}$"

  def isValidPhone(phone: Option[String]): Boolean =
    phone.matches(phonePattern)

  def isValidPhone(phone: String): Boolean =
    phone.matches(phonePattern)

  def isValidTradingName(tradingName: String): Boolean =
    tradingName.matches("^[a-zA-Z0-9 '.&\\/]{1,160}$")

  def isValidOrganisationType(organisationType: String): Boolean =
    organisationType.matches("^[12357]{1}$")

  def checkParams(idType: String, idNumber: String): List[FailureMessage] =
    List(
      isValidIdType(idType),
      isValidIdNumber(idNumber)
    ) filter (_.isDefined) map (x => x.get)

  val payloadFailure = FailureMessage(
    "INVALID_PAYLOAD",
    "Submission has not passed validation. Invalid PAYLOAD."
  )
}

case class FailureMessage(
  code: String,
  reason: String
)

case class FailureResponse(
  failures: List[FailureMessage]
)

object JsonFormats {
  implicit val failureMessageFormat: Format[FailureMessage] = Json.format[FailureMessage]
  implicit val failureResponseFormat: Format[FailureResponse] = Json.format[FailureResponse]
}

// n.b. leaving these although they are unused until we see the retrieve spec
//object ProducerClassification extends Enumeration {
//  type ProducerClassification = Value
//  val Unknown, Large, Small = Value
//  implicit val producerClassificationFormat = EnumUtils.enumFormat(ProducerClassification)
//}
//
//object OrganisationType extends Enumeration {
//  type OrganisationType = Value
//  val SoleProprietor, LimitedCompany, LLP, UnincorporatedBody, Partnership = Value
//  implicit val organisationTypeFormat = EnumUtils.enumFormat(OrganisationType)
//}
//
//object ActionType extends Enumeration {
//  val Unknown, Add, Amend, Remove = Value
//  implicit val actionTypeFormat = EnumUtils.enumFormat(ActionType)
//}
//
//object EntityType extends Enumeration {
//  val Unknown, GroupMember, GroupRepresentativeMember, ControllingBody, Partner = Value
//  implicit val entityTypeFormat = EnumUtils.enumFormat(EntityType)
//}
//
//object ActivityType extends Enumeration {
//  val Unknown, Producer, Importer, ContractPacker = Value
//  implicit val activityTypeFormat = EnumUtils.enumFormat(ActivityType)
//}
