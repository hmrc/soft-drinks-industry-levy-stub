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

package uk.gov.hmrc.softdrinksindustrylevystub.controllers

import java.io.{ BufferedWriter, FileWriter }
import uk.gov.hmrc.softdrinksindustrylevystub.services._
import SubscriptionGenerator.genSubscription

object Report extends App {

  def apply(qty: Int): Iterable[String] = {
    for {
      i <- 1 to qty
    } yield {
      val utr: String = SdilNumberTransformer.tolerantUtr(i)
      val sdil: Option[String] = if (utr.last == '0') None else Some(SdilNumberTransformer.sdilRefEnum(i))
      val rtype = (utr.last) match {
        case '0' => None
        case '1' => Some("Small")
        case '2' => Some("Large")
        case '3' => Some("Importer")
        case '4' => Some("Copacker")
        case '5' => Some("Voluntary")
        case '6' => Some("Any")
        case '7' => Some("Simulate DES Slowdown")
        case '8' => Some("Simulate DES Unreliability")
        case '9' => Some("DES Deluxe")
      }

      s"$utr, ${sdil.getOrElse("-")}, ${rtype.getOrElse("-")}"
    }
  }

  val outFile = new java.io.File("target/ids.csv")
  val bw = new BufferedWriter(new FileWriter(outFile))
  bw.write("UTR, SDIL Ref, Account Type\n")
  for(x <- apply(1000000)){
    bw.write(x)
    bw.write("\n")
  }
  bw.close
}
