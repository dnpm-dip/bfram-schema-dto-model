package de.dnpm.bfarm.model.onco


import java.io.{
  File,
  FileWriter
}
import scala.util.{
  Random,
  Using
}
import org.scalatest.flatspec.AnyFlatSpec
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.model.BaseCompleters
import de.dnpm.dip.mtb.model.MTBPatientRecord
import de.dnpm.dip.mtb.gens.Generators._
import de.dnpm.bfarm.model.onco.MTBMappings._
import de.dnpm.dip.util.mapping.syntax._
import play.api.libs.json.Json
import de.dnpm.dip.service.mvh.Submission
import de.dnpm.mvh.submission.gens.SubmissionGenerators


class Tests extends AnyFlatSpec
with SubmissionGenerators[MTBPatientRecord]
with BaseCompleters
{

  System.setProperty("dnpm.dip.site","UKT:Tübingen")

  implicit val rnd: Random = new Random(42)


  val dir = new File("/home/lucien/Downloads/mv_dummy_data/onco")
  dir.mkdirs

  val records = List.fill(42)(Gen.of[Submission[MTBPatientRecord]].next)


  "MTBMappings" must "have worked" in {

    records.map(_.mapTo[OncologySubmission])
      .tapEach { 
        submission =>
          val file = new File(dir,s"OncoSubmission_${submission.metadata.tanC}.json")
          Using.resource(new FileWriter(file)){
            _.write(Json.prettyPrint(Json.toJson(submission)))
          }  
      }

  }

}
