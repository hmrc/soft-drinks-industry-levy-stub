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
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub.{CreateSubscriptionRequest, CreateSubscriptionResponse}
import uk.gov.hmrc.softdrinksindustrylevystub.services.{DesSubmissionService, SubscriptionGenerator}

class SubscriptionControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService)
  implicit val hc: HeaderCarrier = new HeaderCarrier

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "SubscriptionController" should {

    "return Status: 404 Body: reason: unknown subscription message for a unsuccessful retrieve request" in {

      val utr = "1097172565"

      when(mockDesSubmissionService.retrieveSubscriptionDetails(utr)).thenReturn(None)
      val response = mockSubscriptionController.retrieveSubscriptionDetails(utr)(FakeRequest("GET", "/retrieve-subscription-details/"))

      status(response) mustBe NOT_FOUND
    }

    "return Status: OK Body: CreateSubscriptionRequest for a successful retrieve request" in {
      val utr = "1097172564"
      val r = Json.fromJson[CreateSubscriptionRequest](successfulRetrieveOutput)
      when(mockDesSubmissionService.retrieveSubscriptionDetails(utr)).thenReturn(Some(r.get))
      val response = mockSubscriptionController.retrieveSubscriptionDetails(utr)(FakeRequest("GET", "/retrieve-subscription-details/"))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).retrieveSubscriptionDetails(any())
      contentAsJson(response).mustBe(successfulRetrieveOutput)
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest with all optional data" in {

      when(mockDesSubmissionService.createSubscriptionResponse(any())).thenReturn(CreateSubscriptionResponse("foo", "bar"))
      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(validCreateSubscriptionRequestInput))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response)).getOrElse(CreateSubscriptionResponse("bar", "foo")) mustBe CreateSubscriptionResponse("foo", "bar")
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest without all optional data" in {

      when(mockDesSubmissionService.createSubscriptionResponse(any())).thenReturn(CreateSubscriptionResponse("foo", "bar"))
      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(validCreateSubscriptionRequestInputWithoutOptionals))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response)).getOrElse(CreateSubscriptionResponse("bar", "foo")) mustBe CreateSubscriptionResponse("foo", "bar")
    }

    "return Status: 400 Body: nondescript error message for submission for invalid CreateSubscriptionRequest" in {

      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(invalidCreationInput))

      status(response) mustBe BAD_REQUEST
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any())
    }

  }
}
