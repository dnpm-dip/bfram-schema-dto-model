package de.dnpm.bfarm.model.rd


import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.model.BaseCompleters
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.gens.Generators._
import de.dnpm.bfarm.model.rd.RDMappings._
import de.dnpm.dip.util.mapping.syntax._
import de.dnpm.dip.service.mvh.Submission
import de.dnpm.mvh.submission.gens.SubmissionGenerators


class Tests extends AnyFlatSpec
with SubmissionGenerators[RDPatientRecord]
with BaseCompleters
{

  System.setProperty("dnpm.dip.site","UKT:Tübingen")

  implicit val rnd: Random = new Random(42)

  val records = List.fill(42)(Gen.of[Submission[RDPatientRecord]].next)


  "RDMappings" must "have worked" in {

    noException must be thrownBy records.map(_.mapTo[RDSubmission])

  }

}
