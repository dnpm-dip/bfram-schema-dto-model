package de.dnpm.mvh.submission.gens


import java.time.LocalDate.{now => today}
import java.time.LocalDateTime.now
import de.dnpm.dip.model.{
  Id,
  PatientRecord
}
import de.dnpm.dip.service.mvh.{
  BroadConsent,
  Consent,
  ModelProjectConsent,
  Submission,
  TransferTAN
}
import de.ekut.tbi.generators.Gen
import play.api.libs.json.Json


trait SubmissionGenerators[T <: PatientRecord]
{

  private lazy val broadConsent =
    Json.fromJson[BroadConsent](
      Json.parse(getClass.getClassLoader.getResourceAsStream("consent.json"))
    )
    .get


  protected implicit def genDataUpload(
    implicit genRecord: Gen[T]
  ): Gen[Submission[T]] =
    for {

      ttan <- Gen.listOf(64, Gen.oneOf("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F")).map(_.mkString)

      record <- Gen.of[T]

      consentDate =
        record.getCarePlans
          .map(_.issuedOn)
          .minOption
          .map(_ minusWeeks 2)
          .getOrElse(today)

      hasBroadConsent <- Gen.booleans
        
      (bc,reasonConsentMissing) =
        if (hasBroadConsent) Some(List(broadConsent)) -> None
        else None -> Some(BroadConsent.ReasonMissing.OtherPatientReason)
        

      metadata =
        Submission.Metadata(
          Submission.Type.Test,
          Id[TransferTAN](ttan),
          ModelProjectConsent(
            "Patient Info TE Consent MVGenomSeq vers01",
            Some(consentDate minusDays 1),
            ModelProjectConsent.Purpose.values
              .toList
              .map(
                Consent.Provision(
                  consentDate,
                  _,
                  Consent.Provision.Type.Permit
                )
              )
          ),
          bc,
          reasonConsentMissing
        )

    } yield Submission(
      record,
      metadata,
      now
    )

}
