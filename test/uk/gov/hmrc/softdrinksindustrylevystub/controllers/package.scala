/*
 * Copyright 2018 HM Revenue & Customs
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
      |  "businessAddress": {
      |    "line1": "Juicey Juices",
      |    "line2": "Some Street",
      |    "notUKAddress": false,
      |    "postCode": "AB012AA"
      |  },
      |  "businessContact": {
      |    "email": "a.b@c.com",
      |    "telephone": "+44 1234567890"
      |  },
      |  "sites": [
      |    {
      |      "siteAddress": {
      |        "country": "FR",
      |        "line1": "Juicey Juices",
      |        "line2": "Juicey Juices",
      |        "notUKAddress": true
      |      },
      |      "siteContact": {
      |        "email": "a.b@c.com",
      |        "telephone": "+44 1234567890"
      |      },
      |      "siteReference": "a",
      |      "siteType": "2",
      |      "tradingName": "a"
      |    },
      |    {
      |      "siteAddress": {
      |        "country": "DE",
      |        "line1": "asdasdasd",
      |        "line2": "asfdsdasd",
      |        "notUKAddress": true
      |      },
      |      "siteContact": {
      |        "email": "a.b@c.com",
      |        "telephone": "+44 1234567890"
      |      },
      |      "siteReference": "a",
      |      "siteType": "2",
      |      "tradingName": "a"
      |    }
      |  ],
      |  "subscriptionDetails": {
      |    "contractPacker": false,
      |    "importer": true,
      |    "largeProducer": false,
      |    "primaryContactName": "a",
      |    "primaryEmail": "a.b@c.com",
      |    "primaryPositionInCompany": "a",
      |    "primaryTelephone": "+44 1234567890",
      |    "sdilRegistrationNumber": "unknown",
      |    "smallProducer": true,
      |    "taxObligationEndDate": "1921-02-28",
      |    "taxObligationStartDate": "1920-02-29",
      |    "tradingName": "a",
      |    "voluntaryRegistration": false
      |  },
      |  "utr": "1097172564"
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
      |                "telephone": "44 1234567890",
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
      |                "line2": "Some Street",
      |                "postCode": "AA11 1AA"
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
      |            "telephone": "+44 1234567890",
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

  val validReturnPayload: JsValue = Json.parse(
    """{
      |    "periodKey": "17C4",
      |    "formBundleType": "ZSD1",
      |    "netLevyDueTotal": 50000000000.00,
      |    "packaging": {
      |        "volumeSmall": [
      |            {
      |                "producerRef": "XVSDIL000987654",
      |                "lowVolume": "1234",
      |                "highVolume": "9876543"
      |            }
      |        ],
      |        "volumeLarge": {
      |            "lowVolume": "432",
      |            "highVolume": "123234435"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8712.01,
      |            "highVolume": 854480.01,
      |            "levySubtotal": 639946.01
      |        }
      |    },
      |    "importing": {
      |        "volumeSmall": {
      |            "lowVolume": "44",
      |            "highVolume": "5675688"
      |        },
      |        "volumeLarge": {
      |            "lowVolume": "453435",
      |            "highVolume": "345456567"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 74859.01,
      |            "highVolume": 436644.01,
      |            "levySubtotal": 2470039.01
      |        }
      |    },
      |    "exporting": {
      |        "volumes": {
      |            "lowVolume": "8768",
      |            "highVolume": "784676534"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8068.01,
      |            "highVolume": 172698.01,
      |            "levySubtotal": 6699738186.01
      |        }
      |    },
      |    "wastage": {
      |        "volumes": {
      |            "lowVolume": "234",
      |            "highVolume": "9874653465"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 41212.01,
      |            "highVolume": 262503151.01,
      |            "levySubtotal": 5304161984.01
      |        }
      |    }
      |}""".stripMargin
  )

  val invalidReturnPayload: JsValue = Json.parse(
    """{
      |    "periodKey": "17C4",
      |    "formBundleType": "ZSD1WFT",
      |    "netLevyDueTotal": 99999999999.99,
      |    "packaging": {
      |        "volumeSmall": [
      |            {
      |                "producerRef": "XVSDIL000987654",
      |                "lowVolume": "1234",
      |                "highVolume": "9876543"
      |            }
      |        ],
      |        "volumeLarge": {
      |            "lowVolume": "432",
      |            "highVolume": "123234435"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8712.01,
      |            "highVolume": 854480.01,
      |            "levySubtotal": 639946.01
      |        }
      |    },
      |    "importing": {
      |        "volumeSmall": {
      |            "lowVolume": "44",
      |            "highVolume": "5675688"
      |        },
      |        "volumeLarge": {
      |            "lowVolume": "453435",
      |            "highVolume": "345456567"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 74859.01,
      |            "highVolume": 436644.01,
      |            "levySubtotal": 2470039.01
      |        }
      |    },
      |    "exporting": {
      |        "values": {
      |            "lowVolume": "8768",
      |            "highVolume": "784676534"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8068.01,
      |            "highVolume": 172698.01,
      |            "levySubtotal": 62699738186.01
      |        }
      |    },
      |    "wastage": {
      |        "values": {
      |            "lowVolume": "234",
      |            "highVolume": "9874653465"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 41212.01,
      |            "highVolume": 262503151.01,
      |            "levySubtotal": 52304161984.01
      |        }
      |    }
      |}""".stripMargin
  )

  val invalidPeriodKeyReturnPayload: JsValue = Json.parse(
    """{
      |    "periodKey": "17C4WTF",
      |    "formBundleType": "ZSD1",
      |    "netLevyDueTotal": 99999999999.99,
      |    "packaging": {
      |        "volumeSmall": [
      |            {
      |                "producerRef": "XVSDIL000987654",
      |                "lowVolume": "1234",
      |                "highVolume": "9876543"
      |            }
      |        ],
      |        "volumeLarge": {
      |            "lowVolume": "432",
      |            "highVolume": "123234435"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8712.01,
      |            "highVolume": 854480.01,
      |            "levySubtotal": 639946.01
      |        }
      |    },
      |    "importing": {
      |        "volumeSmall": {
      |            "lowVolume": "44",
      |            "highVolume": "5675688"
      |        },
      |        "volumeLarge": {
      |            "lowVolume": "453435",
      |            "highVolume": "345456567"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 74859.01,
      |            "highVolume": 436644.01,
      |            "levySubtotal": 2470039.01
      |        }
      |    },
      |    "exporting": {
      |        "values": {
      |            "lowVolume": "8768",
      |            "highVolume": "784676534"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 8068.01,
      |            "highVolume": 172698.01,
      |            "levySubtotal": 62699738186.01
      |        }
      |    },
      |    "wastage": {
      |        "values": {
      |            "lowVolume": "234",
      |            "highVolume": "9874653465"
      |        },
      |        "monetaryValues": {
      |            "lowVolume": 41212.01,
      |            "highVolume": 262503151.01,
      |            "levySubtotal": 52304161984.01
      |        }
      |    }
      |}""".stripMargin
  )

  val validReturnResponse: JsValue = Json.parse(
    """{
      |    "formBundleNumber": "531989282162"
      |}""".stripMargin
  )

}
