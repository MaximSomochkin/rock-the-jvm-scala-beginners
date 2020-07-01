name := "essential-slick-chapter-01"

version := "3.3"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "3.3.0",
  "com.h2database"      % "h2"              % "1.4.197",
  "ch.qos.logback"      % "logback-classic" % "1.2.3"
)

initialCommands in console := """
  |import slick.jdbc.H2Profile.api._
  |import Example._
  |import scala.concurrent.duration._
  |import scala.concurrent.Await
  |import scala.concurrent.ExecutionContext.Implicits.global
  |val db = Database.forConfig("chapter01")
  |def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 2 seconds)
  |exec(messages.schema.create)
  |exec(messages ++= freshTestData)
""".trim.stripMargin

//
//libraryDependencies += "org.postgresql" % "postgresql" % "42.2.10"
//libraryDependencies += "com.github.tminglei" %% "slick-pg_core" % "0.18.1"
//
//import com.permutive.sbtliquibase.SbtLiquibase
//enablePlugins(SbtLiquibase)
//liquibaseUsername := "postgres"
//liquibasePassword := "11111111"
//liquibaseDriver   := "org.postgresql.Driver"
//liquibaseUrl      := "jdbc:postgresql://localhost:5432/dino"
