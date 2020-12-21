/*
 * Copyright 2020 HM Revenue & Customs
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

import java.io.{BufferedWriter, FileWriter}
import uk.gov.hmrc.softdrinksindustrylevystub.services._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import sys.process._
import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.sdilRefEnum

object Report extends App {

  def findRegistrationWhere(predicate: Subscription => Boolean): Subscription =
    sdilRefEnum.iterator.collectFirst { sdilRef =>
      Store.fromSdilRef(sdilRef) match {
        case Some(r) if predicate(r) => r
      }
    }.get

  implicit def boolToStr(i: Boolean): String = if (i) "yes" else "no"

  def postcode(addr: Address): String = addr match {
    case UkAddress(_, pc) => pc
    case _                => ""
  }

  def apply(qty: Int) = {
    val records = for {
      i <- (1 to qty).par
    } yield {
      val utr: String = SdilNumberTransformer.tolerantUtr(i)
      val record = Store.fromUtr(utr)
      (utr, record)
    }

    records.map {
      case (utr, optSub) =>
        "\"" ++ utr ++ "\"" :: {
          optSub match {
            case Some(sub) =>
              List[String](
                sub.sdilRef,
                sub.orgName,
                postcode(sub.address),
                sub.activity.isLarge,
                sub.activity.isImporter,
                sub.activity.isProducer,
                sub.activity.isSmallProducer,
                sub.activity.isContractPacker,
                sub.activity.isVoluntaryRegistration
              )
            case _ => List.empty[String]
          }
        }
    }
  }

  val outFile = new java.io.File("target/ids.csv")
  val bw = new BufferedWriter(new FileWriter(outFile))
  bw.write("UTR, SDIL Ref, Name, Postcode, Large, Importer, Producer, Small Producer, Contract Packer, Voluntary \n")

  for (x <- apply(100000).toList) {
    bw.write(x.mkString(","))
    bw.write("\n")
  }
  bw.close
  s"gnumeric target/ids.csv".!
}
