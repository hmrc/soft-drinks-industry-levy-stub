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
import play.api.libs.json.*
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.softdrinksindustrylevystub.Store
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HipSubscriptionControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val cc: ControllerComponents = Helpers.stubControllerComponents()

  private val desSubmissionService = new DesSubmissionService

  private val extraActions = new ExtraActions(
    new AuthorisedFilterAction(cc),
    new EnvironmentFilterAction()(using cc.executionContext),
    new HipFilterAction(cc)
  )

  private val controller = new HipSubscriptionController(
    desSubmissionService,
    cc,
    extraActions
  )

  override def beforeEach(): Unit = {
    Store.clear()
    desSubmissionService.resetSubscriptions()
    super.beforeEach()
  }

  private val hipHeaders: Seq[(String, String)] =
    Seq(
      "correlationid"         -> "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "X-Originating-System"  -> "SDIL",
      "X-Receipt-Date"        -> "2026-03-10T12:34:46Z",
      "X-Transmitting-System" -> "HIP"
    )

  private val validCreateSubscriptionPayload: JsObject =
    Json.obj(
      "registration" -> Json.obj(
        "organisationType" -> "1",
        "applicationDate"  -> "2026-03-10",
        "taxStartDate"     -> "2026-03-10",
        "cin"              -> "1234567890",
        "tradingName"      -> "ACME Trading Ltd",
        "businessContact" -> Json.obj(
          "addressDetails" -> Json.obj(
            "notUKAddress" -> false,
            "line1"        -> "Juicey Juices",
            "line2"        -> "Some Street",
            "line3"        -> "Some Street3",
            "line4"        -> "Some Street4",
            "postCode"     -> "AB1 2AA",
            "country"      -> "GB"
          ),
          "contactDetails" -> Json.obj(
            "telephone" -> "01234567890",
            "mobile"    -> "07890123456",
            "fax"       -> "01234567111",
            "email"     -> "a.b@c.com"
          )
        ),
        "correspondenceContact" -> Json.obj(
          "addressDetails" -> Json.obj(
            "notUKAddress" -> false,
            "line1"        -> "Juicey Juices",
            "line2"        -> "Other Street",
            "line3"        -> "Some Street3",
            "line4"        -> "Somewhere Else",
            "postCode"     -> "AB1 2CC",
            "country"      -> "GB"
          ),
          "contactDetails" -> Json.obj(
            "telephone" -> "01234567890",
            "mobile"    -> "07890123456",
            "fax"       -> "01234567111",
            "email"     -> "a.b@c.com"
          ),
          "differentAddress" -> true
        ),
        "primaryPersonContact" -> Json.obj(
          "name"              -> "Mr Fred Bloggs",
          "positionInCompany" -> "Prod Director",
          "telephone"         -> "01234567890",
          "mobile"            -> "07890123456",
          "email"             -> "a.b@c.com"
        ),
        "details" -> Json.obj(
          "producer" -> true,
          "producerDetails" -> Json.obj(
            "produceMillionLitres"   -> true,
            "producerClassification" -> "1",
            "smallProducerExemption" -> true,
            "useContractPacker"      -> true,
            "voluntarilyRegistered"  -> true
          ),
          "importer"       -> true,
          "contractPacker" -> true
        ),
        "activityQuestions" -> Json.obj(
          "litresProducedUKHigher" -> 2,
          "litresProducedUKLower"  -> 2,
          "litresImportedUKHigher" -> 2,
          "litresImportedUKLower"  -> 2,
          "litresPackagedUKHigher" -> 2,
          "litresPackagedUKLower"  -> 2
        ),
        "estimatedTaxAmount"     -> 0,
        "taxObligationStartDate" -> "2026-03-10"
      ),
      "sites" -> Json.arr(
        Json.obj(
          "action"      -> "1",
          "tradingName" -> "ACME Site",
          "newSiteRef"  -> "SITE1",
          "siteAddress" -> Json.obj(
            "addressDetails" -> Json.obj(
              "notUKAddress" -> false,
              "line1"        -> "Site Line 1",
              "line2"        -> "Site Line 2",
              "postCode"     -> "AB1 2AA",
              "country"      -> "GB"
            ),
            "contactDetails" -> Json.obj(
              "telephone" -> "01234567890",
              "mobile"    -> "07890123456",
              "fax"       -> "01234567111",
              "email"     -> "site@test.com"
            )
          ),
          "siteType" -> "1"
        )
      )
    )

  "createSubscription" should {

    "return 201 with success wrapper for a valid HIP request" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validCreateSubscriptionPayload)

      val result = controller.createSubscription("utr", "1234567890")(request)

      status(result) shouldBe CREATED
      header("correlationid", result) shouldBe Some("a1b2c3d4-e5f6-7890-abcd-ef1234567890")

      val json = contentAsJson(result)
      (json \ "success" \ "processingDate").asOpt[String] shouldBe defined
      (json \ "success" \ "formBundleNumber").asOpt[String] shouldBe defined
    }

    "return 400 when required HIP headers are missing" in {
      val request =
        FakeRequest()
          .withBody(validCreateSubscriptionPayload)

      val result = controller.createSubscription("utr", "1234567890")(request)

      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when the payload is invalid" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(Json.obj("invalid" -> "payload"))

      val result = controller.createSubscription("utr", "1234567890")(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "003"
      (json \ "errors" \ "text").as[String] shouldBe "Request could not be processed"
    }
  }

  "retrieveSubscriptionDetails" should {

    "return 200 with success wrapper for an existing subscription" in {
      val createRequest =
        FakeRequest()
          .withHeaders(hipHeaders*)
          .withBody(validCreateSubscriptionPayload)

      val createResult = controller.createSubscription("utr", "1234567890")(createRequest)
      status(createResult) shouldBe CREATED

      val retrieveRequest =
        FakeRequest()
          .withHeaders(hipHeaders*)

      val retrieveResult =
        controller.retrieveSubscriptionDetails("utr", "1234567890")(retrieveRequest)

      status(retrieveResult) shouldBe OK
      header("correlationid", retrieveResult) shouldBe Some("a1b2c3d4-e5f6-7890-abcd-ef1234567890")

      val json = contentAsJson(retrieveResult)
      (json \ "success" \ "utr").as[String] shouldBe "1234567890"
      (json \ "success" \ "subscriptionDetails" \ "sdilRegistrationNumber").asOpt[String] shouldBe defined
      (json \ "success" \ "subscriptionDetails" \ "deregistrationDate") shouldBe JsDefined(JsNull)
    }

    "return 429 when the subscription idNumber is 0000010901" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)

      val result = controller.retrieveSubscriptionDetails("utr", "0000010901")(request)

      status(result) shouldBe TOO_MANY_REQUESTS

    }

    "return 422 with HIP error code 002 when the subscription cannot be found" in {
      val request =
        FakeRequest()
          .withHeaders(hipHeaders*)

      val result =
        controller.retrieveSubscriptionDetails("sdil", "XASDIL000000430")(request)

      status(result) shouldBe UNPROCESSABLE_ENTITY

      val json = contentAsJson(result)
      (json \ "errors" \ "code").as[String] shouldBe "002"
      (json \ "errors" \ "text").as[String] shouldBe "ID not Found"
    }

    "return 400 when required HIP headers are missing" in {
      val request = FakeRequest()

      val result = controller.retrieveSubscriptionDetails("utr", "1234567890")(request)

      status(result) shouldBe BAD_REQUEST
    }
  }
}
