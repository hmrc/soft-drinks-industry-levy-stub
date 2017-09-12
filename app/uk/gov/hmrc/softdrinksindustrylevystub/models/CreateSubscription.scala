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

package uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub

import java.time.{LocalDate => Date}

import play.api.libs.json.Json
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils

/*
 * Rough Scala approximation of raw ETMP data structures
 * 
 * Not to be used directly in MDTP BE/FE services!
 */

case class Address (
  addressNotInUk: Boolean,
  addressLine1: String,
  addressLine2: String,
  addressLine3: Option[String],
  addressLine4: Option[String],
  postcode: String,
  nonUkCountry: Option[String],
  telephoneNumber: String,
  mobileNumber: Option[String],
  emailAddress: String,
  faxNumber: Option[String]
)

object Address {
  implicit val addressFormat = Json.format[Address]
}

case class ContactDetails(
  name: String,
  positionInCompany: Option[String],
  telephoneNumber: String,
  mobileNumber: Option[String],
  emailAddress: String
)

object ContactDetails {
  implicit val contactDetailsFormat = Json.format[ContactDetails]
}

case class LitresProduced(
  ProducedLower: Option[Int],
  ProducedHigher: Option[Int],
  ImportedLower: Option[Int],
  ImportedHigher: Option[Int],
  PackagedLower: Option[Int],
  PackagedHigher: Option[Int]
)

object LitresProduced {
  implicit val litresProducedFormat = Json.format[LitresProduced]
}

case class BankDetails (
  directDebit: Boolean,
  accountName: Option[String],
  accountNumber: Option[String],
  sortCode: Option[String],
  buildingSocietyRollNumber: Option[String]
)

object BankDetails {
  implicit val bankDetailsFormat = Json.format[BankDetails]
}


object ProducerClassification extends Enumeration {
  type ProducerClassification = Value
  val Unknown, Large, Small = Value
  implicit val producerClassificationFormat = EnumUtils.enumFormat(ProducerClassification)
}


object OrganisationType extends Enumeration {
  type OrganisationType = Value // TODO - figure out if these type statements are needed
  val Unknown, SoleProprietor, LimitedCompany, LLP, UnincorporatedBody, Partnership, Trust = Value
  implicit val organisationTypeFormat = EnumUtils.enumFormat(OrganisationType)
}

object ActionType extends Enumeration {
  val Unknown, Add, Amend, Remove = Value
  implicit val actionTypeFormat = EnumUtils.enumFormat(ActionType)
}

object EntityType extends Enumeration {
  val Unknown, GroupMember, GroupRepresentativeMember, ControllingBody, Partner = Value
  implicit val entityTypeFormat = EnumUtils.enumFormat(EntityType)
}

object ActivityType extends Enumeration {
  val Unknown, Producer, Importer, ContractPacker = Value
  implicit val activityTypeFormat = EnumUtils.enumFormat(ActivityType)
}
case class LevyDetails(
  activities: ActivityType.Value,
  lessThanMillion: Boolean,
  producerClassification: Option[ProducerClassification.Value],
  smallProducerExemption: Boolean,
  usesCopacker: Boolean,
  voluntarilyRegistered: Boolean
)

object LevyDetails {
  implicit val levyDetailsFormat = Json.format[LevyDetails]
}

object SiteAction extends Enumeration {
  val Unknown, NewSite, AmendSite, CloseSite, TransferSite = Value
  implicit val siteActionFormat = EnumUtils.enumFormat(SiteAction)
}

object SiteType extends Enumeration {
  val Unknown, Warehouse, ProductionSite = Value
  implicit val siteTypeFormat = EnumUtils.enumFormat(SiteType)
}

case class Site (
  action: Option[SiteAction.Value],
  siteReference: Option[String],
  dateOfClosure: Option[Date],
  siteClosureReason: Option[String],
  tradingName: Option[String],
  newSiteReference: Option[String],
  address: Address,
  typeOfSite: Option[SiteType.Value]
)

object Site {
  implicit val siteFormat = Json.format[Site]
}

case class CreateSubscriptionRequest (
  organisationType: OrganisationType.Value,
  action: Option[ActionType.Value],
  typeOfEntity: Option[EntityType.Value],
  dateOfApplication: Date,
  taxStartDate: Date,
  joiningDate: Option[Date],
  leavingDate: Option[Date],
  customerIdentificationNumber: String,
  tradingName: String,
  businessContactDetails: Address,
  correspondenceAddressDiffers: Boolean,
  correspondenceAddress: Option[Address],
  primaryPerson: ContactDetails,
  softDrinksIndustryLevyDetails: LevyDetails,
  sdilActivity: LitresProduced,
  estimatedAmountOfTaxInTheNext12Months: Option[Int],
  taxObligationStartDate: Date,
  bankDetails: BankDetails,
  sites: List[Site]
)

object CreateSubscriptionRequest {
  implicit val createSubscriptionRequestFormat = Json.format[CreateSubscriptionRequest]
}


case class CreateSubscriptionResponse (
  safeId: String,
  formBundleNumber: String
)

object CreateSubscriptionResponse {
  implicit val createSubscriptionResponseFormat = Json.format[CreateSubscriptionResponse]
}