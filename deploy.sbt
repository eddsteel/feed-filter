enablePlugins(JavaAppPackaging)
maintainer in Docker := "Edd Steel <edward.steel@gmail.com>"
packageName in Docker := "eddsteel/feed-filter"
packageSummary in Docker := "A feed proxying service"
packageDescription := "A feed proxying service"
dockerExposedPorts := List(8080)
dockerBaseImage := "frolvlad/alpine-scala"
dockerUpdateLatest := true
