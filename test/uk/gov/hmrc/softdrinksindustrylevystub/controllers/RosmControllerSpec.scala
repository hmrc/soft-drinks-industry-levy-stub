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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.softdrinksindustrylevystub.services.RosmService
import uk.gov.hmrc.http.HeaderCarrier

class RosmControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockRosmService: RosmService = mock[RosmService]
  val mockRosmController = new RosmController(mockRosmService)

  implicit val hc = new HeaderCarrier

  "RosmController" should {
    "return Status: OK Body: RosmRegisterResponse with Individual for successful valid registration request" in {
      val utr = "1234123400"

      when(mockRosmService.handleRegisterRequest(any(), any())).thenReturn(rosmRegisterIndividualResponse)

      val response = mockRosmController.register(utr)(FakeRequest("POST", "/register/:utr").withBody(validRosmRegisterIndividualInput))

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(rosmRegisterIndividualResponse)
    }

    "return Status: OK Body: RosmRegisterResponse with Organisation for successful valid registration request" in {
      val utr = "1234123400"

      when(mockRosmService.handleRegisterRequest(any(), any())).thenReturn(rosmRegisterOrganisationResponse)

      val response = mockRosmController.register(utr)(FakeRequest("POST", "/register/:utr").withBody(validRosmRegisterOrganisationnput))

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(rosmRegisterOrganisationResponse)
    }

    "return Status: Bad Request Body: ? for invalid json request" in {
      val utr = "123"

      val response = mockRosmController.register(utr)(FakeRequest("POST", "/register/:utr").withBody(Json.parse(invalidRosmRegisterInput)))

      status(response) mustBe BAD_REQUEST
    }
  }
}
