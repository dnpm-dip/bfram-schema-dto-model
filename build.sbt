import scala.util.Properties.envOrElse


name := "dnpm-bfarm-model"  // Central Clinical Data Node
ThisBuild / organization := "de.dnpm"
ThisBuild / scalaVersion := "2.13.18"
//ThisBuild / version      := envOrElse("VERSION","1.0.0")

val ownerRepo  = envOrElse("REPOSITORY","dnpm-dip/bfram-schema-dto-model").split("/")
ThisBuild / githubOwner      := ownerRepo(0)
ThisBuild / githubRepository := ownerRepo(1)


//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    publish / skip := true
  )
  .aggregate(
    base,
    generators,
    oncology,
    rare_diseases
  )


lazy val base = project
  .settings(
    name := "dnpm-bfarm-model-base",
    version := envOrElse("BASE_MODEL_VERSION","1.0.0-SNAPSHOT"),
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.service_base
    )
  )

lazy val generators = project
  .settings(
    name := "dnpm-mvh-submission-generators",
    settings,
    libraryDependencies ++= Seq(
      dependencies.service_base,
      dependencies.generators
    ),
    // Only required for tests, so no need to publish/release
    publish / skip := true
  )

lazy val oncology = project
  .settings(
    name := "dnpm-bfarm-model-oncology",
    version := envOrElse("ONCOLOGY_MODEL_VERSION","1.0.0-SNAPSHOT"),
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.mtb_dtos,
      dependencies.mtb_generators,
      dependencies.icd10gm,
      dependencies.icdo3,
      dependencies.icd_catalogs,
      dependencies.atc_impl,
      dependencies.atc_catalogs,
      dependencies.hgnc_geneset
    )
  )
  .dependsOn(
    base,
    generators % Test
  )

lazy val rare_diseases = project
  .settings(
    name := "dnpm-bfarm-model-rare-disease",
    version := envOrElse("RD_MODEL_VERSION","1.0.0-SNAPSHOT"),
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.rd_dtos,
      dependencies.rd_generators,
      dependencies.icd10gm,
      dependencies.icdo3,
      dependencies.icd_catalogs,
      dependencies.atc_impl,
      dependencies.atc_catalogs,
      dependencies.hgnc_geneset,
      dependencies.alpha_id_se,
      dependencies.hpo,
      dependencies.orphanet
    )
  )
  .dependsOn(
    base,
    generators % Test
  )


//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val service_base   = "de.dnpm.dip"   %% "service-base"          % "1.2.0"
    val mtb_dtos       = "de.dnpm.dip"   %% "mtb-dto-model"         % "1.1.2"
    val rd_dtos        = "de.dnpm.dip"   %% "rd-dto-model"          % "1.1.2"
    val scalatest      = "org.scalatest" %% "scalatest"             % "3.2.18" % Test
    val generators     = "de.ekut.tbi"   %% "generators"            % "1.0.0"
    val mtb_generators = "de.dnpm.dip"   %% "mtb-dto-generators"    % "1.1.2" % Test
    val rd_generators  = "de.dnpm.dip"   %% "rd-dto-generators"     % "1.1.2" % Test
    val icd10gm        = "de.dnpm.dip"   %% "icd10gm-impl"          % "1.1.2" % Test
    val icdo3          = "de.dnpm.dip"   %% "icdo3-impl"            % "1.1.2" % Test
    val icd_catalogs   = "de.dnpm.dip"   %% "icd-claml-packaged"    % "1.1.2" % Test
    val atc_impl       = "de.dnpm.dip"   %% "atc-impl"              % "1.1.0" % Test
    val atc_catalogs   = "de.dnpm.dip"   %% "atc-catalogs-packaged" % "1.1.0" % Test
    val hgnc_geneset   = "de.dnpm.dip"   %% "hgnc-gene-set-impl"    % "1.1.1" % Test
    val hpo            = "de.dnpm.dip"   %% "hp-ontology"           % "1.1.2"
    val alpha_id_se    = "de.dnpm.dip"   %% "alpha-id-se"           % "1.1.2" % Test
    val orphanet       = "de.dnpm.dip"   %% "orphanet-ordo"         % "1.1.2" % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings

// Compiler options from: https://alexn.org/blog/2020/05/26/scala-fatal-warnings/
lazy val compilerOptions = Seq(
  // Feature options
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ymacro-annotations",

  // Warnings as errors!
  "-Xfatal-warnings",

  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wunused:implicits",
  "-Wvalue-discard",
)


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.githubPackages("dnpm-dip"),
    Resolver.githubPackages("KohlbacherLab"),
    Resolver.sonatypeCentralSnapshots
  )

)

