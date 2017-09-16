# hybrid-pathfinding
Demo of using SVM to smooth A\* generated path.

To generate js from ScalaJS use ```sbt client/fastOptJS```, then put generated app to server's resources ```cp client/target/scala-2.12/client-fastopt.js server/src/main/resources/app```.