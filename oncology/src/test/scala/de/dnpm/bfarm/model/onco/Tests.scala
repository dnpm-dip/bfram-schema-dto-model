package de.dnpm.bfarm.model.onco


import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.model.BaseCompleters
import de.dnpm.dip.mtb.model.MTBPatientRecord
import de.dnpm.dip.mtb.gens.Generators._
import de.dnpm.bfarm.model.onco.MTBMappings._
import de.dnpm.dip.util.mapping.syntax._
import de.dnpm.dip.service.mvh.Submission
import de.dnpm.mvh.submission.gens.SubmissionGenerators


class Tests extends AnyFlatSpec
with SubmissionGenerators[MTBPatientRecord]
with BaseCompleters
{

  System.setProperty("dnpm.dip.site","UKT:Tübingen")

  implicit val rnd: Random = new Random(42)

  val records = List.fill(42)(Gen.of[Submission[MTBPatientRecord]].next)


  "MTBMappings" must "have worked" in {

    noException must be thrownBy records.map(_.mapTo[OncologySubmission])

/*
    import play.api.libs.json.Json

    val dir = new java.io.File("/home/lucien/Downloads/mv_dummy_data/onco")
    dir.mkdirs

    noException must be thrownBy records.map(_.mapTo[OncologySubmission]).tapEach {
        submission =>
          val file = new java.io.File(dir,s"OncoSubmission_${submission.metaData.tanC}.json")
          scala.util.Using.resource(new java.io.FileWriter(file)){
            _.write(Json.prettyPrint(Json.toJson(submission)))
          }
      }
*/      
  }

}
