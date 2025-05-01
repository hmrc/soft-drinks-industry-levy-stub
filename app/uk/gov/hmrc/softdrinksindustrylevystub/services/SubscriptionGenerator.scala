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

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription.rarely.asMutable[String]

  def genSubscription: Gen[Subscription] =
    for {
      utr           <- SdilNumberTransformer.tolerantUtr.gen
      orgName       <- orgNameGen
      orgType       <- Gen.oneOf("1", "2", "3", "4", "5").almostAlways
      address       <- addressGen
      activity      <- internalActivityGenForUTR(utr)
      liabilityDate <- Gen.date(LocalDate.now.minusYears(1), LocalDate.now().minusMonths(6))
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
    } yield Subscription(
      utr,
      orgName,
      orgType,
      address,
      activity,
      liabilityDate,
      productionSites,
      warehouseSites,
      contact
    )

  def genCreateSubscriptionResponse: Gen[CreateSubscriptionResponse] =
    for {
      processingDate   <- Gen.const(LocalDateTime.now.atOffset(ZoneOffset.UTC))
      formBundleNumber <- pattern"999999999999".gen
    } yield CreateSubscriptionResponse(processingDate, formBundleNumber)

  private def createActivity(data: (ActivityType.Value, (Long, Long))*)(isLarge: Boolean): InternalActivity =
    InternalActivity(data.toMap, isLarge)

  private val internalActivityValue: Map[String, InternalActivity] = {
    val entries = Seq(
      "5168983848" -> createActivity( // UTR for 0000001611
        ProducedOwnBrand -> (8592932967685L, 393857365208L),
        CopackerAll      -> (6911728073310L, 9693701140898L),
        Copackee         -> (7732342042367L, 1761987975555L)
      )(isLarge = false),
      "2701345062" -> createActivity( // UTR for 0000000437
        ProducedOwnBrand -> (2435810187383L, 4849497916977L),
        Imported         -> (2145648045812L, 6153117498101L),
        CopackerAll      -> (4066616847404L, 2728740244143L)
      )(isLarge = true),
      "4035769414" -> createActivity( // UTR for 0000000069
        Imported -> (6460796745282L, 6336758185953L),
        Copackee -> (702818577689L, 5022677436353L)
      )(isLarge = false),
      "7296987670" -> createActivity( // UTR for 000000116
        CopackerAll -> (3893666227421L, 6092530156211L),
        Copackee    -> (1547794270596L, 4764940090984L)
      )(isLarge = false),
      "5361150980" -> createActivity( // UTR for 0000000336
        CopackerAll -> (7454210094255L, 3729851344296L),
        Copackee    -> (1484349966325L, 7202971074099L)
      )(isLarge = false)
    )
    entries.toMap
  }

  def internalActivityGenForUTR(ref: String): Gen[InternalActivity] =
    internalActivityValue.get(ref) match {
      case Some(activity) =>
        Gen.const(activity)

      case None =>
        for {
          produced  <- activityGen(ProducedOwnBrand).sometimes
          imported  <- activityGen(Imported).sometimes
          copacking <- activityGen(CopackerAll).sometimes
          copacked  <- activityGen(Copackee).sometimes
          isLarge   <- Gen.boolean
        } yield InternalActivity(
          Seq(produced, imported, copacking, copacked).flatten.toMap,
          isLarge
        )
    }

  private def activityGen(at: ActivityType.Value): Gen[(ActivityType.Value, LitreBands)] =
    for {
      l <- Gen.choose(0: Long, maxL)
      h <- Gen.choose(0: Long, maxL)
    } yield at -> (l -> h)

  private lazy val contactGen: Gen[Contact] = for {
    fname       <- Gen.forename()
    lname       <- Gen.surname
    position    <- jobTitleGen
    phoneNumber <- pattern"9999 999999"
    email       <- genEmail
  } yield Contact(Some(s"$fname $lname"), Some(position), "0" + phoneNumber, email)

  private lazy val genEmail: Gen[String] = for {
    fname  <- Gen.forename()
    lname  <- Gen.surname
    domain <- Gen.oneOf("gmail", "outlook", "yahoo", "mailinator", "example")
    tld    <- Gen.oneOf("com", "co.uk")
  } yield s"$fname.$lname@$domain.$tld"

  private lazy val siteGen: Gen[Site] = for {
    address     <- addressGen
    ref         <- Gen.posNum[Int]
    tradingName <- orgNameGen
    closureDate <- Gen.date(2016, LocalDate.now.getYear + 1)
  } yield Site(address, Some(ref.toString), Some(tradingName.toString), Some(closureDate))

  private lazy val addressGen: Gen[Address] =
    Gen.ukAddress.map { lines =>
      UkAddress(lines.init, lines.last)
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
           "Key"
         )
    b <- Gen.oneOf("Cola", "Juice", "Can", "Drinks", "Products", "Bottle", "Confectionry", "Lemonade")
    c <- Gen.oneOf("Plc", "Ltd", "Group")
  } yield s"$a $b $c"

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
