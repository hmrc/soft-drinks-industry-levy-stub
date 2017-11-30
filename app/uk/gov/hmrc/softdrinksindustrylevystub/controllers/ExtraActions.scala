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

import play.api.http.HeaderNames
import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc.{ActionBuilder, ActionFilter, Request, Result}

import scala.concurrent.Future

trait ExtraActions {

  val AuthAndEnvAction: ActionBuilder[Request] = AuthorisedFilterAction andThen EnvironmentFilterAction

  object AuthorisedFilterAction extends ActionBuilder[Request] with ActionFilter[Request] {
    def filter[A](request: Request[A]): Future[Option[Result]] = {
      Future.successful(
        request.headers.get(HeaderNames.AUTHORIZATION).fold[Option[Result]](
          Some(Unauthorized(""))
        ) {
          _ => None
        }
      )
    }
  }

  object EnvironmentFilterAction extends ActionFilter[Request] {
    def filter[A](request: Request[A]): Future[Option[Result]] = {
      Future.successful(
        request.headers.get("Environment").fold[Option[Result]](
          Some(Forbidden(""))
        ) {
          a =>
            if (a.matches("^(ist0|clone|live)$")) {
              None
            }
            else {
              Some(Forbidden(""))
            }
        }
      )
    }
  }

}
