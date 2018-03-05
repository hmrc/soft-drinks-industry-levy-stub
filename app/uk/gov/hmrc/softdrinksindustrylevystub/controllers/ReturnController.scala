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

import javax.inject.Inject

import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models.{Return, _}
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader

import scala.util.{Success, Try}

class ReturnController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController
  with ExtraActions {

  def createReturn: Action[JsValue] = AuthAndEnvAction(parse.json) {
    implicit request: Request[JsValue] =>
      Try(request.body.validate[Return]) match {
        case Success(JsSuccess(payload, _)) if payload.isValid =>
          Ok(Json.toJson(
            desSubmissionService.createReturnResponse(payload)
          )).withHeaders(
            ("CorrelationId", genCorrelationIdHeader.seeded(payload.sdilRef).get)
          )
          // TODO some other cases
      }
  }
}
