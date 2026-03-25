package de.dnpm.bfarm.model.base


import scala.util.chaining._
import java.time.YearMonth
import de.dnpm.dip.model
import de.dnpm.dip.model.{
  CarePlan,
  ExternalId,
  Id,
  NGSReport,
  PatientRecord,
  Site
}
import de.dnpm.dip.service.mvh
import de.dnpm.dip.service.mvh.{
  Submission,
  UseCase
}
import de.dnpm.dip.util.mapping.syntax._
import play.api.libs.json.Json


trait Mappings[RecordType <: PatientRecord,UseCaseSubmission <: de.dnpm.bfarm.model.base.Submission[_,_,_,_]]
{

  val config: Config

  val useCase: UseCase.Value


  protected implicit def stringToId[T](id: String): Id[T] =
    Id(id)

  protected implicit def extIdToId[T,U](id: ExternalId[T,U]): Id[T] =
    id.value

  protected implicit def idToOther[T,U](id: Id[T]): Id[U] =
    id.asInstanceOf[Id[U]]


  protected implicit val useCaseMapping: UseCase.Value => Metadata.DiseaseType.Value =
    Map(
      UseCase.MTB -> Metadata.DiseaseType.Oncological,
      UseCase.RD  -> Metadata.DiseaseType.Rare
    )

  protected implicit val chromosomeMapping: model.Chromosome.Value => Chromosome.Value =
    _.toString.replace("chr","")
     .pipe(Chromosome.withName)


  protected implicit val sequencingType: NGSReport.Type.Value => LibraryType.Value =
    Map(
      NGSReport.Type.GenomeLongRead  -> LibraryType.WGSLr,
      NGSReport.Type.GenomeShortRead -> LibraryType.WGS,
      NGSReport.Type.Exome           -> LibraryType.WES, 
      NGSReport.Type.Panel           -> LibraryType.Panel
    )
    .orElse {
      case _ => LibraryType.None
    }


  protected implicit val nonInclusionReasonMapping: CarePlan.NoSequencingPerformedReason.Value => Metadata.RejectionJustification.Value = {
    import CarePlan.NoSequencingPerformedReason._

    Map(
      TargetedDiagnosticsRecommended -> Metadata.RejectionJustification.TargetDiagnosisRecommended,
      Pyschosomatic                  -> Metadata.RejectionJustification.ProbablyPsychosomaticIllness,
      NotRareDisease                 -> Metadata.RejectionJustification.ProbablyCommonDisease,
      NonGeneticCause                -> Metadata.RejectionJustification.ProbablyNotGeneticCause,
      Other                          -> Metadata.RejectionJustification.OtherReason
    )
  }


  import mvh.extensions._

  protected def performedSequencingType(record: RecordType): LibraryType.Value =
    record.mvhSequencingReports
      .maxByOption(_.issuedOn)
      .map(_.`type`.code.enumValue.mapTo[LibraryType.Value])
      .getOrElse(LibraryType.None)


  protected implicit val metadataMapping: Submission[RecordType] => Metadata = {

    case Submission(record,metadata,datetime) =>
    
      import Metadata._


      implicit val consentProvisionType: mvh.Consent.Provision.Type.Value => MVConsent.Scope.Type.Value =
        Map(
          mvh.Consent.Provision.Type.Permit -> MVConsent.Scope.Type.Permit,
          mvh.Consent.Provision.Type.Deny   -> MVConsent.Scope.Type.Deny  
        )     

      implicit val consentPurpose: mvh.ModelProjectConsent.Purpose.Value => MVConsent.Scope.Domain.Value =
        Map(
          mvh.ModelProjectConsent.Purpose.Sequencing         -> MVConsent.Scope.Domain.MvSequencing,
          mvh.ModelProjectConsent.Purpose.Reidentification   -> MVConsent.Scope.Domain.ReIdentification,
          mvh.ModelProjectConsent.Purpose.CaseIdentification -> MVConsent.Scope.Domain.CaseIdentification
        )

/*
      implicit val reasonBroadConsentMissing: BroadConsent.ReasonMissing.Value => Metadata.ResearchConsent.NoScopeJustification.Value = 
        Map(
          BroadConsent.ReasonMissing.PatientInability     -> Metadata.ResearchConsent.NoScopeJustification.PatientUnable,
          BroadConsent.ReasonMissing.PatientRefusal       -> Metadata.ResearchConsent.NoScopeJustification.PatientRefuses,
          BroadConsent.ReasonMissing.NonReturnedConsent   -> Metadata.ResearchConsent.NoScopeJustification.NotReturned,
          BroadConsent.ReasonMissing.OtherPatientReason   -> Metadata.ResearchConsent.NoScopeJustification.OtherPatientReason,
          BroadConsent.ReasonMissing.TechnicalIssues      -> Metadata.ResearchConsent.NoScopeJustification.TechnicalReason,
          BroadConsent.ReasonMissing.OrganizationalIssues -> Metadata.ResearchConsent.NoScopeJustification.OrganizationalIssues
        )
*/


    val mvhCarePlan = record.mvhCarePlan.get  // .get safe here, because validation ensures non-empty careplan list for MVH submissions 

    val site = record.patient.managingSite.getOrElse(Site.local).code

    Metadata(
      Metadata.Submission(
        datetime.toLocalDate,
        metadata.`type`,
        config.submitterId(site),
        config.dataNodeIds(useCase),
        Option.when(performedSequencingType(record) != LibraryType.None)(
          config.gdcId(site)
        ),
        useCase.mapTo[DiseaseType.Value]
      ),
      record.patient.healthInsurance.`type`.code,
      Metadata.MVConsent(
        metadata.modelProjectConsent.date,
        metadata.modelProjectConsent.version,
        metadata.modelProjectConsent.provisions.map(
          p => MVConsent.Scope(
            p.date,
            p.purpose.mapTo[MVConsent.Scope.Domain.Value],
            p.`type`.mapTo[MVConsent.Scope.Type.Value]
          )
        )
      ),
      metadata.reasonResearchConsentMissing match {
        case None =>
          metadata.researchConsents.map(
            _.map(
              consent => ResearchConsent(
                "2025.0.1",
                consent.date,
                Some(Json.toJson(consent)),
                None
              )
            )
          )
          .getOrElse(List.empty)
        case Some(_) => List.empty
      },
      metadata.transferTAN,
      None,  // Don't transmit Patient.id (for now)
      record.patient.gender.code,
      YearMonth.from(record.patient.birthDate),
      record.patient.address.map(_.municipalityCode.value).getOrElse(""),
      mvhCarePlan.noSequencingPerformedReason.isEmpty,
      mvhCarePlan.issuedOn,
      mvhCarePlan.noSequencingPerformedReason.map(_.code.enumValue.mapTo[RejectionJustification.Value]),
    )
  }


  implicit val vitalStatus: model.VitalStatus.Value => VitalStatus.Value =
    Map(
      model.VitalStatus.Alive    -> VitalStatus.Living,
      model.VitalStatus.Deceased -> VitalStatus.Deceased
    )


  implicit val useCaseSubmission: Submission[RecordType] => UseCaseSubmission

}
