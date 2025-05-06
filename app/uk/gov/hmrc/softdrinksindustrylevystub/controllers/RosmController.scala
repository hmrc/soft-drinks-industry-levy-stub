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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.services.RosmService

import scala.concurrent.Future

@Singleton
class RosmController @Inject() (rosmService: RosmService, cc: ControllerComponents, extraActions: ExtraActions)
    extends BackendController(cc) {

  def register(utr: String): Action[JsValue] = extraActions.AuthAndEnvAction.async(parse.json) { implicit request =>
    withJsonBody[RosmRegisterRequest](rosmRequest =>
      if (rosmRequest.regime.matches("ZSDL"))
        rosmService.handleRegisterRequest(rosmRequest, utr) match {
          case Some(data) => Future successful Ok(Json.toJson(data))
          case _ =>
            Future successful NotFound(
              Json.toJson(FailureMessage("NOT_FOUND", "The remote endpoint has indicated that no data can be found"))
            )
        }
      else
        Future successful BadRequest(
          Json.toJson(FailureMessage("INVALID_PAYLOAD", "Submission has not passed validation. Invalid Payload."))
        )
    )
  }

}
