package de.dnpm.mvh.submission.test


import scala.util.Random
import scala.util.chaining._
import scala.jdk.CollectionConverters._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.Inspectors._
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.model.PatientRecord
import de.dnpm.dip.service.mvh.Submission
import de.dnpm.bfarm.model.base
import de.dnpm.mvh.submission.gens.SubmissionGenerators
import play.api.libs.json.{
  Json,
  Writes
}
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.{
  JsonSchemaFactory,
  SchemaLocation,
  SpecVersion
}


abstract class BaseMappingTest[T <: PatientRecord, S <: base.Submission[_,_,_,_]: Writes](
  useCase: String,
  schemaURL: String
)(
  implicit
  recordGen: Gen[T],
  mapping: Submission[T] => S
)
extends AnyFlatSpec
with SubmissionGenerators[T]
{

  // required because random-generated Patient objects within PatientRecord
  // don't have Patient.managingSite initialized
  System.setProperty("dnpm.dip.site","UKT:Tübingen")


  protected implicit val rnd: Random = new Random(42)

  protected val objectMapper = new ObjectMapper

  protected val schema =
    JsonSchemaFactory
      .getInstance(SpecVersion.VersionFlag.V202012)
      .getSchema(SchemaLocation.of(schemaURL))


  protected val submissions: Seq[Submission[T]] =
    List.fill(42)(Gen.of[Submission[T]].next)


  s"$useCase Mappings" must "have worked and produced schema-conformant JSON output" in {

    forAll(
      submissions.map(mapping)
        .map(Json.toJson(_))
        .map(Json.stringify)
    ){ 
      json => 

        val errors =
          schema.validate(objectMapper.readTree(json))
            .asScala
            .tap(_.foreach(msg => println(msg.getMessage)))

        errors must be (empty)    
    }

  }

}
