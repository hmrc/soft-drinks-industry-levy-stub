/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services._
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader
import uk.gov.hmrc.softdrinksindustrylevystub.Store
import scala.util.{Failure, Success, Try}
import des._
import cats.implicits._
import sdil.models.des.FinancialTransaction._

@Singleton
class FinancialDataController @Inject()()(implicit ec: ExecutionContext) extends BaseController
    with ExtraActions {

  def test(
    sdilRef: String,
    onlyOpenItems: Boolean,
    includeLocks: Boolean,
    calculateAccruedInterest: Boolean,
    customerPaymentInformation: Boolean
  ): Action[AnyContent] = Action {
    val data = Store.financialHistory(sdilRef)
    Ok(Json.toJson(data))
  }

}
