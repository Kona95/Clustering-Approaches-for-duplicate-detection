//import AssemblyKeys._
//
//assemblySettings
//
//Common.baseSettings
ThisBuild / scalaVersion := "2.11.12"
val sparkVersion = "2.4.4"
ThisBuild / resolvers += Resolver.sonatypeRepo("public")
//spark-dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-catalyst_2.11" % sparkVersion , //"2.0.0"
  "org.apache.spark" %% "spark-core" % sparkVersion ,
  "org.apache.spark" %% "spark-mllib" % sparkVersion ,
  "org.apache.spark" %% "spark-sql" % sparkVersion )

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.2.0",
//  "org.apache.spark" % "spark-core_2.10" % "1.1.0" exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.commons" % "commons-lang3" % "3.3.2" % "provided",
  "org.apache.commons" % "commons-math3" % "3.3" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "io.spray" % "spray-json_2.10" % "1.2.6",
  "net.sf.trove4j" % "trove4j" % "3.0.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "com.google.guava" % "guava" % "18.0"
)

libraryDependencies += "com.databricks" %% "spark-avro" % "3.2.0"

assembly / test := {}
//
//run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
//
assembly / mainClass := Some("com.damirvandic.sparker.core.MainRunner")
//
//mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
//{
//  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
//  case x => old(x)
//}
//}
//assembly / assemblyMergeStrategy := {
//    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
//    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
//    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
//}

