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

package uk.gov.hmrc.softdrinksindustrylevystub.services

import scala.collection.JavaConverters._
import java.io._
import play.api.libs.json._
import scala.io.Source
import scala.util.Try
import cats.implicits._
import com.fasterxml.jackson.core.JsonParseException
import sdil.models.des._
import FinancialTransaction._

object CannedFinancialData {

  def read(file: File): Either[Throwable, FinancialTransactionResponse] = for {
    stream <- Either.catchNonFatal(new FileInputStream(file))
    json <- Either.catchOnly[JsonParseException](Json.parse(stream))
    obj <- Either.catchOnly[JsResultException](json.as[FinancialTransactionResponse])
  } yield ( obj )

  val path = getClass.getResource("/canned-data").getPath
  lazy val canned = (new File(path)).listFiles.toList.map{ read }
  def bad = (new File(path)).listFiles.toList.flatMap { f => 
    read(f) match {
      case Left(e) => List(new IllegalStateException(s"unable to parse $f", e))
      case _ => Nil
    }
  }
}
