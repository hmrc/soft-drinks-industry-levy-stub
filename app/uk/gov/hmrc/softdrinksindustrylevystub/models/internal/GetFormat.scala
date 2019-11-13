/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._

object GetFormat {
  implicit val contactWrites: Writes[Contact] = new Writes[Contact] {
    override def writes(o: Contact): JsObject =
      Json.obj(
        "name"              -> o.name,
        "positionInCompany" -> o.positionInCompany,
        "telephone"         -> o.phoneNumber,
        "email"             -> o.email
      )
  }

  implicit val addressWrites: Writes[Address] = new Writes[Address] {

    def writes(address: Address): JsValue = {

      val jsLines = address.lines.zipWithIndex.map {
        case (v, i) =>
          s"line${i + 1}" -> JsString(v)
      }

      JsObject(
        {
          address match {
            case UkAddress(_, postCode) =>
              List(
                "notUKAddress" -> JsBoolean(false),
                "postCode"     -> JsString(postCode)
              )
            case ForeignAddress(_, country) =>
              List(
                "notUKAddress" -> JsBoolean(true),
                "country"      -> JsString(country)
              )
          }
        } ++ jsLines
      )
    }
  }

  val subscriptionWrites: Writes[Subscription] = new Writes[Subscription] {

    override def writes(o: Subscription): JsValue = {

      def siteList(sites: Seq[Site], isWarehouse: Boolean): Seq[JsObject] =
        sites map { site =>
          Json.obj(
            "tradingName"   -> site.tradingName,
            "siteReference" -> site.ref,
            "siteAddress"   -> site.address,
            "siteContact" -> Json.obj(
              "telephone" -> o.contact.phoneNumber,
              "email"     -> o.contact.email
            ),
            "closureDate" -> site.closureDate,
            "siteType"    -> (if (isWarehouse) "1" else "2")
          )

        }

      Json.obj(
        "utr" -> o.utr,
        "subscriptionDetails" -> Json.obj(
          "sdilRegistrationNumber"   -> o.sdilRef,
          "taxObligationStartDate"   -> o.liabilityDate.toString,
          "taxObligationEndDate"     -> o.liabilityDate.plusYears(1).toString,
          "deregistrationDate"       -> o.deregDate.getOrElse("").toString,
          "tradingName"              -> o.orgName,
          "voluntaryRegistration"    -> o.activity.isVoluntaryRegistration,
          "smallProducer"            -> o.activity.isSmallProducer,
          "largeProducer"            -> o.activity.isLarge,
          "contractPacker"           -> o.activity.isContractPacker,
          "importer"                 -> o.activity.isImporter,
          "primaryContactName"       -> o.contact.name,
          "primaryPositionInCompany" -> o.contact.positionInCompany,
          "primaryTelephone"         -> o.contact.phoneNumber,
          "primaryEmail"             -> o.contact.email
        ),
        "businessAddress" -> o.address,
        "businessContact" -> Json.obj(
          "telephone" -> o.contact.phoneNumber,
          "email"     -> o.contact.email
        ),
        "sites" -> (siteList(o.warehouseSites, true) ++ siteList(o.productionSites, false))
      )
    }

  }
}
