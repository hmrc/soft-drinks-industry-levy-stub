/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.{JsSuccess, JsValue}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.softdrinksindustrylevystub.models.DmsHtmlSubmission

import scala.concurrent.ExecutionContext

@Singleton
class GFormController @Inject()(cc: ControllerComponents, extraActions: ExtraActions)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  //Stub Gform functionality for local testing due to configuration issues with pdf generator service
  def submitToDms(): Action[JsValue] = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validateOpt[DmsHtmlSubmission] match {
      case JsSuccess(Some(submission: DmsHtmlSubmission), _) => Ok("DMS Submission is valid")
      case _                                                 => BadRequest("DMS Submission Failed")
    }
  }
}
