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
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub.CreateSubscriptionRequest
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

import scala.concurrent.Future

@Singleton
class SubscriptionController @Inject()(desSubmissionService: DesSubmissionService) extends BaseController {

  def createSubscription(idType: String, idNumber: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CreateSubscriptionRequest](data =>
      // TODO
      // validate idType = "utr"
      // validate id is a valid UTR
      // validate id matches the data.registration.cin
      // check the data is json
      Future.successful(Ok(Json.toJson(desSubmissionService.createSubscriptionResponse(data)))))
  }

  def retrieveSubscriptionDetails(idType: String, idNumber: String) = Action {
    desSubmissionService.retrieveSubscriptionDetails(idNumber) match {
      case Some(data) => Ok(Json.toJson(Some(data)))
      case _ => NotFound(Json.parse("""{"reason" : "unknown subscription"}"""))
    }
  }
}
