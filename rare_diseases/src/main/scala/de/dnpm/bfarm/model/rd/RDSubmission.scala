package de.dnpm.bfarm.model.rd


import de.dnpm.bfarm.model.base.{
  Metadata,
  Submission
}
import play.api.libs.json.{
  Json,
  OWrites
}

final case class RDSubmission
(
  metadata: Metadata,
  `case`: RDCase,
  molecular: Option[RDMolecular],
  plan: Option[RDPlan],
  followUp: Option[RDFollowUps]
)
extends Submission[
  RDCase,
  RDMolecular,
  RDPlan,
  RDFollowUps
]


object RDSubmission
{
  implicit val format: OWrites[RDSubmission] =
    Json.writes[RDSubmission]
}
