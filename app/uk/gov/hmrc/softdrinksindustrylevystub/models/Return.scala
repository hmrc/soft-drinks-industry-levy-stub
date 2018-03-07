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

package uk.gov.hmrc.softdrinksindustrylevystub.models

import play.api.Logger

case class Return(
                   periodKey: String,
                   fbType: String,
                   sdilRef: String,
                   revenueType: String,
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
      validateString("fbType", fbType, fbTypePattern),
      validateString("sdilRef", sdilRef, sdilRefPattern),
      validateString("revenueType", revenueType, revenueTypePattern),
      validateMonetary("netLevyDueTotal", netLevyDueTotal),
      packaging.forall(_.isValid),
      importing.forall(_.isValid),
      exporting.forall(_.isValid),
      wastage.forall(_.isValid)
    ).reduce(_ & _)

  }
}

case class ExpoWasted(
                    volumeNode: Option[Volume],
                    valueNode: Option[ValueNode]
                    ) {
  def isValid: Boolean = {
    Seq(
      volumeNode.forall(_.isValid),
      valueNode.forall(_.isValid)
    ).reduce( _ & _ )
  }
}

case class Importing(
                    volumeSmall: Option[Volume],
                    volumeLarge: Option[Volume],
                    valueNode: Option[ValueNode]
                    ) {
  def isValid: Boolean = {
    Seq(
      volumeSmall.forall(_.isValid),
      volumeLarge.forall(_.isValid),
      valueNode.forall(_.isValid)
    ).reduce( _ & _ )
  }
}

case class Packaging(
                    volumeSmall: Option[List[Item]],
                    volumeLarge: Option[Volume],
                    valueNode: Option[ValueNode]
                    ) {
  def isValid: Boolean = {
    Seq(
      volumeSmall.forall(_.forall(_.isValid)),
      volumeLarge.forall(_.isValid),
      valueNode.forall(_.isValid)).reduce( _ & _ )
//    volumeSmall.map(x => x.isValid) :+ volumeLarge.isValid :+ valueNode.isValid reduce( _ & _ )
  }
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
    ).reduce( _ & _ )
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
    ).reduce( _ & _ )

  }

}

case class ValueNode(
                      lowVolume: Option[BigDecimal],
                      highVolume: Option[BigDecimal],
                      levyTotal: Option[BigDecimal]
                    ) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateMonetary("lowVolume", lowVolume),
      validateMonetary("highVolume", highVolume),
      validateMonetary("levyTotal", levyTotal)
    ).reduce( _ & _ )
  }
}

case class ReturnSuccessResponse(
                            formBundleNumber: String
                          )

case class ReturnFailureResponse(
                          code: String,
                          reason: String
                          )

object ReturnValidation {

  val sdilRefPattern = "^X[A-Z]{1}SDIL000[0-9]{6}$"
  val periodKeyPattern = "^[0-9]{2}C[1-4]{1}$"
  val fbTypePattern = "^ZSD1$"
  val revenueTypePattern = "^Z045$"
  val volumeStringPattern = "^[0-9]{1,13}$"

  def validateString(label: String, value: String, regex: String): Boolean = {
    val r = regex.r
    value match {
      case r() => true
      case _ =>
        Logger.error(s"Invalid Return: $label $value doesn't match $regex")
        false
    }
  }

  def validateString(label: String, value: Option[String], regex: String): Boolean = {
    val r = regex.r
    value match {
      case None | Some(r()) => true
      case _ =>
        Logger.error(s"Invalid Return: $label ${value.getOrElse("<empty>")} doesn't match $regex")
        false
    }
  }

  def validateMonetary(label: String, value: Option[BigDecimal]): Boolean = {
    val n: BigDecimal = 99999999999.99
    value match {
      case Some(a) if a >= -n && a <= n => true
      case _ =>
        Logger.error(s"Invalid Return: $label $value is either > $n or < -$n")
        false
    }
  }

  def validateMonetary(label: String, value: BigDecimal): Boolean = {
    val n: BigDecimal = 99999999999.99
    value match {
      case a if a >= -n && a <= n => true
      case _ =>
        Logger.error(s"Invalid Return: $label $value is either > $n or < -$n")
        false
    }
  }

}