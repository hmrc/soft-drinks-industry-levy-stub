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
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader
import uk.gov.hmrc.softdrinksindustrylevystub.services.{DesSubmissionService, SdilNumberTransformer}

class ReturnController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController
  with ExtraActions {

  def createReturn(sdilRef: String): Action[JsValue] = AuthAndEnvAction(parse.json) {
    implicit request: Request[JsValue] =>
      request.body.validate[Return] match {
        case JsSuccess(_,_) if !sdilRef.matches(ReturnValidation.sdilRefPattern) =>
          BadRequest(Json.toJson(ReturnFailureResponse.invalidSdilRef))
        case JsSuccess(a,_) if desSubmissionService.retrieveSubscriptionDetails("sdil",sdilRef).isEmpty =>
          Forbidden(Json.toJson(ReturnFailureResponse.noBpKey))
        case JsSuccess(a,_) if !a.periodKey.matches(ReturnValidation.periodKeyPattern) =>
          BadRequest(Json.toJson(ReturnFailureResponse.invalidPeriodKey))
        case JsSuccess(a,_) if desSubmissionService.checkForExistingReturn(sdilRef + a.periodKey) =>
          Conflict(Json.toJson(ReturnFailureResponse.obligationFilled))
        case JsSuccess(a,_) if a.isValid =>
          Ok(Json.toJson(
            desSubmissionService.createReturnResponse(a, sdilRef)
          )).withHeaders(
            ("CorrelationId", genCorrelationIdHeader.seeded(sdilRef)(SdilNumberTransformer.sdilRefEnum).get)
          )
        case _ =>
          BadRequest(Json.toJson(ReturnFailureResponse.invalidPayload))
      }
  }

  implicit val sdilToLong: Enumerable[String] = pattern"ZZ9999999994".imap{
    i => i.take(2) ++ "SDIL" ++ i.substring(2,7) ++ "C" ++ i.takeRight(1)
  }{ b => b.take(2) ++ b.takeRight(9) }

  def resetReturns: Action[AnyContent] = Action {
    desSubmissionService.returnStore.clear
    Ok
  }

}


