/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.softdrinksindustrylevystub.models.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader

import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}

@Singleton
class HipReturnController @Inject() (
  desSubmissionService: DesSubmissionService,
  cc: ControllerComponents,
  extraActions: ExtraActions
) extends BackendController(cc) {

  private def correlationId(implicit request: Request[?]): String =
    request.headers
      .get("correlationid")
      .orElse(request.headers.get("CorrelationId"))
      .getOrElse(genCorrelationIdHeader.sample.get)

  private def hipError(code: String, text: String): JsObject =
    Json.obj(
      "errors" -> Json.obj(
        "processingDate" -> OffsetDateTime.now(ZoneOffset.UTC).toString,
        "code"           -> code,
        "text"           -> text
      )
    )

  def createReturn(sdilReference: String): Action[JsValue] =
    extraActions.hipAction(parse.json) { implicit request =>
      val cid = correlationId

      request.body.validate[Return] match {
        case JsSuccess(_, _) if !sdilReference.matches(ReturnValidation.sdilRefPattern) =>
          UnprocessableEntity(hipError("039", "Invalid Input Data"))
            .withHeaders("correlationid" -> cid)

        case JsSuccess(_, _) if desSubmissionService.retrieveSubscriptionDetails("sdil", sdilReference).isEmpty =>
          UnprocessableEntity(hipError("002", "ID not Found"))
            .withHeaders("correlationid" -> cid)

        case JsSuccess(a, _) if !a.periodKey.matches(ReturnValidation.periodKeyPattern) =>
          UnprocessableEntity(hipError("040", "Invalid Period Key"))
            .withHeaders("correlationid" -> cid)

        case JsSuccess(a, _) if desSubmissionService.checkForExistingReturn(sdilReference + a.periodKey) =>
          UnprocessableEntity(hipError("044", "Obligation Already Fulfilled"))
            .withHeaders("correlationid" -> cid)

        case JsSuccess(a, _) if a.isValid =>
          Created(
            Json.obj(
              "success" -> Json.toJson(
                desSubmissionService.createReturnResponse(a, sdilReference)
              )
            )
          ).withHeaders("correlationid" -> cid)

        case _ =>
          UnprocessableEntity(hipError("039", "Invalid Input Data"))
            .withHeaders("correlationid" -> cid)
      }
    }
}
