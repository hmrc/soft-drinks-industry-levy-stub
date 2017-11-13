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

import java.time.{LocalDateTime, ZoneOffset}

import cats.implicits._
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, CreateSubscriptionRequest] = genCreateSubscriptionRequest.asMutable[String]

  def genCreateSubscriptionRequest: Gen[CreateSubscriptionRequest] = {
    registrationGen                                          |@| // registration
    Gen.choose(1,5).flatMap {
      n => Gen.listOfN(n, siteGen)}.sometimes                |@| // sites
    Gen.choose(1,5).flatMap {
      n => Gen.listOfN(n, entityActionGen).sometimes
    }                                                            // entityAction
  }.map(CreateSubscriptionRequest.apply)

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] = {
    Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC))    |@| // processingDate
    pattern"999999999999".gen                                    // formBundleNumber
  }.map(CreateSubscriptionResponse.apply)

  def genCorrelationIdHeader: Gen[String] = {
    Gen.listOfN(
      36,
      Gen.frequency(
        (3,Gen.alphaUpperChar),
        (3,Gen.alphaLowerChar),
        (3,Gen.numChar),
        (1, Gen.const("-"))
      )
    ).map(_.mkString)                                            // correlationId
  }

  private lazy val entityActionGen: Gen[EntityAction] = {
    Gen.const("1")                                           |@| // action
    Gen.const("4")                                           |@| // entityType
    Gen.oneOf("1","2","3","4","5")                           |@| // organisationType
    cinGen                                                   |@| // cin
    Gen.alphaLowerStr                                        |@| // tradingName
    businessContactGen                                           // businessContact
  }.map(EntityAction.apply)

  private lazy val cinGen = {
    Gen.choose(1,15).flatMap { no =>
      Gen.listOfN[Char](no, Gen.alphaNumChar).map(_.mkString)
    }
  }

  private lazy val registrationGen: Gen[Registration] = {
    Gen.oneOf("1","2","3","4","5")                           |@| // organisationType
    Gen.date(2014, 2017)                                     |@| // applicationDate
    Gen.date(2014, 2017)                                     |@| // taxStartDate
    cinGen                                                   |@| // cin
    Gen.alphaLowerStr                                        |@| // tradingName
    businessContactGen                                       |@| // businessContact
    correspondenceContactGen                                 |@| // correspondenceContact
    primaryPersonContactGen                                  |@| // primaryPerson
    detailsGen                                               |@| // details
    litresProducedGen.sometimes                              |@| // sdilActivity
    Gen.choose(1d, 10000d).map(BigDecimal.valueOf)           |@| // estimatedTaxAmount
    Gen.date(2017, 2020)                                         // taxObligationStartDate
  }.map(Registration.apply)

  private lazy val siteGen: Gen[Site] = {
    Gen.const("1")                                           |@| // action
    Gen.alphaNumStr                                          |@| // tradingName
    Gen.alphaNumStr                                          |@| // newSiteReference
    businessContactGen                                       |@| // address
    Gen.oneOf("1","2")                                           // typeOfSite
  }.map(Site.apply)

  private lazy val addressGen: Gen[Address] = {
    Gen.boolean                                              |@| // notUKAddress
    Gen.alphaNumStr                                          |@| // line1
    Gen.alphaNumStr                                          |@| // line2
    Gen.alphaNumStr.sometimes                                |@| // line3
    Gen.alphaNumStr.sometimes                                |@| // line4
    Gen.alphaNumStr.sometimes                                |@| // postCode
    pattern"AA".gen.sometimes                                    // country
  }.map(Address.apply)

  private lazy val litresProducedGen: Gen[LitresProduced] = {
    Gen.choose(1, maxL).sometimes                            |@| // litresProducedUKHigher
    Gen.choose(1, maxL).sometimes                            |@| // litresProducedUKLower
    Gen.choose(1, maxL).sometimes                            |@| // litresImportedUKHigher
    Gen.choose(1, maxL).sometimes                            |@| // litresImportedUKLower
    Gen.choose(1, maxL).sometimes                            |@| // litresPackagedUKHigher
    Gen.choose(1, maxL).sometimes                                // litresPackagedUKLower
  }.map(LitresProduced.apply)

  private lazy val producerDetailsGen: Gen[ProducerDetails] = {
    Gen.boolean                                              |@| // produceMillionLitres
    Gen.oneOf("1","2")                                       |@| // producerClassification
    Gen.boolean.sometimes                                    |@| // smallProducerExemption
    Gen.boolean.sometimes                                    |@| // useContractPacker
    Gen.boolean.sometimes                                        // voluntarilyRegistered
  }.map(ProducerDetails.apply)

  private lazy val detailsGen: Gen[Details] = {
    Gen.boolean                                              |@| // producer
    producerDetailsGen.sometimes                             |@| // producerDetails
    Gen.boolean                                              |@| // importer
    Gen.boolean                                                  // contractPacker
  }.map(Details.apply)

  private lazy val contactDetailsGen: Gen[ContactDetails] = {
    pattern"99999 999999".gen                                |@| // telephone
    pattern"99999 999999".gen.sometimes                      |@| // mobile
    pattern"99999 999999".gen.sometimes                      |@| // fax
    Gen.const("john.doe@somedomain.com")                         // email
  }.map(ContactDetails.apply)

  private lazy val businessContactGen: Gen[BusinessContact] = {
    addressGen                                               |@| // addressDetails
    contactDetailsGen                                            // contactDetails
  }.map(BusinessContact.apply)

  private lazy val correspondenceContactGen: Gen[CorrespondenceContact] = {
    addressGen                                               |@| // addressDetails
    contactDetailsGen                                        |@| // contactDetails
    Gen.boolean.sometimes                                        // differentAddress
  }.map(CorrespondenceContact.apply)

  private lazy val primaryPersonContactGen: Gen[PrimaryPersonContact] = {
    Gen.alphaStr                                             |@| // name
    Gen.alphaStr.sometimes                                   |@| // positionInCompany
    Gen.ukPhoneNumber                                        |@| // telephone
    pattern"99999 999999".gen.sometimes                      |@| // mobile
    Gen.const("john.doe@somedomain.com")                         // email
  }.map(PrimaryPersonContact.apply)

  // n.b. leaving these although they are unused until we see the retrieve spec
//  private lazy val entityTypeGen: Gen[EntityType.Value] =
//    Gen.oneOf(EntityType.values.toSeq)
//
//  private lazy val actionTypeGen: Gen[ActionType.Value] =
//    Gen.oneOf(ActionType.values.toSeq)
//
//  private lazy val organisationTypeGen: Gen[OrganisationType.Value] =
//    Gen.oneOf(OrganisationType.values.toSeq)
//
//  private lazy val siteTypeGen: Gen[SiteType.Value] =
//    Gen.oneOf(SiteType.values.toSeq)
//
//  private lazy val siteActionGen: Gen[SiteAction.Value] =
//    Gen.oneOf(SiteAction.values.toSeq)
//
//  private lazy val activitiesGen: Gen[ActivityType.Value] =
//    Gen.oneOf(ActivityType.values.toSeq)
//
//  private lazy val producerClassificationGen: Gen[ProducerClassification.Value] =
//    Gen.oneOf(ProducerClassification.values.toSeq)

}


