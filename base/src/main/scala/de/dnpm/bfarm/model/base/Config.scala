package de.dnpm.bfarm.model.base


import scala.util.chaining._
import play.api.libs.json.{
  Json,
  Reads
}
import de.dnpm.dip.coding.Code
import de.dnpm.dip.model.{
  Id,
  Site
}
import de.dnpm.dip.util.Logging
import de.dnpm.dip.service.mvh.UseCase


final case class Config
(
  dataNodeIds: Map[UseCase.Value,Id[CDN]],
  sites: Map[Code[Site],Config.SiteInfo]
){

  def submitterId(site: Code[Site]): Id[Site] =
    sites(site).submitterId

  def gdcId(site: Code[Site]): Id[GDC] =
    sites(site).gdcId
}


object Config extends Logging
{

  final case class SiteInfo
  (
    submitterId: Id[Site],
    gdcId: Id[GDC]
  )


  implicit val readsSiteInfo: Reads[SiteInfo] =
    Json.reads[SiteInfo]

  implicit val reads: Reads[Config] =
    Json.reads[Config]

  lazy val instance: Config =
    Option( 
      getClass.getClassLoader.getResourceAsStream("config.json")
    )
    .map(
      Json.parse(_) pipe (
        Json.fromJson[Config](_)
          .fold(
            errs => {
              log.error(errs.toString)
              throw new Exception(errs.toString)
            },
            identity
          )
      )
    )
    .get

}
