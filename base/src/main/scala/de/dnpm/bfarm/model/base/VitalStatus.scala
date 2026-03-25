package de.dnpm.bfarm.model.base


import play.api.libs.json.{
  Json,
  Format
}

object VitalStatus extends Enumeration
{
  val Living   = Value("living")
  val Deceased = Value("deceased")

  implicit val format: Format[Value] =
    Json.formatEnum(this)
}
