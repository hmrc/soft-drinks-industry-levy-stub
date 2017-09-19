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
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub._

object Generator {

  lazy val store: PersistentGen[String, CreateSubscriptionRequest] = genCreateSubscriptionRequest.asMutable[String]

  def genCreateSubscriptionRequest: Gen[CreateSubscriptionRequest] = {
    organisationTypeGen |@|                                 // organisationType
    actionTypeGen.sometimes |@|                             // action
    entityTypeGen.sometimes |@|                             // typeOfEntity
    Gen.date(2014, 2017) |@|                                // dateOfApplication
    Gen.date(2014, 2017) |@|                                // taxStartDate
    Gen.date(2014, 2017).sometimes |@|                      // joining date
    Gen.date(2014, 2017).sometimes |@|                      // leaving date
    pattern"999999999999" |@|                               // customerIdentificationNumber
    Gen.alphaLowerStr |@|                                   // tradingName
    addressGen |@|                                          // businessContactDetails
    Gen.boolean |@|                                         // correspondenceAddressDiffers
    addressGen.sometimes |@|                                // correspondenceAddress
    contactDetailsGen |@|                                   // primaryPerson
    levyDetailsGen |@|                                      // softDrinksIndustryLevyDetails
    litresProducedGen |@|                                   // sdilActivity
    Gen.choose(1, 100000).sometimes |@|                     // estimatedAmountOfTaxInTheNext12Months
    Gen.date(2017, 2020) |@|                                // taxObligationStartDate
    bankDetailsGen |@|                                      // bankDetails
    Gen.choose(1,5).flatMap { n => Gen.listOfN(n, siteGen)} // sites
  }.map(CreateSubscriptionRequest.apply)

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] = {
    pattern"99999999".gen |@| // safeId
    pattern"99999999".gen     // formBundleNumber
  }.map(CreateSubscriptionResponse.apply)

  private lazy val siteGen: Gen[Site] = {
    siteActionGen.sometimes |@|         // action
    Gen.alphaNumStr.sometimes |@|       // siteReference
    Gen.date(2017, 2020).sometimes |@|  // dateOfClosure
    Gen.alphaNumStr.sometimes |@|       // siteClosureReason
    Gen.alphaNumStr.sometimes |@|       // tradingName
    Gen.alphaNumStr.sometimes |@|       // newSiteReference
    addressGen |@|                      // address
    siteTypeGen.sometimes               // typeOfSite
  }.map(Site.apply)

  private lazy val addressGen: Gen[Address] = {
    Gen.boolean |@|                     // addressNotInUk
    Gen.alphaNumStr |@|                 // addressLine1
    Gen.alphaNumStr |@|                 // addressLine2
    Gen.alphaNumStr.sometimes |@|       // addressLine3
    Gen.alphaNumStr.sometimes |@|       // addressLine4
    Gen.alphaNumStr |@|                 // postcode
    Gen.alphaNumStr.sometimes |@|       // nonUkCountry
    Gen.alphaNumStr |@|                 // telephoneNumber
    Gen.alphaNumStr.sometimes |@|       // mobileNumber
    Gen.alphaNumStr |@|                 // emailAddress
    Gen.alphaNumStr.sometimes           // faxNumber
  }.map(Address.apply)

  private lazy val bankDetailsGen: Gen[BankDetails] = {
    Gen.boolean |@|                     // directDebit
    Gen.alphaNumStr.sometimes |@|       // accountName
    Gen.alphaNumStr.sometimes |@|       // accountNumber
    Gen.alphaNumStr.sometimes |@|       // sortCode
    Gen.alphaNumStr.sometimes           // buildingSocietyRollNumber
  }.map(BankDetails.apply)

  private lazy val litresProducedGen: Gen[LitresProduced] = {
    Gen.choose(1, 1000000).sometimes |@|  // producedLower
    Gen.choose(1, 1000000).sometimes |@|  // producedHigher
    Gen.choose(1, 1000000).sometimes |@|  // importedLower
    Gen.choose(1, 1000000).sometimes |@|  // importedHigher
    Gen.choose(1, 1000000).sometimes |@|  // packagedLower
    Gen.choose(1, 1000000).sometimes      // packagedHigher
  }.map(LitresProduced.apply)

  private lazy val entityTypeGen: Gen[EntityType.Value] =
    for {
      entityType <- Gen.oneOf(
        EntityType.withName("Unknown"),
        EntityType.withName("GroupMember"),
        EntityType.withName("GroupRepresentativeMember"),
        EntityType.withName("ControllingBody"),
        EntityType.withName("Partner"))
    } yield entityType

  private lazy val actionTypeGen: Gen[ActionType.Value] =
    for {
      actionType <- Gen.oneOf(
        ActionType.withName("Unknown"),
        ActionType.withName("Add"),
        ActionType.withName("Amend"),
        ActionType.withName("Remove"))
    } yield actionType

  private lazy val organisationTypeGen: Gen[OrganisationType.Value] =
    for {
      orgType <- Gen.oneOf(
        OrganisationType.withName("Unknown"),
        OrganisationType.withName("SoleProprietor"),
        OrganisationType.withName("LimitedCompany"),
        OrganisationType.withName("LLP"),
        OrganisationType.withName("UnincorporatedBody"),
        OrganisationType.withName("Partnership"),
        OrganisationType.withName("Trust"))
    } yield orgType

  private lazy val siteTypeGen: Gen[SiteType.Value] =
    for {
      siteType <- Gen.oneOf(
        SiteType.withName("Unknown"),
        SiteType.withName("Warehouse"),
        SiteType.withName("ProductionSite"))
    } yield siteType

  private lazy val siteActionGen: Gen[SiteAction.Value] =
    for {
      siteAction <- Gen.oneOf(
        SiteAction.withName("Unknown"),
        SiteAction.withName("NewSite"),
        SiteAction.withName("AmendSite"),
        SiteAction.withName("CloseSite"),
        SiteAction.withName("TransferSite"))
    } yield siteAction

  private lazy val activitiesGen: Gen[ActivityType.Value] =
    for {
      activityType <- Gen.oneOf(
        ActivityType.withName("Unknown"),
        ActivityType.withName("Producer"),
        ActivityType.withName("Importer"),
        ActivityType.withName("ContractPacker"))
    } yield activityType

  private lazy val producerClassificationGen: Gen[ProducerClassification.Value] =
    for {
      producerClassification <- Gen.oneOf(
        ProducerClassification.withName("Unknown"),
        ProducerClassification.withName("Large"),
        ProducerClassification.withName("Small"))
    } yield producerClassification

  private lazy val levyDetailsGen: Gen[LevyDetails] = {
    activitiesGen |@|                         // activities
    Gen.boolean |@|                           // lessThanMillion
    producerClassificationGen.sometimes |@|   // producerClassification
    Gen.boolean |@|                           // smallProducerExemption
    Gen.boolean |@|                           // usesCopacker
    Gen.boolean                               // voluntarilyRegistered
  }.map(LevyDetails.apply)

  private lazy val contactDetailsGen: Gen[ContactDetails] = {
    Gen.forename() |@|                        // name
    Gen.alphaNumStr.sometimes |@|             // positionInCompany
    Gen.ukPhoneNumber |@|                     // telephoneNumber
    pattern"99999 999999".gen.sometimes |@|   // mobileNumber
    Gen.const("john.doe@somedomain.com")      // emailAddress
  }.map(ContactDetails.apply)

}
