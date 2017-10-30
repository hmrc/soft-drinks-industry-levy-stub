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

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, CreateSubscriptionRequest] = genCreateSubscriptionRequest.asMutable[String]

  def genCreateSubscriptionRequest: Gen[CreateSubscriptionRequest] = {
    Gen.oneOf("1","2","3","4","5") |@|                                       // organisationType
    Gen.date(2014, 2017) |@|                                      // applicationDate
    Gen.date(2014, 2017) |@|                                      // taxStartDate
    pattern"999999999999" |@|                                     // cin
    Gen.alphaLowerStr |@|                                         // tradingName
    businessContactGen |@|                                                // businessContact
    correspondenceContactGen |@|                                  // correspondenceContact
    primaryPersonContactGen |@|                                         // primaryPerson
    detailsGen |@|                                            // details
    litresProducedGen |@|                                         // sdilActivity
    Gen.choose(1d, 10000d).map(BigDecimal.valueOf).sometimes |@|  // estimatedTaxAmount
    Gen.date(2017, 2020) |@|                                      // taxObligationStartDate
    bankDetailsGen |@|                                            // bankDetails
    Gen.choose(1,5).flatMap { n => Gen.listOfN(n, siteGen)}       // sites
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
    Gen.boolean |@|                     // notUKAddress
    Gen.alphaNumStr |@|                 // line1
    Gen.alphaNumStr |@|                 // line2
    Gen.alphaNumStr.sometimes |@|       // line3
    Gen.alphaNumStr.sometimes |@|       // line4
    Gen.alphaNumStr.sometimes |@|       // postCode
    Gen.alphaNumStr.sometimes           // country
  }.map(Address.apply)

  private lazy val bankDetailsGen: Gen[BankDetails] = {
    Gen.boolean |@|                     // directDebit
    Gen.alphaNumStr.sometimes |@|       // accountName
    Gen.alphaNumStr.sometimes |@|       // accountNumber
    Gen.alphaNumStr.sometimes |@|       // sortCode
    Gen.alphaNumStr.sometimes           // buildingSocietyRollNumber
  }.map(BankDetails.apply)

  private lazy val litresProducedGen: Gen[LitresProduced] = {
    Gen.choose(1, 1000000).sometimes |@|  // litresProducedUKHigher
    Gen.choose(1, 1000000).sometimes |@|  // litresProducedUKLower
    Gen.choose(1, 1000000).sometimes |@|  // litresImportedUKHigher
    Gen.choose(1, 1000000).sometimes |@|  // litresImportedUKLower
    Gen.choose(1, 1000000).sometimes |@|  // litresPackagedUKHigher
    Gen.choose(1, 1000000).sometimes      // litresPackagedUKLower
  }.map(LitresProduced.apply)

  private lazy val entityTypeGen: Gen[EntityType.Value] =
    Gen.oneOf(EntityType.values.toSeq)

  private lazy val actionTypeGen: Gen[ActionType.Value] =
    Gen.oneOf(ActionType.values.toSeq)

  private lazy val organisationTypeGen: Gen[OrganisationType.Value] =
    Gen.oneOf(OrganisationType.values.toSeq)

  private lazy val siteTypeGen: Gen[SiteType.Value] =
    Gen.oneOf(SiteType.values.toSeq)

  private lazy val siteActionGen: Gen[SiteAction.Value] =
    Gen.oneOf(SiteAction.values.toSeq)

  private lazy val activitiesGen: Gen[ActivityType.Value] =
    Gen.oneOf(ActivityType.values.toSeq)

  private lazy val producerClassificationGen: Gen[ProducerClassification.Value] =
    Gen.oneOf(ProducerClassification.values.toSeq)

  private lazy val producerDetailsGen: Gen[ProducerDetails] = {
    Gen.boolean |@|
    Gen.oneOf("1","2") |@|
    Gen.boolean.sometimes |@|
    Gen.boolean.sometimes |@|
    Gen.boolean.sometimes
  }.map(ProducerDetails.apply)

  private lazy val detailsGen: Gen[Details] = {
    Gen.boolean |@|
    producerDetailsGen.sometimes |@|
    Gen.boolean |@|
    Gen.boolean
  }.map(Details.apply)

  private lazy val contactDetailsGen: Gen[ContactDetails] = {
    Gen.ukPhoneNumber |@|                     // telephone
    pattern"99999 999999".gen.sometimes |@|   // mobile
    pattern"99999 999999".gen.sometimes |@|   // fax TODO number pattern now includes +-() see DES spec
    Gen.const("john.doe@somedomain.com")      // email
  }.map(ContactDetails.apply)

  private lazy val businessContactGen: Gen[BusinessContact] = {
    addressGen             |@|
    contactDetailsGen
  }.map(BusinessContact.apply)

  private lazy val correspondenceContactGen: Gen[CorrespondenceContact] = {
    addressGen             |@|
    contactDetailsGen      |@|
    Gen.boolean.sometimes
  }.map(CorrespondenceContact.apply)

  private lazy val primaryPersonContactGen: Gen[PrimaryPersonContact] = {
    Gen.alphaStr           |@|
    Gen.alphaStr.sometimes           |@|
    Gen.ukPhoneNumber |@|                     // telephone
    pattern"99999 999999".gen.sometimes |@|   // mobile
    pattern"99999 999999".gen.sometimes |@|   // fax TODO number pattern now includes +-() see DES spec
    Gen.const("john.doe@somedomain.com")      // email
  }.map(PrimaryPersonContact.apply)
}
