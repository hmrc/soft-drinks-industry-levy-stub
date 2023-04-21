/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.softdrinksindustrylevystub.models

import play.api.Logger

case class Return(
  periodKey: String,
  formBundleType: String,
  netLevyDueTotal: BigDecimal,
  packaging: Option[Packaging],
  importing: Option[Importing],
  exporting: Option[ExpoWasted],
  wastage: Option[ExpoWasted]
) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateString("periodKey", periodKey, periodKeyPattern),
      validateString("formBundleType", formBundleType, formBundleTypePattern),
      validateMonetary("netLevyDueTotal", netLevyDueTotal),
      packaging.forall(_.isValid),
      importing.forall(_.isValid),
      exporting.forall(_.isValid),
      wastage.forall(_.isValid)
    ).reduce(_ & _)

  }
}

case class ExpoWasted(
  values: Option[Volume],
  monetaryValues: Option[MonetaryValues]
) {
  def isValid: Boolean =
    Seq(
      values.forall(_.isValid),
      monetaryValues.forall(_.isValid)
    ).reduce(_ & _)
}

case class Importing(
  volumeSmall: Option[Volume],
  volumeLarge: Option[Volume],
  monetaryValues: Option[MonetaryValues]
) {
  def isValid: Boolean =
    Seq(
      volumeSmall.forall(_.isValid),
      volumeLarge.forall(_.isValid),
      monetaryValues.forall(_.isValid)
    ).reduce(_ & _)
}

case class Packaging(
  volumeSmall: Option[List[Item]],
  volumeLarge: Option[Volume],
  monetaryValues: Option[MonetaryValues]
) {
  def isValid: Boolean =
    Seq(volumeSmall.forall(_.forall(_.isValid)), volumeLarge.forall(_.isValid), monetaryValues.forall(_.isValid))
      .reduce(_ & _)
}

case class Item(
  producerRef: Option[String],
  lowVolume: Option[String],
  highVolume: Option[String]
) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateString("producerRef", producerRef, sdilRefPattern),
      validateString("lowVolume", lowVolume, volumeStringPattern),
      validateString("highVolume", highVolume, volumeStringPattern)
    ).reduce(_ & _)
  }

}

case class Volume(
  lowVolume: Option[String],
  highVolume: Option[String]
) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateString("lowVolume", lowVolume, volumeStringPattern),
      validateString("highVolume", highVolume, volumeStringPattern)
    ).reduce(_ & _)

  }

}

case class MonetaryValues(
  lowVolume: Option[BigDecimal],
  highVolume: Option[BigDecimal],
  levySubtotal: Option[BigDecimal]
) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateMonetary("lowVolume", lowVolume),
      validateMonetary("highVolume", highVolume),
      validateMonetary("levySubtotal", levySubtotal)
    ).reduce(_ & _)
  }
}

case class ReturnSuccessResponse(
  formBundleNumber: String
)

case class ReturnFailureResponse(
  code: String,
  reason: String
)

case object ReturnFailureResponse {

  val noBpKey = ReturnFailureResponse(
    "NOT_FOUND_BPKEY",
    "The remote endpoint has indicated that business partner key information cannot be found for the idNumber."
  )

  val invalidPeriodKey = ReturnFailureResponse(
    "INVALID_PERIOD_KEY",
    "The remote endpoint has indicated that the period key in the request is invalid."
  )

  val obligationFilled = ReturnFailureResponse(
    "OBLIGATION_FULFILLED",
    "The remote endpoint has indicated that the obligation for the period is already fulfilled."
  )

  val invalidPayload = ReturnFailureResponse(
    "INVALID_PAYLOAD",
    "Submission has not passed validation. Invalid Payload."
  )

  val invalidSdilRef = ReturnFailureResponse(
    "INVALID_SDIL_REFERENCE",
    "Submission has not passed validation. Invalid parameter sdilReference."
  )

}

object ReturnValidation {

  lazy val logger: Logger = Logger(this.getClass)

  val sdilRefPattern = "^X[A-Z]SDIL000[0-9]{6}$"
  val periodKeyPattern = "^[0-9]{2}C[1-4]{1}$"
  val formBundleTypePattern = "^ZSD1$"
  val volumeStringPattern = "^[0-9]{1,13}$"
  val monetaryHighLow: BigDecimal = 50000000000.00

  def validateString(label: String, value: String, regex: String): Boolean = {
    val r = regex.r
    value match {
      case r() => true
      case _ =>
        logger.error(s"Invalid Return: $label $value doesn't match $regex")
        false
    }
  }

  def validateString(label: String, value: Option[String], regex: String): Boolean = {
    val r = regex.r
    value match {
      case None | Some(r()) => true
      case _ =>
        logger.error(s"Invalid Return: $label ${value.getOrElse("<empty>")} doesn't match $regex")
        false
    }
  }

  def validateMonetary(label: String, value: Option[BigDecimal]): Boolean =
    value match {
      case Some(a) if a >= -monetaryHighLow && a <= monetaryHighLow => true
      case _ =>
        logger.error(s"Invalid Return: $label $value is either > $monetaryHighLow or < -$monetaryHighLow")
        false
    }

  def validateMonetary(label: String, value: BigDecimal): Boolean =
    value match {
      case a if a >= -monetaryHighLow && a <= monetaryHighLow => true
      case _ =>
        logger.error(s"Invalid Return: $label $value is either > $monetaryHighLow or < -$monetaryHighLow")
        false
    }

}
