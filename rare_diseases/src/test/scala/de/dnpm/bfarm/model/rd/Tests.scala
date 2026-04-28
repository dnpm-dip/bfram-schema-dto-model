package de.dnpm.bfarm.model.rd


import de.dnpm.mvh.submission.test.BaseMappingTest
import de.dnpm.dip.service.mvh.Submission
import de.dnpm.dip.rd.model.RDPatientRecord
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.rd.gens.Generators._
import de.dnpm.bfarm.model.rd.RDMappings._


class Tests extends BaseMappingTest[RDPatientRecord,RDSubmission](
  "Rare Diseases",
  "https://raw.githubusercontent.com/BfArM-MVH/MVGenomseq_KDK/main/KDK/RareDiseases.json"
){

  override val submissions =
    LazyList.continually(Gen.of[Submission[RDPatientRecord]].next)
      .filter(_.record.diagnoses.forall(_.codes.size == 3))
      .take(42)

}
