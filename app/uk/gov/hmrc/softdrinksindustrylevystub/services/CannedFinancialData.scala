/*
 * Copyright 2024 HM Revenue & Customs
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

import scala.jdk.CollectionConverters._
import java.io._
import play.api.libs.json._
import cats.implicits._
import com.fasterxml.jackson.core.JsonParseException
import sdil.models.des._
import FinancialTransaction._
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import java.nio.file.Paths

object CannedFinancialData {

  case class SchemaValidator(path: String) {

    private val schema: JsonNode = {
      val stream = getClass.getResourceAsStream(path)
      val schemaText = scala.io.Source.fromInputStream(stream).getLines().mkString
      stream.close()
      JsonLoader.fromString(schemaText)
    }

    val validator = JsonSchemaFactory.byDefault.getValidator

    def report(model: JsValue): ProcessingReport = {
      val json = JsonLoader.fromString(Json.prettyPrint(model))
      validator.validate(schema, json)
    }

    def apply(model: JsValue): Either[Throwable, JsValue] =
      for {
        report <- Either.catchOnly[ProcessingException] { report(model) }
        _      <- if (!report.isSuccess) report.iterator.asScala.toList.map { _.asException }.head.asLeft else ().asRight
      } yield (model)
  }

  def read(file: File): Either[Throwable, FinancialTransactionResponse] =
    for {
      stream <- Either.catchNonFatal(new FileInputStream(file))
      json   <- Either.catchOnly[JsonParseException](Json.parse(stream))
      _      <- SchemaValidator("/des-financial-data.schema.json")(json)
      obj    <- Either.catchOnly[JsResultException](json.as[FinancialTransactionResponse])
    } yield (obj)

  val path = Paths.get(getClass.getResource("/canned-data").toURI)

  lazy val canned = path.toFile.listFiles.toList
    .filter(_.getName.endsWith(".json"))
    .sortBy(_.getName)
    .map { f =>
      (f, read(f))
    }

  def bad = path.toFile.listFiles.toList.flatMap { f =>
    read(f) match {
      case Left(e) => List(new IllegalStateException(s"unable to parse $f", e))
      case _       => Nil
    }
  }
}
