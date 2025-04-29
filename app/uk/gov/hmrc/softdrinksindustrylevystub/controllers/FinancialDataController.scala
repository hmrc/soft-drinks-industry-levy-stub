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

package uk.gov.hmrc.softdrinksindustrylevystub.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.*
import play.api.mvc.*
import play.api.Logger
import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.*
import sdil.models.des.FinancialTransaction.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton
class FinancialDataController @Inject() (cc: ControllerComponents) extends BackendController(cc) {

  val logger = Logger("FinancialDataController")
  val canned = CannedFinancialData.canned

  implicit val sdilEnum: Enumerable[String] = SdilNumberTransformer.sdilRefEnum

  def test(
    sdilRef: String,
    onlyOpenItems: Boolean,
    includeLocks: Boolean,
    calculateAccruedInterest: Boolean,
    customerPaymentInformation: Boolean
  ): Action[AnyContent] = Action {
    logger.info(
      s"Query parameters onlyOpenItems=$onlyOpenItems, includeLocks=$includeLocks, calculateAccruedInterest=$calculateAccruedInterest, customerPaymentInformation= $customerPaymentInformation"
    )

    val id = summon[Enumerable[String]].asLong(sdilRef) % canned.size

    canned(id.toInt) match {
      case (file, Left(e)) => throw new IllegalStateException(s"unable to parse $file", e)
      case (file, Right(json)) =>
        logger.info(s"Serving ${file.getName}")
        Ok(Json.toJson(json))
    }
  }
}
