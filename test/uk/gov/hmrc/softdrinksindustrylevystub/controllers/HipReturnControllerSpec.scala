/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.softdrinksindustrylevystub.Store
import uk.gov.hmrc.softdrinksindustrylevystub.models._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

import java.time.LocalDate

class HipReturnControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val cc: ControllerComponents = Helpers.stubControllerComponents()

  private val desSubmissionService = new DesSubmissionService

  private val extraActions = new ExtraActions(
    new AuthorisedFilterAction(cc),
    new EnvironmentFilterAction()(using cc.executionContext),
    new HipFilterAction(cc)
  )

  private val controller = new HipReturnController(
    desSubmissionService,
    cc,
    extraActions
  )

  override def beforeEach(): Unit = {
    Store.clear()
    desSubmissionService.resetReturns()
    super.beforeEach()
  }

  private val hipHeaders: Seq[(String, String)] =
    Seq(
      "correlationid"         -> "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "X-Originating-System"  -> "SDIL",
      "X-Receipt-Date"        -> "2026-03-10T12:34:46Z",
      "X-Transmitting-System" -> "HIP"
    )

  private val sdilRef = "XASDIL000000431"

  private def seedSubscription(): Unit =
    Store.add(
      Subscription(
        utr = "1234567890",
        orgName = "ACME Trading Ltd",
        orgType = Some("1"),
        address = UkAddress(Seq("Line 1", "Line 2"), "AB1 2AA"),
        activity = InternalActivity(Map.empty, isLarge = true),
        liabilityDate = LocalDate.of(2026, 3, 10),
        productionSites = Nil,
        warehouseSites = Nil,
        contact = Contact(
          name = Some("Mr Fred Bloggs"),
          positionInCompany = Some("Director"),
          phoneNumber = "01234567890",
          email = "fred@example.com"
        ),
        sdilRef = sdilRef
      )
    )

  private val validReturnPayload: JsObject =
    Json.obj(
      "periodKey"       -> "26C1",
      "formBundleType"  -> "ZSD1",
      "netLevyDueTotal" -> 100.00,
      "packaging" -> Json.obj(
        "volumeSmall" -> Json.arr(
          Json.obj(
            "producerRef" -> sdilRef,
            "lowVolume"   -> "10",
            "highVolume"  -> "20"
          )
        ),
        "volumeLarge" -> Json.obj(
          "lowVolume"  -> "30",
          "highVolume" -> "40"
        ),
        "monetaryValues" -> Json.obj(
          "lowVolume"    -> 10.00,
          "highVolume"   -> 20.00,
          "levySubtotal" -> 30.00
        )
      ),
      "importing" -> Json.obj(
        "volumeSmall" -> Json.obj(
          "lowVolume"  -> "5",
          "highVolume" -> "6"
        ),
        "volumeLarge" -> Json.obj(
          "lowVolume"  -> "7",
          "highVolume" -> "8"
        ),
        "monetaryValues" -> Json.obj(
          "lowVolume"    -> 5.00,
          "highVolume"   -> 6.00,
          "levySubtotal" -> 11.00
        )
      ),
      "exporting" -> Json.obj(
        "volumes" -> Json.obj(
          "lowVolume"  -> "1",
          "highVolume" -> "2"
        ),
        "monetaryValues" -> Json.obj(
          "lowVolume"    -> 1.00,
          "highVolume"   -> 2.00,
          "levySubtotal" -> 3.00
        )
      ),
      "wastage" -> Json.obj(
        "volumes" -> Json.obj(
          "lowVolume"  -> "3",
          "highVolume" -> "4"
        ),
        "monetaryValues" -> Json.obj(
          "lowVolume"    -> 3.00,
          "highVolume"   -> 4.00,
          "levySubtotal" -> 7.00
        )
      )
    )

  "createReturn" should {

    "return 201 with success wrapper for a valid HIP return request" in {
      seedSubscription()

      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validReturnPayload)

      val result = controller.createReturn(sdilRef)(request)

      status(result) shouldBe CREATED
      header("correlationid", result) shouldBe Some("a1b2c3d4-e5f6-7890-abcd-ef1234567890")

      val json = contentAsJson(result)
      (json \ "success" \ "formBundleNumber").asOpt[String] shouldBe defined
    }

    "return 400 when required HIP headers are missing" in {
      seedSubscription()

      val request =
        FakeRequest()
          .withBody(validReturnPayload)

      val result = controller.createReturn(sdilRef)(request)

      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when the sdil reference is invalid" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validReturnPayload)

      val result = controller.createReturn("invalid-sdil-ref")(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "039"
      (json \ "errors" \ "text").as[String] shouldBe "Invalid Input Data"
    }

    "return 422 when no subscription exists for the sdil reference" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validReturnPayload)

      val result = controller.createReturn("XASDIL000000430")(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "002"
      (json \ "errors" \ "text").as[String] shouldBe "ID not Found"
    }

    "return 422 when the period key is invalid" in {
      seedSubscription()

      val invalidPayload =
        validReturnPayload ++ Json.obj("periodKey" -> "2026C1")

      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(invalidPayload)

      val result = controller.createReturn(sdilRef)(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "040"
      (json \ "errors" \ "text").as[String] shouldBe "Invalid Period Key"
    }

    "return 422 when the obligation has already been fulfilled" in {
      seedSubscription()

      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validReturnPayload)

      val firstResult = controller.createReturn(sdilRef)(request)
      status(firstResult) shouldBe CREATED

      val secondResult = controller.createReturn(sdilRef)(request)

      status(secondResult) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(secondResult)
      (json \ "errors" \ "code").as[String] shouldBe "044"
      (json \ "errors" \ "text").as[String] shouldBe "Obligation Already Fulfilled"
    }

    "return 422 when the payload is invalid" in {
      seedSubscription()

      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(Json.obj("invalid" -> "payload"))

      val result = controller.createReturn(sdilRef)(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "039"
      (json \ "errors" \ "text").as[String] shouldBe "Invalid Input Data"
    }

    "accept legacy values field for exporting and wastage if the custom ExpoWasted formatter is in place" in {
      seedSubscription()

      val legacyPayload =
        validReturnPayload ++ Json.obj(
          "periodKey" -> "26C2",
          "exporting" -> Json.obj(
            "values" -> Json.obj(
              "lowVolume"  -> "1",
              "highVolume" -> "2"
            ),
            "monetaryValues" -> Json.obj(
              "lowVolume"    -> 1.00,
              "highVolume"   -> 2.00,
              "levySubtotal" -> 3.00
            )
          ),
          "wastage" -> Json.obj(
            "values" -> Json.obj(
              "lowVolume"  -> "3",
              "highVolume" -> "4"
            ),
            "monetaryValues" -> Json.obj(
              "lowVolume"    -> 3.00,
              "highVolume"   -> 4.00,
              "levySubtotal" -> 7.00
            )
          )
        )

      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(legacyPayload)

      val result = controller.createReturn(sdilRef)(request)

      status(result) shouldBe CREATED
    }
  }
}
