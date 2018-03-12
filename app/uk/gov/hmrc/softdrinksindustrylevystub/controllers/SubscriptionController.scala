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
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class SubscriptionController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController
  with ExtraActions {

  def createSubscription(idType: String, idNumber: String): Action[JsValue] = AuthAndEnvAction(parse.json) {
    implicit request: Request[JsValue] =>
      (Try(request.body.validate[CreateSubscriptionRequest]), Validation.checkParams(idType, idNumber)) match {
        case (Success(JsSuccess(payload, _)), failures) if payload.isValid && failures.isEmpty =>
          Ok(Json.toJson(
            desSubmissionService.createSubscriptionResponse(idNumber, request.body.as[Subscription](CreateFormat.subscriptionReads))
          )).withHeaders(
            ("CorrelationId", genCorrelationIdHeader.seeded(idNumber).get)
          )
        case (Success(JsSuccess(payload, _)), failures) if !payload.isValid =>
          BadRequest(Json.toJson(FailureResponse(failures :+ Validation.payloadFailure)))
        case (Success(JsError(_)) | Failure(_), failures) =>
          BadRequest(Json.toJson(FailureResponse(failures :+ Validation.payloadFailure)))
        case (_, failures) =>
          BadRequest(Json.toJson(FailureResponse(failures)))
      }
  }

  def retrieveSubscriptionDetails(idType: String, idNumber: String) = AuthAndEnvAction {
    desSubmissionService.retrieveSubscriptionDetails(idType, idNumber) match {
      case Some(data) => Ok(Json.toJson(data)(GetFormat.subscriptionWrites))
      case _ => NotFound(Json.obj("reason" -> "unknown subscription"))
    }
  }

  def reset: Action[AnyContent] = Action {
    desSubmissionService.store.clear
    desSubmissionService.returnStore.clear
    Ok
  }

}
