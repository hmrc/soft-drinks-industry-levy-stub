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
import org.scalacheck.*
import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.softdrinksindustrylevystub.Store.{sdilToUtr, utrToSdil}
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.ActivityType.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.{CreateSubscriptionResponse, maxL}

object SubscriptionGenerator {

  lazy val store: PersistentGen[String, Option[Subscription]] = genSubscription.rarely.asMutable[String]

  def genSubscription: Gen[Subscription] =
    for {
      utr           <- SdilNumberTransformer.tolerantUtr.gen
      orgName       <- orgNameGen
      orgType       <- Gen.oneOf("1", "2", "3", "4", "5").almostAlways
      address       <- addressGen
      activity      <- internalActivityGenForRef(utr)
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

//  private lazy val internalActivityGen: Gen[Activity] = for {
//    produced  <- activityGen(ProducedOwnBrand).sometimes
//    imported  <- activityGen(Imported).sometimes
//    copacking <- activityGen(CopackerAll).sometimes
//    copacked  <- activityGen(Copackee).sometimes
//    isLarge   <- Gen.boolean
//  } yield InternalActivity(
//    Seq(produced, imported, copacking, copacked).flatten.toMap,
//    isLarge
//  )

//  def internalActivityGenForRef(ref: String): Gen[InternalActivity] = {
//    ref match {
//      case "0000001611" =>
//        Gen.const(
//          InternalActivity(
//            Map(
//              ProducedOwnBrand -> (8592932967685L, 393857365208L),
//              CopackerAll      -> (6911728073310L, 9693701140898L),
//              Copackee         -> (7732342042367L, 1761987975555L)
//            ),
//            isLarge = false
//          )
//        )
//      case "0000000437" =>
//        Gen.const(
//          InternalActivity(
//            Map(
//              ProducedOwnBrand -> (2435810187383L, 4849497916977L),
//              Imported         -> (2145648045812L, 6153117498101L),
//              CopackerAll      -> (4066616847404L, 2728740244143L)
//            ),
//            isLarge = true
//          )
//        )
//      case "0000000069" =>
//        Gen.const(
//          InternalActivity(
//            Map(
//              Imported -> (6460796745282L, 6336758185953L),
//              Copackee -> (702818577689L, 5022677436353L)
//            ),
//            isLarge = false
//          )
//        )
//      case _ =>
//        for {
//          produced  <- activityGen(ProducedOwnBrand).sometimes
//          imported  <- activityGen(Imported).sometimes
//          copacking <- activityGen(CopackerAll).sometimes
//          copacked  <- activityGen(Copackee).sometimes
//          isLarge   <- Gen.boolean
//        } yield InternalActivity(
//          Seq(produced, imported, copacking, copacked).flatten.toMap,
//          isLarge
//        )
//    }
//  }

  val activity1 = InternalActivity(
    Map(
      ProducedOwnBrand -> (8592932967685L, 393857365208L),
      CopackerAll      -> (6911728073310L, 9693701140898L),
      Copackee         -> (7732342042367L, 1761987975555L)
    ),
    isLarge = false
  )
  println(s"Loaded activity for 0000001611: $activity1")

  val activity2 = InternalActivity(
    Map(
      ProducedOwnBrand -> (2435810187383L, 4849497916977L),
      Imported         -> (2145648045812L, 6153117498101L),
      CopackerAll      -> (4066616847404L, 2728740244143L)
    ),
    isLarge = true
  )
  println(s"Loaded activity for 0000000437: $activity2")

  val activity3 = InternalActivity(
    Map(
      Imported -> (6460796745282L, 6336758185953L),
      Copackee -> (702818577689L, 5022677436353L)
    ),
    isLarge = false
  )
  println(s"Loaded activity for 0000000069: $activity3")

  val testDataOverrides: Map[String, InternalActivity] = Map(
    "5168983848" -> activity1,
    "2701345062" -> activity2,
    "4035769414" -> activity3
  )

  def internalActivityGenForRef(ref: String): Gen[InternalActivity] =
    Gen
      .delay {
        println(s"[internalActivityGenForRef] Using test override for UTR: $ref")
        testDataOverrides.get(ref) match {
          case Some(activity) =>
            Gen.const(activity)
          case None =>
            println(s"[internalActivityGenForRef] No override found for UTR: $ref, generating random activity")
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
      }
      .flatMap(identity) // Flatten Gen[Gen[InternalActivity]] to Gen[InternalActivity]

  //  def internalActivityGenForRef(ref: String): Gen[InternalActivity] =
//    Gen.const(
//      InternalActivity(
//        Map(
//          ProducedOwnBrand -> (2435810187383L, 4849497916977L),
//          Imported         -> (2145648045812L, 6153117498101L),
//          CopackerAll      -> (4066616847404L, 2728740244143L)
//        ),
//        isLarge = true
//      )
//    )

//  private lazy val internalActivityGen: Gen[Activity] = for {
//    produced  <- activityGen(ProducedOwnBrand).sometimes
//    imported  <- activityGen(Imported).sometimes
//    copacking <- activityGen(CopackerAll).sometimes
//    copacked  <- activityGen(Copackee).sometimes
//    isLarge   <- Gen.boolean
//  } yield InternalActivity(
//    Seq(produced, imported, copacking, copacked).flatten.toMap,
//    isLarge
//  )

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
