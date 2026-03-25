package de.dnpm.bfarm.model.onco


import de.dnpm.bfarm.model.base.{
  Metadata,
  Submission
}
import play.api.libs.json.{
  Json,
  OWrites
}

final case class OncologySubmission
(
  metadata: Metadata,
  `case`: OncologyCase,
  molecular: Option[OncologyMolecular],
  plan: Option[OncologyPlan],
  followUp: Option[OncologyFollowUps]
)
extends Submission[
  OncologyCase,
  OncologyMolecular,
  OncologyPlan,
  OncologyFollowUps
]


object OncologySubmission
{
  implicit val format: OWrites[OncologySubmission] =
    Json.writes[OncologySubmission]
}
