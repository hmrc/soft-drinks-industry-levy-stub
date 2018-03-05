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
                   packaging: Packaged,
                   importing: Produced,
                   exporting: Produced,
                   wastage: Produced
                 ) {


  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateString("periodKey", periodKey, periodKeyPattern),
      validateString("fbType", fbType, fbTypePattern),
      validateString("sdilRef", sdilRef, sdilRefPattern),
      validateString("revenueType", revenueType, revenueTypePattern),
      validateMonetary("netLevyDueTotal", netLevyDueTotal)
    ).reduce(_ & _)

  }
}

case class Item(
                producerRef: String,
                volume: Volume
                ) {

  def isValid: Boolean = {
    import ReturnValidation._
    Seq(
      validateString("producerRef", producerRef, sdilRefPattern),
      volume.isValid
    ).reduce( _ & _ )
  }

}

case class Volume(
                 lowVolume: String,
                 highVolume: String
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
                      lowVolume: BigDecimal,
                      highVolume: BigDecimal,
                      levyTotal: BigDecimal
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

case class Packaged(
                      items: List[Item],
                      produced: Produced
                    ) {
  def isValid: Boolean = {
    items.map(x => x.isValid) :+ produced.isValid reduce( _ & _ )
  }
}

case class Produced (
                      volumeNode: Volume,
                      valueNode: ValueNode
                    ) {

  def isValid: Boolean = {
    Seq(
      volumeNode.isValid,
      valueNode.isValid
    ).reduce( _ & _ )
  }

}

case class SuccessResponse(
                            formBundleNumber: String
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

  def validateMonetary(label: String, value: BigDecimal): Boolean = {
    val n: BigDecimal = 99999999999.99
    value match {
      case a if a > -n && a < n => true
      case _ =>
        Logger.error(s"Invalid Return: $label $value is either > $n or < -$n")
        false
    }
  }

}