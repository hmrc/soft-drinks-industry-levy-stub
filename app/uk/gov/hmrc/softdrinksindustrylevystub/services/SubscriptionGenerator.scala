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
    Gen.date(
      LocalDate.now.minusYears(yearsOfLiability + 1),
      LocalDate.now().minusYears(yearsOfLiability).minusMonths(6)
    )
  }

  private def generatorForActivity(activity: Int, activityProdType: Int): Gen[Activity] = {
//    TODO: FILL IN CORRECT ACTIVITY VALUES HERE
    val ownBrandGen = activityGen(ProducedOwnBrand)
    val importedGen = activityGen(Imported)
    val copackerAllGen = activityGen(CopackerAll)
    val copackeeGen = activityGen(Copackee)
    val activityRequired = (activity, activityProdType) match {
      case (0, 0) => {
        ("neither importer nor copacker", "none")
        //        ownBrand NO
        //        imported NO
        //        copackerAll NO
        //        copackee NO
        //        isLarge NO
      }
      case (0, 1) => {
        ("neither importer nor copacker", "small prod - copackee")
        //        ownBrand NO
        //        imported NO
        //        copackerAll NO
        //        copackee YES
        //        isLarge NO
      }
      case (0, 2) => {
        ("neither importer nor copacker", "small prod - produced own brands")
        //        ownBrand YES
        //        imported NO
        //        copackerAll NO
        //        copackee NO
        //        isLarge NO
      }
      case (0, 3) => {
        ("neither importer nor copacker", "large - produced own brands and copackee")
        //        ownBrand
        //        imported NO
        //        copackerAll NO
        //        copackee YES
        //        isLarge YES
      }
      case (1, 0) => {
        ("importer", "none")
        //        ownBrand NO
        //        imported YES
        //        copackerAll NO
        //        copackee NO
        //        isLarge NO
      }
      case (1, 1) => {
        ("importer", "small prod - copackee")
        //        ownBrand NO
        //        imported YES
        //        copackerAll NO
        //        copackee YES
        //        isLarge NO
      }
      case (1, 2) => {
        ("importer", "small prod - produced own brands")
        //        ownBrand YES
        //        imported YES
        //        copackerAll NO
        //        copackee NO
        //        isLarge NO
      }
      case (1, 3) => {
        ("importer", "large - produced own brands and copackee")
        //        ownBrand YES
        //        imported YES
        //        copackerAll NO
        //        copackee YES
        //        isLarge YES
      }
      case (2, 0) => {
        ("copacker", "none")
        //        ownBrand NO
        //        imported NO
        //        copackerAll YES
        //        copackee NO
        //        isLarge NO
      }
      case (2, 1) => {
        ("copacker", "small prod - copackee")
        //        ownBrand NO
        //        imported NO
        //        copackerAll YES
        //        copackee YES
        //        isLarge NO
      }
      case (2, 2) => {
        ("copacker", "small prod - produced own brands")
        //        ownBrand YES
        //        imported NO
        //        copackerAll YES
        //        copackee NO
        //        isLarge NO
      }
      case (2, 3) => {
        ("copacker", "large - produced own brands and copackee")
        //        ownBrand YES
        //        imported NO
        //        copackerAll YES
        //        copackee YES
        //        isLarge YES
      }
      case (3, 0) => {
        ("importer and copacker", "none")
        //        ownBrand NO
        //        imported YES
        //        copackerAll YES
        //        copackee NO
        //        isLarge NO
      }
      case (3, 1) => {
        ("importer and copacker", "small prod - copackee")
        //        ownBrand NO
        //        imported YES
        //        copackerAll YES
        //        copackee YES
        //        isLarge NO
      }
      case (3, 2) => {
        ("importer and copacker", "small prod - produced own brands")
        //        ownBrand YES
        //        imported YES
        //        copackerAll YES
        //        copackee NO
        //        isLarge NO
      }
      case (3, 3) => {
        ("importer and copacker", "large - produced own brands and copackee")
        //        ownBrand YES
        //        imported YES
        //        copackerAll YES
        //        copackee YES
        //        isLarge YES
      }
    }
    internalActivityGen
  }

  private lazy val siteGenNEW: Gen[Site] = for {
    address <- addressGen
    ref <- Gen.posNum[Int]
    tradingName <- orgNameGen
    closureDate <- Gen.date(LocalDate.now, LocalDate.now.plusYears(1))
  } yield Site(address, Some(ref.toString), Some(tradingName.toString), Some(closureDate))

  private def generatorForProdSites(totalSites: Int, activity: Activity): Gen[List[Site]] = {
    if ((activity.isLarge || activity.isContractPacker) && totalSites > 0)
      Gen
        .const(totalSites)
        .flatMap(Gen.listOfN(_, siteGen))
        .retryUntil(_.exists(_.closureDate.forall(_.isAfter(LocalDate.now))))
    else
      Gen.const(Nil)
  }

  private def generatorForWarehouses(totalSites: Int, activity: Activity): Gen[List[Site]] = {
    if (activity.isVoluntaryRegistration && totalSites > 0)
      Gen.const(Nil)
    else
      Gen
        .const(totalSites)
        .flatMap(Gen.listOfN(_, siteGen))
        .retryUntil(_.exists(_.closureDate.forall(_.isAfter(LocalDate.now))))
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
      productionSites <- generatorForProdSites((genValues(3).toInt + 1) % 3, activity)
      warehouseSites <- generatorForWarehouses((genValues(3).toInt + 1) / 3, activity)
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
