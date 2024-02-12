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

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription(None).rarely.asMutable[String]

  def genSubscription(utr: Option[String]): Gen[Subscription] = {
    val genValues: String = utr.map(_.takeRight(5)).getOrElse("00000")
    for {
      generatedUtr    <- SdilNumberTransformer.tolerantUtr.gen
      orgName         <- orgNameGen
      orgType         <- Gen.some(Gen.oneOf("1", "2", "3", "4", "5"))
      address         <- addressGen
      activity        <- generatorForActivity(activity = genValues(1).asDigit, activityProdType = genValues(2).asDigit)
      liabilityDate   <- generatorForYearsOfLiability(genValues(3).asDigit)
      productionSites <- generatorForProdSites(genValues(4).asDigit % 3)
      warehouseSites  <- generatorForWarehouses(genValues(4).asDigit / 3)
      contact         <- contactGen
      deregDate       <- generatorForDeregDate(genValues(0).asDigit)
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
        contact,
        deregDate = deregDate)
    }
  }

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] =
    for {
      processingDate   <- Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC))
      formBundleNumber <- pattern"999999999999".gen
    } yield CreateSubscriptionResponse(processingDate, formBundleNumber)

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

  private lazy val addressGen: Gen[Address] = {
    Gen.ukAddress.map { lines =>
      UkAddress(lines.init, lines.last)
    }
  }

  private def activityGen(at: ActivityType.Value): Gen[(ActivityType.Value, LitreBands)] =
    for {
      l <- Gen.choose(0, maxL)
      h <- Gen.choose(0, maxL)
    } yield {
      at -> (l -> h)
    }

  private def generatorForActivity(activity: Int, activityProdType: Int): Gen[Activity] = {
    val isImporter = List(1, 3).contains(activity)
    val isCopacker = List(2, 3).contains(activity)
    val producedOwnBrand = List(2, 3, 5, 6).contains(activityProdType)
    val isCopackee = List(1, 3, 4, 6).contains(activityProdType)
    val isLarge = List(4, 5, 6).contains(activityProdType)

    for {
      produced  <- if (producedOwnBrand) Gen.some(activityGen(ProducedOwnBrand)) else Gen.const(None)
      imported  <- if (isImporter) Gen.some(activityGen(Imported)) else Gen.const(None)
      copacking <- if (isCopacker) Gen.some(activityGen(CopackerAll)) else Gen.const(None)
      copacked  <- if (isCopackee) Gen.some(activityGen(Copackee)) else Gen.const(None)
    } yield {
      InternalActivity(
        Seq(produced, imported, copacking, copacked).flatten.toMap,
        isLarge
      )
    }
  }

  private def generatorForYearsOfLiability(yearsOfLiability: Int): Gen[LocalDate] =
    Gen.date(
      LocalDate.now.minusYears(yearsOfLiability + 1),
      LocalDate.now().minusYears(yearsOfLiability).minusMonths(6)
    )

  private lazy val siteGen: Gen[Site] = for {
    address     <- addressGen
    ref         <- Gen.posNum[Int]
    tradingName <- orgNameGen
    closureDate <- Gen.date(LocalDate.now, LocalDate.now.plusYears(1))
  } yield Site(address, Some(ref.toString), Some(tradingName), Some(closureDate))

  private def generatorForProdSites(totalSites: Int): Gen[List[Site]] =
    Gen
      .const(totalSites)
      .flatMap(Gen.listOfN(_, siteGen))

  private def generatorForWarehouses(totalSites: Int): Gen[List[Site]] =
    Gen
      .const(totalSites)
      .flatMap(Gen.listOfN(_, siteGen))

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

  private lazy val jobTitleGen: Gen[String] = for {
    grade  <- Gen.oneOf(gradeList)
    sector <- Gen.oneOf(sectorList)
    role   <- Gen.oneOf(roleList)
  } yield s"$grade $sector $role"

  private def generatorForDeregDate(deregDateIndex: Int): Gen[Option[LocalDate]] = {
    if (deregDateIndex > 6) {
      val dateGen = Gen.date(
        LocalDate.now.minusYears(10 - deregDateIndex),
        LocalDate.now().minusYears(9 - deregDateIndex).minusMonths(6)
      )
      Gen.some(dateGen)
    } else {
      Gen.const(None)
    }
  }

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
