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
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader
import uk.gov.hmrc.softdrinksindustrylevystub.services.{DesSubmissionService, SdilNumberTransformer}

class ReturnController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController
  with ExtraActions {

  def createReturn: Action[JsValue] = AuthAndEnvAction(parse.json) {
    implicit request: Request[JsValue] =>
      request.body.validate[Return] match {
        case JsSuccess(a,_) if desSubmissionService.retrieveSubscriptionDetails("sdil",a.sdilRef).isEmpty =>
          Forbidden(Json.toJson(noBpKey))
        case JsSuccess(a,_) if !a.periodKey.matches(ReturnValidation.periodKeyPattern) =>
          BadRequest(Json.toJson(invalidPeriodKey))
        case JsSuccess(a,_) if desSubmissionService.checkForExistingReturn(a.sdilRef + a.periodKey) =>
          Forbidden(Json.toJson(obligationFilled))
        case JsSuccess(a,_) if a.isValid =>
          Ok(Json.toJson(
            desSubmissionService.createReturnResponse(a)
          )).withHeaders(
            ("CorrelationId", genCorrelationIdHeader.seeded(a.sdilRef)(SdilNumberTransformer.sdilRefEnum).get)
          )
        case _ =>
          BadRequest(Json.toJson(invalidPayload))
      }
  }

  val noBpKey = ReturnFailureResponse(
    "NOT_FOUND_BPKEY",
    "The remote endpoint has indicated that business partner key information cannot be found for the idNumber."
  )

  val invalidPeriodKey = ReturnFailureResponse(
    "INVALID_PERIOD_KEY",
    "The remote endpoint has indicated that the period key in the request is invalid."
  )

  val obligationFilled = ReturnFailureResponse(
    "OBLIGATION_FULFILLED",
    "The remote endpoint has indicated that the obligation for the period is already fulfilled."
  )

  val invalidPayload = ReturnFailureResponse(
    "INVALID_PAYLOAD",
    "Submission has not passed validation. Invalid Payload."
  )

  implicit val sdilToLong: Enumerable[String] = pattern"ZZ9999999994".imap{
    i => i.take(2) ++ "SDIL" ++ i.substring(2,7) ++ "C" ++ i.takeRight(1)
  }{ b => b.take(2) ++ b.takeRight(9) }


}
