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

import cats.implicits._
import cats.instances.option._
import cats.syntax.flatMap._
import cats.syntax.option._
import des._
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.Store
import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils.idEnum
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services.HeadersGenerator.genCorrelationIdHeader
import uk.gov.hmrc.softdrinksindustrylevystub.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class SubscriptionController @Inject() (
  desSubmissionService: DesSubmissionService,
  cc: ControllerComponents,
  extraActions: ExtraActions
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def createSubscription(idType: String, idNumber: String): Action[JsValue] =
    extraActions.AuthAndEnvAction(parse.json) { implicit request: Request[JsValue] =>
      (Try(request.body.validate[CreateSubscriptionRequest]), Validation.checkParams(idType, idNumber)) match {
        case (Success(JsSuccess(payload, _)), failures) if payload.isValid && failures.isEmpty =>
          Ok(
            Json.toJson(
              desSubmissionService
                .createSubscriptionResponse(idNumber, request.body.as[Subscription](CreateFormat.subscriptionReads))
            )
          ).withHeaders(
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

  def retrieveSubscriptionDetails(idType: String, idNumber: String): Action[AnyContent] =
    extraActions.AuthAndEnvAction.async {

//      val subscription: Option[Subscription] = {
//        idType match {
//          case "sdil" => idNumber.some
//          case "utr"  => Store.utrToSdil(idNumber).lastOption
//          case weird  => throw new IllegalArgumentException(s"Weird id type: $weird")
//        }
//      } >>= Store.fromSdilRef

      val subscription: Option[Subscription] = {
        val sdilRefOpt = idType match {
          case "sdil" => Some(idNumber)
          case "utr"  => Store.utrToSdil(idNumber).lastOption
          case weird  => throw new IllegalArgumentException(s"Weird id type: $weird")
        }

        println(s"idType: $idType, idNumber: $idNumber")
        println(s"Subscription ref: $sdilRefOpt")

        sdilRefOpt.flatMap(Store.fromSdilRef)

      }

      println(s"Final subscription: $subscription")

      Future
        .successful(
          subscription match {
            case Some(_) if idNumber == "0000010901" => TooManyRequests(Json.obj("reason" -> "too many requests"))
            case Some(data)                          => Ok(Json.toJson(data)(GetFormat.subscriptionWrites))
            case _                                   => NotFound(Json.obj("reason" -> "unknown subscription"))
          }
        )
        .desify(idNumber)
    }

  def reset: Action[AnyContent] = Action {
    desSubmissionService.resetSubscriptions()
    Store.clear()
    Ok
  }

}
