package de.dnpm.bfarm.model.base


trait Submission[Case,MolSeq,Plan,FU]
{
  val metaData: Metadata
  val `case`: Case
  val molecular: Option[MolSeq]
  val plan: Option[Plan]
  val followUp: Option[FU]
}
