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

import play.api.libs.json.{JsValue, Json}

package object controllers {
  val invalidCreationInput: JsValue = Json.parse("""{
                                              "organisationType" : "LimitedCompany BORKED",
                                              "dateOfApplication" : "2017-09-12",
                                              "taxStartDate" : "2017-09-12",
                                              "customerIdentificationNumber" : "1097172564",
                                              "tradingName" : "some trading name",
                                              "businessContactDetails" : {
                                                "addressNotInUk" : false,
                                                "addressLine1" : "line1",
                                                "addressLine2" : "line2",
                                                "postcode" : "ABC123",
                                                "telephoneNumber" : "123123123",
                                                "emailAddress" : "foo@bar.com"
                                              },
                                              "correspondenceAddressDiffers" : false,
                                              "primaryPerson" : {
                                                "name" : "foo",
                                                "telephoneNumber" : "123123123",
                                                "emailAddress" : "foo@bar.com"
                                              },
                                              "softDrinksIndustryLevyDetails" : {
                                                "activities" : "ContractPacker",
                                                "lessThanMillion" : true,
                                                "smallProducerExemption" : false,
                                                "usesCopacker" : false,
                                                "voluntarilyRegistered" : false
                                              },
                                              "sdilActivity" : {
                                              },
                                              "taxObligationStartDate" : "2017-09-12",
                                              "bankDetails" : {
                                                "directDebit" : false
                                              },
                                              "sites" : [ {
                                                "address" : {
                                                  "addressNotInUk" : false,
                                                  "addressLine1" : "line1",
                                                  "addressLine2" : "line2",
                                                  "postcode" : "ABC123",
                                                  "telephoneNumber" : "123123123",
                                                  "emailAddress" : "foo@bar.com"
                                                }
                                              } ]
                                            }
                                            """)
  val successfulRetrieveOutput = Json.parse("""{
                                                  "organisationType" : "LimitedCompany",
                                                  "action" : "Add",
                                                  "typeOfEntity" : "GroupMember",
                                                  "dateOfApplication" : "2017-09-12",
                                                  "taxStartDate" : "2017-09-12",
                                                  "joiningDate" : "2017-09-12",
                                                  "leavingDate" : "2017-09-12",
                                                  "customerIdentificationNumber" : "1097172564",
                                                  "tradingName" : "some trading name",
                                                  "businessContactDetails" : {
                                                    "addressNotInUk" : true,
                                                    "addressLine1" : "line1",
                                                    "addressLine2" : "line2",
                                                    "addressLine3" : "line3",
                                                    "addressLine4" : "line4",
                                                    "postcode" : "XYZ123",
                                                    "nonUkCountry" : "Ukraine",
                                                    "telephoneNumber" : "123123123",
                                                    "mobileNumber": "123123123",
                                                    "emailAddress" : "foo@bar.com",
                                                    "faxNumber": "123123123"
                                                  },
                                                  "correspondenceAddressDiffers" : true,
                                                  "correspondenceAddress" : {
                                                    "addressNotInUk" : true,
                                                    "addressLine1" : "line1",
                                                    "addressLine2" : "line2",
                                                    "addressLine3" : "line3",
                                                    "addressLine4" : "line4",
                                                    "postcode" : "XYZ123",
                                                    "nonUkCountry" : "Uganda",
                                                    "telephoneNumber" : "123123123",
                                                    "mobileNumber" : "123123123",
                                                    "emailAddress" : "foo@bar.com",
                                                    "faxNumber": "123123123"
                                                  },
                                                  "primaryPerson" : {
                                                    "name" : "foo",
                                                    "positionInCompany" : "Boss",
                                                    "telephoneNumber" : "123123123",
                                                    "mobileNumber" : "123123123",
                                                    "emailAddress" : "foo@bar.com"
                                                  },
                                                  "softDrinksIndustryLevyDetails" : {
                                                    "activities" : "ContractPacker",
                                                    "lessThanMillion" : true,
                                                    "producerClassification" : "Large",
                                                    "smallProducerExemption" : false,
                                                    "usesCopacker" : false,
                                                    "voluntarilyRegistered" : false
                                                  },
                                                  "sdilActivity" : {
                                                    "producedLower" : 1,
                                                    "producedHigher" : 2,
                                                    "importedLower" : 3,
                                                    "importedHigher" : 4,
                                                    "packagedLower" : 5,
                                                    "packagedHigher" : 6
                                                  },
                                                  "estimatedAmountOfTaxInTheNext12Months" : 5000,
                                                  "taxObligationStartDate" : "2017-09-12",
                                                  "bankDetails" : {
                                                    "directDebit" : false,
                                                    "accountName" : "some account name",
                                                    "accountNumber" : "some account number",
                                                    "sortCode" : "some sort code",
                                                    "buildingSocietyRollNumber" : "building society roll number"
                                                  },
                                                  "sites" : [ {
                                                    "action" : "NewSite",
                                                    "siteReference" : "foo",
                                                    "dateOfClosure" : "2017-09-12",
                                                    "siteClosureReason" : "rats",
                                                    "tradingName" : "cats",
                                                    "newSiteReference" : "foobar",
                                                    "address" : {
                                                      "addressNotInUk" : true,
                                                      "addressLine1" : "line1",
                                                      "addressLine2" : "line2",
                                                      "addressLine3" : "line3",
                                                      "addressLine4" : "line4",
                                                      "postcode" : "XYZ123",
                                                      "nonUkCountry" : "Uganda",
                                                      "telephoneNumber" : "123123123",
                                                      "mobileNumber": "123123123",
                                                      "emailAddress" : "foo@bar.com",
                                                      "faxNumber": "123123123"
                                                    },
                                                    "typeOfSite" : "Warehouse"
                                                  } ]
                                                }
                                                """)
  val validCreateSubscriptionRequestInput = Json.parse("""{
                                                            "organisationType" : "LimitedCompany",
                                                            "action" : "Add",
                                                            "typeOfEntity" : "GroupMember",
                                                            "dateOfApplication" : "2017-09-12",
                                                            "taxStartDate" : "2017-09-12",
                                                            "joiningDate" : "2017-09-12",
                                                            "leavingDate" : "2017-09-12",
                                                            "customerIdentificationNumber" : "1097172564",
                                                            "tradingName" : "some trading name",
                                                            "businessContactDetails" : {
                                                              "addressNotInUk" : true,
                                                              "addressLine1" : "line1",
                                                              "addressLine2" : "line2",
                                                              "addressLine3" : "line3",
                                                              "addressLine4" : "line4",
                                                              "postcode" : "XYZ123",
                                                              "nonUkCountry" : "Ukraine",
                                                              "telephoneNumber" : "123123123",
                                                              "mobileNumber": "123123123",
                                                              "emailAddress" : "foo@bar.com",
                                                              "faxNumber": "123123123"
                                                            },
                                                            "correspondenceAddressDiffers" : true,
                                                            "correspondenceAddress" : {
                                                              "addressNotInUk" : true,
                                                              "addressLine1" : "line1",
                                                              "addressLine2" : "line2",
                                                              "addressLine3" : "line3",
                                                              "addressLine4" : "line4",
                                                              "postcode" : "XYZ123",
                                                              "nonUkCountry" : "Uganda",
                                                              "telephoneNumber" : "123123123",
                                                              "mobileNumber" : "123123123",
                                                              "emailAddress" : "foo@bar.com",
                                                              "faxNumber": "123123123"
                                                            },
                                                            "primaryPerson" : {
                                                              "name" : "foo",
                                                              "positionInCompany" : "Boss",
                                                              "telephoneNumber" : "123123123",
                                                              "mobileNumber" : "123123123",
                                                              "emailAddress" : "foo@bar.com"
                                                            },
                                                            "softDrinksIndustryLevyDetails" : {
                                                              "activities" : "ContractPacker",
                                                              "lessThanMillion" : true,
                                                              "producerClassification" : "Large",
                                                              "smallProducerExemption" : false,
                                                              "usesCopacker" : false,
                                                              "voluntarilyRegistered" : false
                                                            },
                                                            "sdilActivity" : {
                                                              "producedLower" : 1,
                                                              "producedHigher" : 2,
                                                              "importedLower" : 3,
                                                              "importedHigher" : 4,
                                                              "packagedLower" : 5,
                                                              "packagedHigher" : 6
                                                            },
                                                            "estimatedAmountOfTaxInTheNext12Months" : 5000,
                                                            "taxObligationStartDate" : "2017-09-12",
                                                            "bankDetails" : {
                                                              "directDebit" : false,
                                                              "accountName" : "some account name",
                                                              "accountNumber" : "some account number",
                                                              "sortCode" : "some sort code",
                                                              "buildingSocietyRollNumber" : "building society roll number"
                                                            },
                                                            "sites" : [ {
                                                              "action" : "NewSite",
                                                              "siteReference" : "foo",
                                                              "dateOfClosure" : "2017-09-12",
                                                              "siteClosureReason" : "rats",
                                                              "tradingName" : "cats",
                                                              "newSiteReference" : "foobar",
                                                              "address" : {
                                                                "addressNotInUk" : true,
                                                                "addressLine1" : "line1",
                                                                "addressLine2" : "line2",
                                                                "addressLine3" : "line3",
                                                                "addressLine4" : "line4",
                                                                "postcode" : "XYZ123",
                                                                "nonUkCountry" : "Uganda",
                                                                "telephoneNumber" : "123123123",
                                                                "mobileNumber": "123123123",
                                                                "emailAddress" : "foo@bar.com",
                                                                "faxNumber": "123123123"
                                                              },
                                                              "typeOfSite" : "Warehouse"
                                                            } ]
                                                          }
                                                          """)
  val validCreateSubscriptionRequestInputWithoutOptionals = Json.parse("""{
                                                                            "organisationType" : "LimitedCompany",
                                                                            "dateOfApplication" : "2017-09-12",
                                                                            "taxStartDate" : "2017-09-12",
                                                                            "customerIdentificationNumber" : "1097172564",
                                                                            "tradingName" : "some trading name",
                                                                            "businessContactDetails" : {
                                                                              "addressNotInUk" : false,
                                                                              "addressLine1" : "line1",
                                                                              "addressLine2" : "line2",
                                                                              "postcode" : "ABC123",
                                                                              "telephoneNumber" : "123123123",
                                                                              "emailAddress" : "foo@bar.com"
                                                                            },
                                                                            "correspondenceAddressDiffers" : false,
                                                                            "primaryPerson" : {
                                                                              "name" : "foo",
                                                                              "telephoneNumber" : "123123123",
                                                                              "emailAddress" : "foo@bar.com"
                                                                            },
                                                                            "softDrinksIndustryLevyDetails" : {
                                                                              "activities" : "ContractPacker",
                                                                              "lessThanMillion" : true,
                                                                              "smallProducerExemption" : false,
                                                                              "usesCopacker" : false,
                                                                              "voluntarilyRegistered" : false
                                                                            },
                                                                            "sdilActivity" : {
                                                                            },
                                                                            "taxObligationStartDate" : "2017-09-12",
                                                                            "bankDetails" : {
                                                                              "directDebit" : false
                                                                            },
                                                                            "sites" : [ {
                                                                              "address" : {
                                                                                "addressNotInUk" : false,
                                                                                "addressLine1" : "line1",
                                                                                "addressLine2" : "line2",
                                                                                "postcode" : "ABC123",
                                                                                "telephoneNumber" : "123123123",
                                                                                "emailAddress" : "foo@bar.com"
                                                                              }
                                                                            } ]
                                                                          }
                                                                          """)
}
