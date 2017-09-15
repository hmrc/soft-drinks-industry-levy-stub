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

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

import com.google.inject.Singleton
import uk.gov.hmrc.smartstub.{AdvGen, _}
import cats.implicits._
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.softdrinksindustrylevystub.models.DesSubmissionResult
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub._
import cats.implicits._
import org.scalacheck.support.cats._
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum

@Singleton
class DesSubmissionService {


  lazy val store = genCreateSubscriptionRequest.asMutable[String]

  def createSubscriptionResponse(data: CreateSubscriptionRequest): CreateSubscriptionResponse = {
    store(data.customerIdentificationNumber) = data
    genCreateSubscriptionResponse.seeded(data.customerIdentificationNumber).get
  }

  def retrieveSubscriptionDetails(utr: String) = {
    store.get(utr)
  }

  def genCreateSubscriptionRequest: Gen[CreateSubscriptionRequest] = {
    organisationTypeGen |@|
    actionTypeGen.sometimes |@|
    entityTypeGen.sometimes |@|
    Gen.date(2014, 2017) |@|
    Gen.date(2014, 2017) |@|
    Gen.date(2014, 2017).sometimes |@|
    Gen.date(2014, 2017).sometimes |@|
    pattern"999999999999" |@|
    Gen.alphaLowerStr |@| // TODO get company name generator version of smart stubs (this is supposed to be trading name)
    addressGen |@|
    Gen.boolean |@|
    addressGen.sometimes |@|
    contactDetailsGen |@|
    levyDetailsGen |@|
    litresProducedGen |@|
    Gen.choose(1, 100000).sometimes |@|
    Gen.date(2017, 2020) |@|
    bankDetailsGen |@|
    Gen.choose(1,5).flatMap { n => Gen.listOfN(n, siteGen)}
  }.map(CreateSubscriptionRequest.apply)

  lazy val siteGen: Gen[Site] = {
    siteActionGen.sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.date(2017, 2020).sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    addressGen |@| // TODO talk to LT about changing private state of ukAddress components
    siteTypeGen.sometimes
  }.map(Site.apply)

  lazy val addressGen: Gen[Address] = {
    Gen.boolean |@|
      Gen.alphaNumStr |@|
      Gen.alphaNumStr |@|
      Gen.alphaNumStr.sometimes |@|
      Gen.alphaNumStr.sometimes |@|
      Gen.alphaNumStr |@|
      Gen.alphaNumStr.sometimes |@|
      Gen.alphaNumStr |@|
      Gen.alphaNumStr.sometimes |@|
      Gen.alphaNumStr |@|
      Gen.alphaNumStr.sometimes
  }.map(Address.apply)

  lazy val bankDetailsGen: Gen[BankDetails] = {
    Gen.boolean |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.alphaNumStr.sometimes
  }.map(BankDetails.apply)

  lazy val litresProducedGen: Gen[LitresProduced] = {
    Gen.choose(1, 1000000).sometimes |@|
    Gen.choose(1, 1000000).sometimes |@|
    Gen.choose(1, 1000000).sometimes |@|
    Gen.choose(1, 1000000).sometimes |@|
    Gen.choose(1, 1000000).sometimes |@|
    Gen.choose(1, 1000000).sometimes
  }.map(LitresProduced.apply)

  lazy val entityTypeGen: Gen[EntityType.Value] =
    for {
      entityType <- Gen.oneOf(
        EntityType.withName("Unknown"),
        EntityType.withName("GroupMember"),
        EntityType.withName("GroupRepresentativeMember"),
        EntityType.withName("ControllingBody"),
        EntityType.withName("Partner"))
    } yield entityType

  lazy val actionTypeGen: Gen[ActionType.Value] =
    for {
      actionType <- Gen.oneOf(
        ActionType.withName("Unknown"),
        ActionType.withName("Add"),
        ActionType.withName("Amend"),
        ActionType.withName("Remove"))
    } yield actionType

  lazy val organisationTypeGen: Gen[OrganisationType.Value] =
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

  lazy val siteTypeGen: Gen[SiteType.Value] =
    for {
      siteType <- Gen.oneOf(
        SiteType.withName("Unknown"),
        SiteType.withName("Warehouse"),
        SiteType.withName("ProductionSite"))
    } yield siteType

  lazy val siteActionGen: Gen[SiteAction.Value] =
    for {
      siteAction <- Gen.oneOf(
        SiteAction.withName("Unknown"),
        SiteAction.withName("NewSite"),
        SiteAction.withName("AmendSite"),
        SiteAction.withName("CloseSite"),
        SiteAction.withName("TransferSite"))
    } yield siteAction

  val activitiesGen: Gen[ActivityType.Value] =
    for {
      activityType <- Gen.oneOf(
        ActivityType.withName("Unknown"),
        ActivityType.withName("Producer"),
        ActivityType.withName("Importer"),
        ActivityType.withName("ContractPacker"))
    } yield activityType

  val producerClassificationGen: Gen[ProducerClassification.Value] =
    for {
      producerClassification <- Gen.oneOf(
        ProducerClassification.withName("Unknown"),
        ProducerClassification.withName("Large"),
        ProducerClassification.withName("Small"))
    } yield producerClassification

  val levyDetailsGen: Gen[LevyDetails] = {
    activitiesGen |@|
    Gen.boolean |@|
    producerClassificationGen.sometimes |@|
    Gen.boolean |@|
    Gen.boolean |@|
    Gen.boolean
  }.map(LevyDetails.apply)

  val contactDetailsGen: Gen[ContactDetails] = {
    Gen.forename() |@|
    Gen.alphaNumStr.sometimes |@|
    Gen.ukPhoneNumber |@|
    pattern"99999 999999".gen.sometimes |@|
    Gen.const("john.doe@somedomain.com")
  }.map(ContactDetails.apply)

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] = {
    pattern"99999999".gen |@|
    pattern"99999999".gen
  }.map(CreateSubscriptionResponse.apply)

  def buildResponse(): DesSubmissionResult = DesSubmissionResult(true)
}

