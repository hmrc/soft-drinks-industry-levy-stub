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

case class ContactDetails(
  name: String,
  positionInCompany: Option[String],
  telephoneNumber: String,
  mobileNumber: Option[String],
  emailAddress: String
)

case class LevyDetails(
  activities: ActivityType.Value,
  lessThanMillion: Boolean,
  producerClassification: Option[ProducerClassification.Value],
  smallProducerExemption: Boolean,
  usesCopacker: Boolean,
  voluntarilyRegistered: Boolean
)

case class LitresProduced(
  ProducedLower: Option[Int],
  ProducedHigher: Option[Int],
  ImportedLower: Option[Int],
  ImportedHigher: Option[Int],
  PackagedLower: Option[Int],
  PackagedHigher: Option[Int]
)

case class BankDetails (
  directDebit: Boolean,
  accountName: Option[String],
  accountNumber: Option[String],
  sortCode: Option[String],
  buildingSocietyRollNumber: Option[String]
)


object ProducerClassification extends Enumeration {
  type OrganisationType = Value
  val Unknown, Large, Small = Value
}


object OrganisationType extends Enumeration {
  type OrganisationType = Value
  val Unknown, SoleProprietor, LimitedCompany, LLP, UnincorporatedBody, Partnership, Trust = Value
}

object ActionType extends Enumeration {
  val Unknown, Add, Amend, Remove = Value
}

object EntityType extends Enumeration {
  val Unknown, GroupMember, GroupRepresentativeMember, ControllingBody, Partner = Value
}

object ActivityType extends Enumeration {
  val Unknown, Producer, Importer, ContractPacker = Value
}

object SiteAction extends Enumeration {
  val Unknown, NewSite, AmendSite, CloseSite, TransferSite = Value
}

object SiteType extends Enumeration {
  val Unknown, Warehouse, ProductionSite = Value
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

case class CreateSubscriptionResponse (
  safeId: String,
  formBundleNumber: String
)
