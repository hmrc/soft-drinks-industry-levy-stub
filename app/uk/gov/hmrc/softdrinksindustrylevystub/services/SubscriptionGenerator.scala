/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import org.scalacheck._
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.ActivityType._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.models.{CreateSubscriptionResponse, maxL}
import cats.implicits._

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription.rarely.asMutable[String]

  def genSubscription: Gen[Subscription] =
    for {
      utr           <- SdilNumberTransformer.tolerantUtr.gen
      orgName       <- orgNameGen
      orgType       <- Gen.oneOf("1", "2", "3", "4", "5").almostAlways
      address       <- addressGen
      activity      <- internalActivityGen
      liabilityDate <- Gen.date(LocalDate.of(2018, 4, 15), LocalDate.of(2018, 7, 3))
      productionSites <- if (activity.isLarge || activity.isContractPacker)
                          Gen
                            .choose(1, 10)
                            .flatMap(Gen.listOfN(_, siteGen))
                            .retryUntil(_.exists(_.closureDate.forall(_.isAfter(LocalDate.now))))
                        else
                          Gen.const(Nil)
      warehouseSites <- if (activity.isVoluntaryRegistration)
                         Gen.const(Nil)
                       else
                         Gen
                           .choose(1, 10)
                           .flatMap(Gen.listOfN(_, siteGen))
                           .retryUntil(_.exists(_.closureDate.forall(_.isAfter(LocalDate.now))))
      contact <- contactGen
    } yield {
      Subscription(utr, orgName, orgType, address, activity, liabilityDate, productionSites, warehouseSites, contact)
    }

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] =
    for {
      processingDate   <- Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC))
      formBundleNumber <- pattern"999999999999".gen
    } yield CreateSubscriptionResponse(processingDate, formBundleNumber)

  private lazy val internalActivityGen: Gen[Activity] = for {
    produced  <- activityGen(ProducedOwnBrand).sometimes
    imported  <- activityGen(Imported).sometimes
    copacking <- activityGen(CopackerAll).sometimes
    copacked  <- activityGen(Copackee).sometimes
    isLarge   <- Gen.boolean
  } yield {
    InternalActivity(
      Seq(produced, imported, copacking, copacked).flatten.toMap,
      isLarge
    )
  }

  private def activityGen(at: ActivityType.Value): Gen[(ActivityType.Value, LitreBands)] =
    for {
      l <- Gen.choose(0, maxL)
      h <- Gen.choose(0, maxL)
    } yield {
      at -> (l -> h)
    }

  private lazy val contactGen: Gen[Contact] = for {
    fname       <- Gen.forename()
    lname       <- Gen.surname
    position    <- jobTitleGen
    phoneNumber <- pattern"9999 999999"
    email       <- genEmail
  } yield {
    Contact(Some(s"$fname $lname"), Some(position), "0" + phoneNumber, email)
  }

  private lazy val genEmail: Gen[String] = for {
    fname  <- Gen.forename()
    lname  <- Gen.surname
    domain <- Gen.oneOf("gmail", "outlook", "yahoo", "mailinator", "example")
    tld    <- Gen.oneOf("com", "co.uk")
  } yield {
    s"$fname.$lname@$domain.$tld"
  }

  private lazy val siteGen: Gen[Site] = for {
    address     <- addressGen
    ref         <- Gen.posNum[Int]
    tradingName <- orgNameGen
    closureDate <- Gen.date(2016, 2024)
  } yield Site(address, Some(ref.toString), Some(tradingName.toString), Some(closureDate))

  private lazy val addressGen: Gen[Address] = {
    Gen.ukAddress.map { lines =>
      UkAddress(lines.init, lines.last)
    }
  }

  private lazy val orgNameGen: Gen[String] = for {
    a <- Gen.oneOf(
          "Vivid",
          "Vegan",
          "Soft",
          "Star",
          "Verdant",
          "Monster",
          "Highly Addictive",
          "Frenzy",
          "Wild",
          "Party",
          "Fire",
          "Lightning",
          "Crackling",
          "Mega",
          "Super",
          "Key")
    b <- Gen.oneOf("Cola", "Juice", "Can", "Drinks", "Products", "Bottle", "Confectionry", "Lemonade")
    c <- Gen.oneOf("Plc", "Ltd", "Group")
  } yield (s"$a $b $c")

  private lazy val jobTitleGen: Gen[String] = for {
    grade  <- Gen.oneOf(gradeList)
    sector <- Gen.oneOf(sectorList)
    role   <- Gen.oneOf(roleList)
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
