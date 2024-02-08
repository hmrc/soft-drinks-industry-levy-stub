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

package uk.gov.hmrc.softdrinksindustrylevystub.services

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import org.scalacheck._
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.ActivityType._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.models.{CreateSubscriptionResponse, maxL}

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription().rarely.asMutable[String]

  private def generatorForYearsOfLiability(yearsOfLiability: Int): Gen[LocalDate] = {
    //    B - YEARS OF LIABILITY 0-4
    ???
  }

  private def generatorForActivity(activity: Int, activityProdType: Int): Gen[Activity] = {
    //    C - ACTIVITY 0-3
    //    0 = neither importer nor copacker
    //	1 = importer
    //	2 = copacker
    //	3 = importer and copacker
    //    D - ACTIVITY PROD TYPE 0-3
    //    0 = none
    //    1 = voluntary and small
    //    2 = small prod
    //      3 = large
    ???
  }

  private def generatorForProdSites(sitesIndex: Int): Gen[List[Site]] = {
    //    E - WAREHOUSES/PROD SITES 0 -8
    //    0 = 0 p 0 w
    //      1 = 1 p 0 w
    //      2 = 2 p 0 w
    //      3 = 0 p 1 w
    //      4 = 1 p 1 w
    //      5 = 2 p 1 w
    //      6 = 0 p 2 w
    //      7 = 1 p 2 w
    //      8 = 2 p 2 w (edited)
    ???
  }

  private def generatorForWarehouses(sitesIndex: Int): Gen[List[Site]] = {
    //    E - WAREHOUSES/PROD SITES 0 -8
    //    0 = 0 p 0 w
    //      1 = 1 p 0 w
    //      2 = 2 p 0 w
    //      3 = 0 p 1 w
    //      4 = 1 p 1 w
    //      5 = 2 p 1 w
    //      6 = 0 p 2 w
    //      7 = 1 p 2 w
    //      8 = 2 p 2 w (edited)
    ???
  }

  def genSubscriptionNEW(utr: Option[String]): Gen[Subscription] = {
    //    TODO: UTR 00A00BCDE , ONLY GENERATE ONCE
    //    A - REGISTRATION 0-9
//    REGISTRATION COVERED ELSEWHERE IN SDIL NUMBER TRANSFORMER
    val genValues: String = utr.map(_.takeRight(4)).getOrElse("0000")
    for {
      generatedUtr <- SdilNumberTransformer.tolerantUtr.gen
      orgName <- orgNameGen
      orgType <- Gen.oneOf("1", "2", "3", "4", "5").almostAlways
      address <- addressGen
      liabilityDate <- generatorForYearsOfLiability(genValues(0).toInt)
      activity <- generatorForActivity(activity = genValues(1).toInt, activityProdType = genValues(2).toInt)
      productionSites <- generatorForProdSites(genValues(3).toInt)
      warehouseSites <- generatorForWarehouses(genValues(3).toInt)
      contact <- contactGen
    } yield {
      Subscription(
        utr.getOrElse(generatedUtr),
        orgName,
        orgType,
        address,
        activity,
        liabilityDate,
        productionSites,
        warehouseSites,
        contact)
    }
  }

  def genSubscription(passedUtr: Option[String] = None): Gen[Subscription] =
    for {
      generatedUtr       <- SdilNumberTransformer.tolerantUtr.gen
      orgName            <- orgNameGen
      orgType            <- Gen.oneOf("1", "2", "3", "4", "5").almostAlways
      address            <- addressGen
      activity           <- internalActivityGen
      shortLiabilityDate <- Gen.date(LocalDate.now.minusYears(1), LocalDate.now().minusMonths(6))
      longLiabilityDate  <- Gen.date(LocalDate.now.minusYears(4), LocalDate.now().minusYears(1).minusMonths(6))
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
      val utr = passedUtr.getOrElse(generatedUtr)
      val liabilityDate = if (utr.takeRight(4).toInt > 3000) longLiabilityDate else shortLiabilityDate
      Subscription(
        generatedUtr,
        orgName,
        orgType,
        address,
        activity,
        liabilityDate,
        productionSites,
        warehouseSites,
        contact)
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
    closureDate <- Gen.date(2016, LocalDate.now.getYear + 1)
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
