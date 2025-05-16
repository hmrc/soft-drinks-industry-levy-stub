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

package uk.gov.hmrc.softdrinksindustrylevystub.models.internal

import play.api.libs.json.{JsBoolean, JsObject, JsString}

import java.time.LocalDate

case class Subscription(
  utr: String,
  orgName: String,
  orgType: Option[String],
  address: Address,
  activity: Activity,
  liabilityDate: LocalDate,
  productionSites: Seq[Site],
  warehouseSites: Seq[Site],
  contact: Contact,
  sdilRef: String = "",
  deregDate: Option[LocalDate] = None
)

trait Address {
  def lines: Seq[String]
  def country: String

  def jsLines = lines.zipWithIndex.map { case (v, i) =>
    s"line${i + 1}" -> JsString(v)
  }

  def asJson: JsObject
}

case class UkAddress(lines: Seq[String], postCode: String) extends Address {
  val country = "GB"

  override def asJson: JsObject = JsObject(
    List(
      "notUKAddress" -> JsBoolean(false),
      "postCode"     -> JsString(postCode)
    ) ++ jsLines
  )
}

case class ForeignAddress(lines: Seq[String], country: String) extends Address {
  override def asJson: JsObject = JsObject(
    List(
      "notUKAddress" -> JsBoolean(true),
      "country"      -> JsString(country)
    ) ++ jsLines
  )
}

case class Site(address: Address, ref: Option[String], tradingName: Option[String], closureDate: Option[LocalDate])

case class Contact(name: Option[String], positionInCompany: Option[String], phoneNumber: String, email: String)
