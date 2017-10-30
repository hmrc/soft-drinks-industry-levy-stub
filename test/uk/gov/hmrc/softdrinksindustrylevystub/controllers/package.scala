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

package uk.gov.hmrc.softdrinksindustrylevystub

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.softdrinksindustrylevystub.models._

package object controllers {
  val invalidCreationInput: JsValue = Json.parse(
    """{
      |	"organisationType": "LimitedCompany BORKED",
      |	"applicationDate": "2017-09-12",
      |	"taxStartDate": "2017-09-12",
      |	"cin": "1097172564",
      |	"tradingName": "some trading name",
      |	"businessContact": {
      |		"notUKAddress": false,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"postCode": "ABC123",
      |		"telephone": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"correspondenceAddressDiffers": false,
      |	"primaryPerson": {
      |		"name": "foo",
      |		"telephone": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"softDrinksIndustryLevyDetails": {
      |		"activities": "ContractPacker",
      |		"lessThanMillion": true,
      |		"smallProducerExemption": false,
      |		"usesCopacker": false,
      |		"voluntarilyRegistered": false
      |	},
      |	"sdilActivity": {},
      |	"taxObligationStartDate": "2017-09-12",
      |	"bankDetails": {
      |		"directDebit": false
      |	},
      |	"sites": [{
      |		"address": {
      |			"notUKAddress": false,
      |			"line1": "line1",
      |			"line2": "line2",
      |			"postCode": "ABC123",
      |			"telephone": "123123123",
      |			"email": "foo@bar.com"
      |		}
      |	}]
      |}
    """.stripMargin
  )

  val successfulRetrieveOutput: JsValue = Json.parse(
    """{
      |	"organisationType": "LimitedCompany",
      |	"action": "Add",
      |	"typeOfEntity": "GroupMember",
      |	"applicationDate": "2017-09-12",
      |	"taxStartDate": "2017-09-12",
      |	"joiningDate": "2017-09-12",
      |	"leavingDate": "2017-09-12",
      |	"cin": "1097172564",
      |	"tradingName": "some trading name",
      |	"businessContact": {
      |		"notUKAddress": true,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"line3": "line3",
      |		"line4": "line4",
      |		"postCode": "XYZ123",
      |		"country": "Ukraine",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com",
      |		"fax": "123123123"
      |	},
      |	"correspondenceAddressDiffers": true,
      |	"correspondenceAddress": {
      |		"notUKAddress": true,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"line3": "line3",
      |		"line4": "line4",
      |		"postCode": "XYZ123",
      |		"country": "Uganda",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com",
      |		"fax": "123123123"
      |	},
      |	"primaryPerson": {
      |		"name": "foo",
      |		"positionInCompany": "Boss",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"softDrinksIndustryLevyDetails": {
      |		"activities": "ContractPacker",
      |		"lessThanMillion": true,
      |		"producerClassification": "Large",
      |		"smallProducerExemption": false,
      |		"usesCopacker": false,
      |		"voluntarilyRegistered": false
      |	},
      |	"sdilActivity": {
      |		"litresProducedUKLower": 1,
      |		"litresProducedUKHigher": 2,
      |		"litresImportedUKLower": 3,
      |		"litresImportedUKHigher": 4,
      |		"litresPackagedUKLower": 5,
      |		"litresPackagedUKHigher": 6
      |	},
      |	"estimatedTaxAmount": 5000,
      |	"taxObligationStartDate": "2017-09-12",
      |	"bankDetails": {
      |		"directDebit": false,
      |		"accountName": "some account name",
      |		"accountNumber": "some account number",
      |		"sortCode": "some sort code",
      |		"buildingSocietyRollNumber": "building society roll number"
      |	},
      |	"sites": [{
      |		"action": "NewSite",
      |		"siteReference": "foo",
      |		"dateOfClosure": "2017-09-12",
      |		"siteClosureReason": "rats",
      |		"tradingName": "cats",
      |		"newSiteReference": "foobar",
      |		"address": {
      |			"notUKAddress": true,
      |			"line1": "line1",
      |			"line2": "line2",
      |			"line3": "line3",
      |			"line4": "line4",
      |			"postCode": "XYZ123",
      |			"country": "Uganda",
      |			"telephone": "123123123",
      |			"mobile": "123123123",
      |			"email": "foo@bar.com",
      |			"fax": "123123123"
      |		},
      |		"typeOfSite": "Warehouse"
      |	}]
      |}
    """.stripMargin
  )

  val validCreateSubscriptionRequestInput: JsValue = Json.parse(
    """{
      |	"organisationType": "LimitedCompany",
      |	"action": "Add",
      |	"typeOfEntity": "GroupMember",
      |	"applicationDate": "2017-09-12",
      |	"taxStartDate": "2017-09-12",
      |	"joiningDate": "2017-09-12",
      |	"leavingDate": "2017-09-12",
      |	"cin": "1097172564",
      |	"tradingName": "some trading name",
      |	"businessContact": {
      |		"notUKAddress": true,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"line3": "line3",
      |		"line4": "line4",
      |		"postCode": "XYZ123",
      |		"country": "Ukraine",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com",
      |		"fax": "123123123"
      |	},
      |	"correspondenceAddressDiffers": true,
      |	"correspondenceAddress": {
      |		"notUKAddress": true,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"line3": "line3",
      |		"line4": "line4",
      |		"postCode": "XYZ123",
      |		"country": "Uganda",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com",
      |		"fax": "123123123"
      |	},
      |	"primaryPerson": {
      |		"name": "foo",
      |		"positionInCompany": "Boss",
      |		"telephone": "123123123",
      |		"mobile": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"softDrinksIndustryLevyDetails": {
      |		"activities": "ContractPacker",
      |		"lessThanMillion": true,
      |		"producerClassification": "Large",
      |		"smallProducerExemption": false,
      |		"usesCopacker": false,
      |		"voluntarilyRegistered": false
      |	},
      |	"sdilActivity": {
      |		"litresProducedUKLower": 1,
      |		"litresProducedUKHigher": 2,
      |		"litresImportedUKLower": 3,
      |		"litresImportedUKHigher": 4,
      |		"litresPackagedUKLower": 5,
      |		"litresPackagedUKHigher": 6
      |	},
      |	"estimatedTaxAmount": 5000,
      |	"taxObligationStartDate": "2017-09-12",
      |	"bankDetails": {
      |		"directDebit": false,
      |		"accountName": "some account name",
      |		"accountNumber": "some account number",
      |		"sortCode": "some sort code",
      |		"buildingSocietyRollNumber": "building society roll number"
      |	},
      |	"sites": [{
      |		"action": "NewSite",
      |		"siteReference": "foo",
      |		"dateOfClosure": "2017-09-12",
      |		"siteClosureReason": "rats",
      |		"tradingName": "cats",
      |		"newSiteReference": "foobar",
      |		"address": {
      |			"notUKAddress": true,
      |			"line1": "line1",
      |			"line2": "line2",
      |			"line3": "line3",
      |			"line4": "line4",
      |			"postCode": "XYZ123",
      |			"country": "Uganda",
      |			"telephone": "123123123",
      |			"mobile": "123123123",
      |			"email": "foo@bar.com",
      |			"fax": "123123123"
      |		},
      |		"typeOfSite": "Warehouse"
      |	}]
      |}
    """.stripMargin
  )

  val validCreateSubscriptionRequestInputWithoutOptionals: JsValue = Json.parse(
    """{
      |	"organisationType": "LimitedCompany",
      |	"applicationDate": "2017-09-12",
      |	"taxStartDate": "2017-09-12",
      |	"cin": "1097172564",
      |	"tradingName": "some trading name",
      |	"businessContact": {
      |		"notUKAddress": false,
      |		"line1": "line1",
      |		"line2": "line2",
      |		"postCode": "ABC123",
      |		"telephone": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"correspondenceAddressDiffers": false,
      |	"primaryPerson": {
      |		"name": "foo",
      |		"telephone": "123123123",
      |		"email": "foo@bar.com"
      |	},
      |	"softDrinksIndustryLevyDetails": {
      |		"activities": "ContractPacker",
      |		"lessThanMillion": true,
      |		"smallProducerExemption": false,
      |		"usesCopacker": false,
      |		"voluntarilyRegistered": false
      |	},
      |	"sdilActivity": {},
      |	"taxObligationStartDate": "2017-09-12",
      |	"bankDetails": {
      |		"directDebit": false
      |	},
      |	"sites": [{
      |		"address": {
      |			"notUKAddress": false,
      |			"line1": "line1",
      |			"line2": "line2",
      |			"postCode": "ABC123",
      |			"telephone": "123123123",
      |			"email": "foo@bar.com"
      |		}
      |	}]
      |}
    """.stripMargin
  )

  val validRosmRegisterIndividualInput: JsValue = Json.parse(
    """{
      |	"regime": "SDIL",
      |	"requiresNameMatch": true,
      |	"isAnAgent": true,
      |	"individual": {
      |		"firstName": "Stephen",
      |		"lastName": "Wood",
      |		"dateOfBirth": "1990-04-03"
      |	}
      |}""".stripMargin
  )

  val validRosmRegisterOrganisationnput: JsValue = Json.parse(
    """{
      |	"regime": "SDIL",
      |	"requiresNameMatch": true,
      |	"isAnAgent": true,
      |	"organisation": {
      |		"organisationName": "Big Wig",
      |		"organisationType": "Partnership"
      |	}
      |}""".stripMargin
  )

  val invalidRosmRegisterInput = """{"number": 1}"""

  val validRosmResponseAddress = RosmResponseAddress("50", Some("The Street"), None, None, "GB", "AA11 1AA")

  val validRosmResponseContactDetails = RosmResponseContactDetails(Some("12345678900"), None, None, Some("a@a.com"))

  val validResponseIndividual = Individual("Stephen", None, "Wood", Some(LocalDate.parse("1990-04-03")))

  val validResponseOrganisation = OrganisationResponse("Big Wig", false, RosmOrganisationType.Partnership)

  val rosmRegisterIndividualResponse = RosmRegisterResponse(
    "safeID", None, false, false, true, Some(validResponseIndividual), None, validRosmResponseAddress, validRosmResponseContactDetails
  )

  val rosmRegisterOrganisationResponse = RosmRegisterResponse(
    "safeID", None, false, false, true, None, Some(validResponseOrganisation), validRosmResponseAddress, validRosmResponseContactDetails
  )

}
