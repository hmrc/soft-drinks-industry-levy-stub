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

import cats.implicits.*
import des.*
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.softdrinksindustrylevystub.Store
import uk.gov.hmrc.softdrinksindustrylevystub.models.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader

import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class HipSubscriptionController @Inject() (
  desSubmissionService: DesSubmissionService,
  cc: ControllerComponents,
  extraActions: ExtraActions
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private def correlationId(implicit request: Request[?]): String =
    request.headers
      .get("correlationid")
      .orElse(request.headers.get("CorrelationId"))
      .getOrElse(genCorrelationIdHeader.sample.get)

  private def tooManyRequestsResponse(cid: String): Result =
    TooManyRequests(hipError("429", "Too many requests"))
      .withHeaders("correlationid" -> cid)

  private def hipError(code: String, text: String): JsObject =
    Json.obj(
      "errors" -> Json.obj(
        "processingDate" -> OffsetDateTime.now(ZoneOffset.UTC).toString,
        "code"           -> code,
        "text"           -> text
      )
    )

  def createSubscription(idType: String, idNumber: String): Action[JsValue] =
    extraActions.hipAction.async(parse.json) { implicit request =>
      val cid = correlationId

      Future
        .successful {
          (Try(request.body.validate[CreateSubscriptionRequest]), Validation.checkParams(idType, idNumber)) match {
            case _ if idNumber == "0000010901" =>
              tooManyRequestsResponse(cid)

            case (Success(JsSuccess(payload, _)), failures) if payload.isValid && failures.isEmpty =>
              val response =
                desSubmissionService.createSubscriptionResponse(
                  idNumber,
                  request.body.as[Subscription](using CreateFormat.subscriptionReads)
                )

              Created(Json.obj("success" -> Json.toJson(response)))
                .withHeaders("correlationid" -> cid)

            case _ =>
              UnprocessableEntity(hipError("003", "Request could not be processed"))
                .withHeaders("correlationid" -> cid)
          }
        }
        .desify(idNumber)
    }

  def retrieveSubscriptionDetails(idType: String, idNumber: String): Action[AnyContent] =
    extraActions.hipAction.async { implicit request =>
      val cid = correlationId

      val subscription: Option[Subscription] = {
        idType match {
          case "sdil" => idNumber.some
          case "utr"  => Store.utrToSdil(idNumber).lastOption
          case _      => None
        }
      } flatMap Store.fromSdilRef

      Future
        .successful {
          subscription match {
            case _ if idNumber == "0000010901" =>
              tooManyRequestsResponse(cid)

            case Some(data) =>
              Ok(Json.obj("success" -> Json.toJson(data)(using GetFormat.subscriptionWrites)))
                .withHeaders("correlationid" -> cid)

            case None =>
              UnprocessableEntity(hipError("002", "ID not Found"))
                .withHeaders("correlationid" -> cid)
          }
        }
        .desify(idNumber)
    }
}
