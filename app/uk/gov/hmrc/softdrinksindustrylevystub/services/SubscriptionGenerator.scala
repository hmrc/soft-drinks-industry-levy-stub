/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.models.maxL
import ActivityType._
import uk.gov.hmrc.softdrinksindustrylevystub.models.CreateSubscriptionResponse
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription.asMutable[String]

  def genSubscription: Gen[Option[Subscription]] = {
    SdilNumberTransformer.tolerantUtr.gen |@|
      orgNameGen |@|
      Gen.oneOf("1", "2", "3", "4", "5").almostAlways |@|
      addressGen |@|
      internalActivityGen |@|
      Gen.date(2018, 2028) |@|
      Gen.choose(0, 10).flatMap(Gen.listOfN(_, siteGen)) |@|
      Gen.choose(0, 10).flatMap(Gen.listOfN(_, siteGen)) |@|
      contactGen
  } map { Subscription.apply } rarely

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] = {
    Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC)) |@| // processingDate
      pattern"999999999999".gen // formBundleNumber
  }.map(CreateSubscriptionResponse.apply)

  private lazy val internalActivityGen: Gen[Activity] = for {
    produced <- activityGen(ProducedOwnBrand).sometimes
    imported <- activityGen(Imported).sometimes
    copacking <- activityGen(CopackerAll).sometimes
    copacked <- activityGen(Copackee).sometimes
    isLarge <- Gen.boolean
  } yield {
    InternalActivity(
      Seq(produced, imported, copacking, copacked).flatten.toMap,
      isLarge
    )
  }

  private def activityGen(at: ActivityType.Value): Gen[(ActivityType.Value, LitreBands)] = for {
    l <- Gen.choose(0, maxL)
    h <- Gen.choose(0, maxL)
  } yield {
    at -> (l -> h)
  }

  private lazy val contactGen: Gen[Contact] = for {
    fname <- Gen.forename().usually
    lname <- Gen.surname
    position <- jobTitleGen.usually
    phoneNumber <- pattern"9999 999999"
    email <- genEmail
  } yield {
    Contact(fname.map(f => s"$f $lname"), position, "0" + phoneNumber, email)
  }

  private lazy val genEmail: Gen[String] = for {
    fname <- Gen.forename()
    lname <- Gen.surname
    domain <- Gen.oneOf("gmail", "outlook", "yahoo", "mailinator", "example")
    tld <- Gen.oneOf("com", "co.uk")
  } yield {
    s"$fname.$lname@$domain.$tld"
  }

  private lazy val siteGen: Gen[Site] = for {
    address <- addressGen
    ref <- Gen.alphaStr.almostAlways
  } yield Site(address, ref)

  private lazy val addressGen: Gen[Address] = {
    Gen.ukAddress.map { lines =>
      UkAddress(lines.init, lines.last)
    }
  }

  // TODO: use smart stub for these
  private lazy val orgNameGen: Gen[String] = Gen.oneOf(orgNames)

  private lazy val orgNames: Seq[String] = Seq(
    "All-Natural Gluten-free Vegan Organic Drinks Ltd",
    "All-Artifical Soft Drinks Inc",
    "Adam's Dyes (Also Soft Drinks)"
  )

  private lazy val jobTitleGen: Gen[String] = for {
    grade <- Gen.oneOf(gradeList)
    sector <- Gen.oneOf(sectorList)
    role <- Gen.oneOf(roleList)
  } yield s"$grade $sector $role"

  private lazy val gradeList = Seq(
    "Senior",
    "Corporate",
    "Central",
    "Regional",
    "Global",
    "Chief",
    "Executive",
    "Trainee",
    "Lead",
    "General",
    "Junior",
    "Apprentice",
    "Enterprise"
  )

  private lazy val sectorList = Seq(
    "Blockchain",
    "Brand",
    "Business",
    "Data",
    "Finance",
    "Infrastructure",
    "Intranet",
    "Marketing",
    "Pharmaceutical",
    "Sales",
    "Security",
    "Technical",
    "Product",
    "Risk",
    "Hiring",
    "Meeting",
    "Testing",
    "Agile",
    "Software",
    "Digital"
  )

  private lazy val roleList = Seq(
    "Officer",
    "Agent",
    "Administrator",
    "Assistant",
    "Advisor",
    "Architect",
    "Supervisor",
    "Engineer",
    "Artisan",
    "Analyst",
    "Distributor",
    "Consultant",
    "Manager"
  )

}


