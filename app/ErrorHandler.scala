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

import javax.inject.{Inject, Singleton}

import play.api.http.HttpErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.softdrinksindustrylevystub.models.{FailureMessage, FailureResponse}

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(implicit val config: Configuration, val env: Environment, val messagesApi: MessagesApi)
    extends HttpErrorHandler with I18nSupport {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    Future.successful(
      Status(statusCode)(
        Json.toJson(
          FailureResponse(
            List(FailureMessage(
              statusCode.toString,
              message
            ))))))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case _ =>
        Logger.error("Server error", exception)
        Future.successful(
          InternalServerError(
            Json.toJson(
              FailureResponse(
                List(FailureMessage(
                  "500",
                  "DES is currently experiencing problems that require live service intervention."
                ))))))
    }
}
