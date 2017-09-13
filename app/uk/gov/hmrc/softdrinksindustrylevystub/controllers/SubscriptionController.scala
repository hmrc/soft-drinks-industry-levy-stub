/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.softdrinksindustrylevystub.models.DesSubmissionRequest
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

import scala.concurrent.Future

@Singleton
class SubscriptionController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController {

  def submitHello(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[DesSubmissionRequest](_ => Future.successful(Ok(Json.toJson(desSubmissionService.buildResponse()))))
  }

  def createSubscription = ???

  def retrieveSubscriptionDetails(nino: String, utr: String): Action[JsValue] = Action.async { implicit request =>

    ???
  }
}
