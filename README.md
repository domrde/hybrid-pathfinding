# hybrid-pathfinding
Demo of using SVM to smooth A\* generated path.

Usage:
- Generate js from scala using ScalaJS 

```sbt client/fastOptJS```

- Copy generated js file to server resources

```cp client/target/scala-2.12/client-fastopt.js server/src/main/resources/app```

- Run the server

```sbt server/run```

- Open page in browser on ```localhost:8080```.

---
Uses LIBSVM library from https://www.csie.ntu.edu.tw/~cjlin/libsvm/
