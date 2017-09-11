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

package uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.getsub

import java.time.{LocalDate => Date}


/*
 * Rough Scala approximation of raw ETMP data structures
 * 
 * Not to be used directly in MDTP BE/FE services!
 */


/**
  * Judging by the EPIDD it looks like this might end up being a POST
  * request, rather than a GET as we might expect
  * a 
  */
case class GetSubscriptionRequest (
  safeId: Option[String],
  nino: Option[String],
  utr: Option[String],
  sap_number: String,
  taxRegime: String,
  taxRegimeReference: Option[String]
)

case class GetSubscriptionResponse (
  safeid: Option[String],
  nino: Option[String],
  utr: Option[String],
  changeableIndicator: String,
  taxObligationStartDate: Date,
  taxObligationEndDate: Date,
  tradingName: Option[String],
  deregistrationDate: Option[Date],
  voluntaryRegistration: Option[String],
  smallProducer: Boolean,
  largeProducer: Boolean,
  contractPacker: Boolean,
  importer: Boolean,
  primaryContactName: Option[String],
  positionInCompany: Option[String],
  telephone: Option[String],
  mobile: Option[String],
  email: Option[String],
  addressDetails: Option[AddressDetails],
  relationshipDetails: RelationshipDetails,
  bankDetails: Option[BankDetails],
  sites: List[SiteDetails]
)

/**
  * isomorphic to createsub.Address
  */
case class AddressDetails (
  businessAddressLine1: Option[String],
  businessAddressLine2: Option[String],
  businessAddressLine3: Option[String],
  businessAddressLine4: Option[String],
  businessCountryKey: Option[String],
  businessPostalCode: Option[String],
  telephone: Option[String],
  mobile: Option[String],
  email: Option[String],
  fax: Option[String]
)

/**
  * Replace internally with List[String]? 
  */
case class RelationshipDetails (
  partnerName1: Option[String],
  partnerName2: Option[String],
  partnerName3: Option[String],
  partnerName4: Option[String]
)

/**
  * Perhaps these should all be mandatory as bankDetails field is
  * optional and presumably these fields are required in conjunction
  */
case class BankDetails (
  bankKey: Option[String],
  bankAccount: Option[String],
  reference: Option[String],
  accountHolder: Option[String],
  accountName: Option[String]
)

/**
  * No normalisation of structure present with AddressDetails, nor
  * standardisation with the Site in create subscription
  */
case class SiteDetails (
  siteReference: Option[String],
  tradingName: Option[String],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postCode: Option[String],
  telephone: Option[String],
  mobile: Option[String],
  emailAddress: Option[String],
  faxNumber: Option[String],
  nonUk: Boolean,
  closureDate: Option[Date],
  validFrom: Option[Date],
  validTo: Option[Date],
  siteType: Option[String] // presumably the same code type as with
                           // create subscription, but not given in EPIDDS
)
