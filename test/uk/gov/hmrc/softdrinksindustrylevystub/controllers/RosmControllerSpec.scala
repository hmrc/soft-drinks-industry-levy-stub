/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.softdrinksindustrylevystub.services.RosmService
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

class RosmControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockRosmService: RosmService = mock[RosmService]
  val cc = stubControllerComponents()
  val authorisedFilterAction = new AuthorisedFilterAction(cc)
  val environmentAction = new EnvironmentFilterAction()
  val extraActions = new ExtraActions(authorisedFilterAction, environmentAction)
  val mockRosmController = new RosmController(mockRosmService, cc, extraActions)
  val authHeader: (String, String) = "Authorization" -> "auth"
  val envHeader: (String, String) = "Environment"    -> "clone"
  val badEnvHeader: (String, String) = "Environment" -> "test"

  implicit val hc = new HeaderCarrier

  "RosmController" should {
    "return Status: OK Body: RosmRegisterResponse with Individual for successful valid registration request" in {
      val utr = "1234123400"

      when(mockRosmService.handleRegisterRequest(any(), any())).thenReturn(Some(rosmRegisterIndividualResponse))

      val response = mockRosmController.register(utr)(
        FakeRequest("POST", "/register/organisation/:utr")
          .withBody(validRosmRegisterIndividualInput)
          .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(rosmRegisterIndividualResponse)
    }

    "return Status: OK Body: RosmRegisterResponse with Organisation for successful valid registration request" in {
      val utr = "1234123400"

      when(mockRosmService.handleRegisterRequest(any(), any())).thenReturn(Some(rosmRegisterOrganisationResponse))

      val response = mockRosmController.register(utr)(
        FakeRequest("POST", "/register/organisation/:utr")
          .withBody(validRosmRegisterOrganisationnput)
          .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(rosmRegisterOrganisationResponse)
    }

    "return Status: NOT_FOUND Body: Error response" in {
      val utr = "9999999999"

      when(mockRosmService.handleRegisterRequest(any(), any())).thenReturn(None)

      val response = mockRosmController.register(utr)(
        FakeRequest("POST", "/register/organisation/:utr")
          .withBody(validRosmRegisterOrganisationnput)
          .withHeaders(envHeader, authHeader))

      val errorResponse = Json.parse("""{
        "code": "NOT_FOUND",
        "reason": "The remote endpoint has indicated that no data can be found"
      }""".stripMargin)

      status(response) mustBe NOT_FOUND
      contentAsJson(response) mustBe errorResponse
    }

    "return Status: Bad Request for invalid json request" in {
      val utr = "123"

      val response = mockRosmController.register(utr)(
        FakeRequest("POST", "/register/organisation/:utr")
          .withBody(Json.parse(invalidRosmRegisterInput))
          .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
    }

    "return Status: Bad Request for invalid regime" in {
      val utr = "123"

      val response = mockRosmController.register(utr)(
        FakeRequest("POST", "/register/organisation/:utr")
          .withBody(invalidRosmRegime)
          .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
      contentAsJson(response) mustBe Json.obj(
        "code"   -> "INVALID_PAYLOAD",
        "reason" -> "Submission has not passed validation. Invalid Payload."
      )
    }
  }
}
