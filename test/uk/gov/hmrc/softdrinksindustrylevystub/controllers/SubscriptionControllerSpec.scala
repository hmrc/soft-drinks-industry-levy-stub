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

import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

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
import uk.gov.hmrc.softdrinksindustrylevystub.models.{CreateSubscriptionRequest, CreateSubscriptionResponse}
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class SubscriptionControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService)
  implicit val hc: HeaderCarrier = new HeaderCarrier
  val utr = "1097172565"
  val idType = "utr"
  val now: OffsetDateTime = LocalDateTime.now.atOffset(ZoneOffset.UTC)

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "SubscriptionController" should {

    "return Status: 404 Body: reason: unknown subscription message for a unsuccessful retrieve request" in {
      when(mockDesSubmissionService
        .retrieveSubscriptionDetails(utr)).thenReturn(None)
      val response = mockSubscriptionController
        .retrieveSubscriptionDetails(idType, utr)(FakeRequest("GET", "/soft-drinks/subscription/"))

      status(response) mustBe NOT_FOUND
    }

    "return Status: OK Body: CreateSubscriptionRequest for a successful retrieve request" in {
      val r = Json.fromJson[CreateSubscriptionRequest](successfulRetrieveOutput)

      when(mockDesSubmissionService
        .retrieveSubscriptionDetails(utr)).thenReturn(Some(r.get))
      val response = mockSubscriptionController
        .retrieveSubscriptionDetails(idType, utr)(FakeRequest("GET", "/soft-drinks/subscription/"))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).retrieveSubscriptionDetails(any())
      contentAsJson(response).mustBe(successfulRetrieveOutput)
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest" in {
      when(mockDesSubmissionService
        .createSubscriptionResponse(any(),any())).thenReturn(CreateSubscriptionResponse(now, "bar"))
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(FakeRequest("POST", "/soft-drinks/subscription")
          .withBody(validCreateSubscriptionRequestInput))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(), any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response))
        .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest " +
      "without all optional data" in {
      when(mockDesSubmissionService
        .createSubscriptionResponse(any(),any())).thenReturn(CreateSubscriptionResponse(now, "bar"))
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(FakeRequest("POST", "/soft-drinks/subscription")
          .withBody(validCreateSubscriptionRequestInputWithoutOptionals))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(),any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response))
        .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
    }

    "return Status: 400 Body: nondescript error message for submission for invalid CreateSubscriptionRequest" in {
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(FakeRequest("POST", "/soft-drinks/subscription").withBody(invalidCreationInput))

      status(response) mustBe BAD_REQUEST
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(),any())
    }

  }
}
