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

package uk.gov.hmrc.softdrinksindustrylevystub.models.internal

import java.time.LocalDate

import play.api.libs.json._

object CreateFormat {

  implicit val businessContactReads: Reads[Contact] = new Reads[Contact] {
    override def reads(json: JsValue): JsResult[Contact] =
      JsSuccess(
        Contact(
          (json \ "name").asOpt[String],
          (json \ "positionInCompany").asOpt[String],
          (json \ "telephone").as[String],
          (json \ "email").as[String]
        ))
  }

  // SDIL create and retrieve subscription formatters
  implicit val addressReads: Reads[Address] = new Reads[Address] {
    def reads(json: JsValue): JsResult[Address] = {

      val lines = List(
        Some((json \ "line1").as[String]),
        Some((json \ "line2").as[String]),
        (json \ "line3").asOpt[String],
        (json \ "line4").asOpt[String]
      ).flatten

      val country = (json \ "country").asOpt[String].map(_.toUpperCase)
      val nonUk = (json \ "notUKAddress").as[Boolean]
      (country, nonUk) match {
        case (Some("GB"), true)         => JsError("Country code is GB, but notUKAddress is true")
        case (Some("GB") | None, false) => JsSuccess(UkAddress(lines, (json \ "postCode").as[String]))
        case (Some(_), false)           => JsError("Country code is not GB, but notUKAddress is false")
        case (None, true)               => JsError("notUKAddress is true, but no country is supplied")
        case (Some(c), true)            => JsSuccess(ForeignAddress(lines, c))
      }
    }
  }

  implicit val subscriptionReads: Reads[Subscription] = new Reads[Subscription] {
    def reads(json: JsValue): JsResult[Subscription] = {

      val (warehouses, production) = json \ "sites" match {
        case JsDefined(JsArray(sites)) => {
          sites.partition(site => (site \ "siteType").as[String] == "1")
        }
        case _ => (Nil, Nil)
      }

      def sites(siteJson: Seq[JsValue]) = {
        siteJson map { site =>
          Site(
            address = (site \ "siteAddress" \ "addressDetails").as[Address],
            ref = (site \ "newSiteRef").asOpt[String],
            tradingName = (site \ "tradingName").asOpt[String],
            closureDate = None
          )
        }
      }.toList

      val regJson = json \ "registration"

      def litreReads(activityField: String) = (
        (regJson \ "activityQuestions" \ s"litres${activityField}UKLower").asOpt[Litres].getOrElse(0L),
        (regJson \ "activityQuestions" \ s"litres${activityField}UKHigher").asOpt[Litres].getOrElse(0L)
      )

      def activity = {
        val produced = ActivityType.ProducedOwnBrand -> litreReads("Produced")
        val imported = ActivityType.Imported         -> litreReads("Imported")
        val packaged = ActivityType.CopackerAll      -> litreReads("Packaged")

        val isSmall = (regJson \ "details" \ "producerDetails" \ ("produce" /*LessThan*/ + "MillionLitres"))
          .asOpt[Boolean]
          .getOrElse(true)

        InternalActivity(Map(produced, imported, packaged), isLarge = !isSmall)
      }

      JsSuccess(
        Subscription(
          utr = (regJson \ "cin").as[String],
          orgName = (regJson \ "tradingName").as[String],
          orgType = (regJson \ "organisationType").asOpt[String],
          address = (regJson \ "businessContact" \ "addressDetails").as[Address],
          activity = activity,
          liabilityDate = (regJson \ "taxStartDate").as[LocalDate],
          productionSites = sites(production),
          warehouseSites = sites(warehouses),
          contact = (regJson \ "primaryPersonContact").as[Contact]
        ))

    }
  }

}
