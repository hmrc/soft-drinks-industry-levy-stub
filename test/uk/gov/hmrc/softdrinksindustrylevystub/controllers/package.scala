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
      |    "registration": {
      |        "organisationType": "1",
      |        "applicationDate": "1920-02-29",
      |        "taxStartDate": "1920-02-29",
      |        "cin": "1097172564",
      |        "tradingName": "a",
      |        "businessContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Some Street",
      |                "line3": " ",
      |                "line4": " ",
      |                "postCode": "AB012AA",
      |                "country": "GB"
      |            },
      |            "contactDetails": {
      |                "telephone": "+44 1234567890",
      |                "mobile": "+44-(0)7890123456",
      |                "fax": "01234567111",
      |                "email": "a.b@c.com"
      |            }
      |        },
      |        "correspondenceContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Someother Street",
      |                "line3": " ",
      |                "line4": "Somewhere Else",
      |                "postCode": "AB012CC",
      |                "country": "GB"
      |            },
      |            "contactDetails": {
      |                "telephone": " ",
      |                "mobile": " ",
      |                "email": "a.b@c.com"
      |            },
      |            "differentAddress": true
      |        },
      |        "primaryPersonContact": {
      |            "name": "a",
      |            "positionInCompany": "a",
      |            "telephone": "a",
      |            "mobile": "a",
      |            "email": "a.b@c.com"
      |        },
      |        "details": {
      |            "producer": true,
      |            "producerDetails": {
      |                "produceMillionLitres": true,
      |                "producerClassification": "1",
      |                "smallProducerExemption": true,
      |                "useContractPacker": true,
      |                "voluntarilyRegistered": true
      |            },
      |            "importer": true,
      |            "contractPacker": true
      |        },
      |        "activityQuestions": {
      |            "litresProducedUKHigher": 2,
      |            "litresProducedUKLower": 2,
      |            "litresImportedUKHigher": 2,
      |            "litresImportedUKLower": 2,
      |            "litresPackagedUKHigher": 2,
      |            "litresPackagedUKLower": 2
      |        },
      |        "estimatedTaxAmount": 0.02,
      |        "taxObligationStartDate": "1920-02-29"
      |    },
      |    "sites": [
      |        {
      |            "action": "1",
      |            "tradingName": "a",
      |            "newSiteRef": "a",
      |            "siteAddress": {
      |                "addressDetails": {
      |                    "notUKAddress": true,
      |                    "line1": " ",
      |                    "line2": " ",
      |                    "line3": " ",
      |                    "line4": " ",
      |                    "postCode": "A00AA",
      |                    "country": "FR"
      |                },
      |                "contactDetails": {
      |                    "telephone": " ",
      |                    "mobile": " ",
      |                    "email": "a.b@c.com"
      |                }
      |            },
      |            "siteType": "2"
      |        },
      |        {
      |            "action": "1",
      |            "tradingName": "a",
      |            "newSiteRef": "a",
      |            "siteAddress": {
      |                "addressDetails": {
      |                    "notUKAddress": true,
      |                    "line1": " ",
      |                    "line2": " ",
      |                    "line3": " ",
      |                    "line4": " ",
      |                    "postCode": "A00AA",
      |                    "country": "DE"
      |                },
      |                "contactDetails": {
      |                    "telephone": " ",
      |                    "mobile": " ",
      |                    "email": "a.b@c.com"
      |                }
      |            },
      |            "siteType": "2"
      |        }
      |    ],
      |    "entityAction": [
      |        {
      |            "action": "1",
      |            "entityType": "4",
      |            "organisationType": "1",
      |            "cin": "a",
      |            "tradingName": "a",
      |            "businessContact": {
      |                "addressDetails": {
      |                    "notUKAddress": false,
      |                    "line1": " ",
      |                    "line2": " ",
      |                    "line3": " ",
      |                    "line4": " ",
      |                    "postCode": "A00AA",
      |                    "country": "GB"
      |                },
      |                "contactDetails": {
      |                    "telephone": " ",
      |                    "mobile": " ",
      |                    "email": "a.b@c.com"
      |                }
      |            }
      |        },
      |        {
      |            "action": "1",
      |            "entityType": "4",
      |            "organisationType": "1",
      |            "cin": "a",
      |            "tradingName": "a",
      |            "businessContact": {
      |                "addressDetails": {
      |                    "notUKAddress": false,
      |                    "line1": " ",
      |                    "line2": " ",
      |                    "line3": " ",
      |                    "line4": " ",
      |                    "postCode": "A00AA",
      |                    "country": "GB"
      |                },
      |                "contactDetails": {
      |                    "telephone": " ",
      |                    "mobile": " ",
      |                    "email": "a.b@c.com"
      |                }
      |            }
      |        }
      |    ]
      |}
    """.stripMargin
  )

  val validCreateSubscriptionRequestInput: JsValue = Json.parse(
    """{
      |    "registration": {
      |        "organisationType": "1",
      |        "applicationDate": "1920-02-29",
      |        "taxStartDate": "1920-02-29",
      |        "cin": "1097172564",
      |        "tradingName": "a",
      |        "businessContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Some Street",
      |                "postCode": "AB012AA",
      |                "country": "GB"
      |            },
      |            "contactDetails": {
      |                "telephone": "+44 1234567890",
      |                "mobile": "+44-(0)7890123456",
      |                "fax": "01234567111",
      |                "email": "a.b@c.com"
      |            }
      |        },
      |        "correspondenceContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Someother Street",
      |                "line4": "Somewhere Else",
      |                "postCode": "AB012CC",
      |                "country": "GB"
      |            },
      |            "contactDetails": {
      |                "telephone": "+44 1234567890",
      |                "mobile": "+44 1234567890",
      |                "email": "a.b@c.com"
      |            },
      |            "differentAddress": true
      |        },
      |        "primaryPersonContact": {
      |            "name": "a",
      |            "positionInCompany": "a",
      |            "telephone": "+44 1234567890",
      |            "mobile": "+44 1234567890",
      |            "email": "a.b@c.com"
      |        },
      |        "details": {
      |            "producer": true,
      |            "producerDetails": {
      |                "produceMillionLitres": true,
      |                "producerClassification": "1",
      |                "smallProducerExemption": true,
      |                "useContractPacker": true,
      |                "voluntarilyRegistered": true
      |            },
      |            "importer": true,
      |            "contractPacker": true
      |        },
      |        "activityQuestions": {
      |            "litresProducedUKHigher": 2,
      |            "litresProducedUKLower": 2,
      |            "litresImportedUKHigher": 2,
      |            "litresImportedUKLower": 2,
      |            "litresPackagedUKHigher": 2,
      |            "litresPackagedUKLower": 2
      |        },
      |        "estimatedTaxAmount": 0.02,
      |        "taxObligationStartDate": "1920-02-29"
      |    },
      |    "sites": [
      |        {
      |            "action": "1",
      |            "tradingName": "a",
      |            "newSiteRef": "a",
      |            "siteAddress": {
      |                "addressDetails": {
      |                    "notUKAddress": true,
      |                    "line1": "Juicey Juices",
      |                    "line2": "Juicey Juices",
      |                    "postCode": "A00AA",
      |                    "country": "FR"
      |                },
      |                "contactDetails": {
      |                    "telephone": "+44 1234567890",
      |                    "mobile": "+44 1234567890",
      |                    "email": "a.b@c.com"
      |                }
      |            },
      |            "siteType": "2"
      |        },
      |        {
      |            "action": "1",
      |            "tradingName": "a",
      |            "newSiteRef": "a",
      |            "siteAddress": {
      |                "addressDetails": {
      |                    "notUKAddress": true,
      |                    "line1": "asdasdasd",
      |                    "line2": "asfdsdasd",
      |                    "postCode": "A00AA",
      |                    "country": "DE"
      |                },
      |                "contactDetails": {
      |                    "telephone": "+44 1234567890",
      |                    "mobile": "+44 1234567890",
      |                    "email": "a.b@c.com"
      |                }
      |            },
      |            "siteType": "2"
      |        }
      |    ],
      |    "entityAction": [
      |        {
      |            "action": "1",
      |            "entityType": "4",
      |            "organisationType": "1",
      |            "cin": "a",
      |            "tradingName": "a",
      |            "businessContact": {
      |                "addressDetails": {
      |                    "notUKAddress": false,
      |                    "line1": "asdasdas",
      |                    "line2": "asdasd",
      |                    "postCode": "A00AA",
      |                    "country": "GB"
      |                },
      |                "contactDetails": {
      |                    "telephone": "+44 1234567890",
      |                    "mobile": "+44 1234567890",
      |                    "email": "a.b@c.com"
      |                }
      |            }
      |        },
      |        {
      |            "action": "1",
      |            "entityType": "4",
      |            "organisationType": "1",
      |            "cin": "a",
      |            "tradingName": "a",
      |            "businessContact": {
      |                "addressDetails": {
      |                    "notUKAddress": false,
      |                    "line1": "asdasd",
      |                    "line2": "aqasda",
      |                    "postCode": "A00AA",
      |                    "country": "GB"
      |                },
      |                "contactDetails": {
      |                    "telephone": "+44 1234567890",
      |                    "mobile": "+44 1234567890",
      |                    "email": "a.b@c.com"
      |                }
      |            }
      |        }
      |    ]
      |}
    """.stripMargin
  )

  val validCreateSubscriptionRequestInputWithoutOptionals: JsValue = Json.parse(
    """{
      |    "registration": {
      |        "organisationType": "1",
      |        "applicationDate": "1920-02-29",
      |        "taxStartDate": "1920-02-29",
      |        "cin": "1097172564",
      |        "tradingName": "a",
      |        "businessContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Some Street"
      |            },
      |            "contactDetails": {
      |                "telephone": "+44 1234567890",
      |                "email": "a.b@c.com"
      |            }
      |        },
      |        "correspondenceContact": {
      |            "addressDetails": {
      |                "notUKAddress": false,
      |                "line1": "Juicey Juices",
      |                "line2": "Someother Street",
      |                "line4": "Somewhere Else",
      |                "postCode": "AB012CC",
      |                "country": "GB"
      |            },
      |            "contactDetails": {
      |                "telephone": "+44 1234567890",
      |                "mobile": "+44 1234567890",
      |                "email": "a.b@c.com"
      |            }
      |        },
      |        "primaryPersonContact": {
      |            "name": "a",
      |			"telephone": "+44 1234567890",
      |            "email": "a.b@c.com"
      |        },
      |        "details": {
      |            "producer": true,
      |            "producerDetails": {
      |                "produceMillionLitres": true,
      |                "producerClassification": "1"
      |            },
      |            "importer": true,
      |            "contractPacker": true
      |        },
      |        "activityQuestions": {
      |        },
      |        "estimatedTaxAmount": 0.02,
      |        "taxObligationStartDate": "1920-02-29"
      |    },
      |    "sites": [],
      |    "entityAction": []
      |}
    """.stripMargin
  )

  val validRosmRegisterIndividualInput: JsValue = Json.parse(
    """{
      |	"regime": "ZSDL",
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
      |	"regime": "ZSDL",
      |	"requiresNameMatch": true,
      |	"isAnAgent": true,
      |	"organisation": {
      |		"organisationName": "Big Wig",
      |		"organisationType": "Partnership"
      |	}
      |}""".stripMargin
  )

  val invalidRosmRegime: JsValue = Json.parse(
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

  val validResponseOrganisation = OrganisationResponse("Big Wig", isAGroup = false, RosmOrganisationType.Partnership)

  val rosmRegisterIndividualResponse = RosmRegisterResponse(
    "safeID", None, isEditable = false, isAnAgent = false, isAnIndividual = true, Some(validResponseIndividual), None, validRosmResponseAddress, validRosmResponseContactDetails
  )

  val rosmRegisterOrganisationResponse = RosmRegisterResponse(
    "safeID", None, isEditable = false, isAnAgent = false, isAnIndividual = true, None, Some(validResponseOrganisation), validRosmResponseAddress, validRosmResponseContactDetails
  )

}
