# Clustering Approaches for duplicate detection

Product duplicate detection is an important problem on the Web, as many information aggregators need to deal with this issue. In this work we investigate the impact of various clustering algorithms on a state-of-the-art product duplicate detection method, i.e., MSM. We compare the existing adapted single linkage alogrithm with three high-performance algorithm from the literature: CURE, BIRCH and CLARANS. Using a custom dataset of 1624 television descriptions the best performance in terms of F1 is obtained by the single-linkage algorithm but this result is not statistically significant  when compared to CURE. CURE appears on as interesting alternative to adapted single-linkage as it is more robust.

### Building the project
In order to build the project, u have to execute the command

```sbt clean assembly```

inside the APFA_software file. This will generate the executable file (apfa_software_2.11-0.1.0-SNAPSHOT.jarapfa_software_2.11-0.1.0-SNAPSHOT.jar) 
in target/scala-2.11 folder.

### Running the code

```spark-submit apfa_software_2.11-0.1.0-SNAPSHOT.jarapfa_software_2.11-0.1.0-SNAPSHOT.jar (algorithm name) and rest of parameters```

The name of the implemented algorithms are:
* CURE "com.damirvandic.sparker.algorithms.Algorithms$CureBasedClustering"
* BIRCH "com.damirvandic.sparker.algorithms.Algorithms$BirchBasedClustering"
* CLARANS "com.damirvandic.sparker.algorithms.Algorithms$ClaransBasedClustering"

Example of execution of BIRCH using the parameters for MSM:

```
com.damirvandic.sparker.algorithms.Algorithms$BirchBasedClustering
100TVS
data/TVs-100.json
results_birch
./results_birch/
-b
50
-ch
10
-sp
16
-p:B="ri 100 1000 100"
-p:T="rd 0.05 0.51 0.05"
-p:alpha="rd 0 1 0.1"
-p:beta="rd 0 1 0.1 "
-p:kappa="rd 0 10 1"
-p:lambda="rd 0 10 1"
-p:gamma="rd 0 1 0.1"
-p:mu="rd 0 1 0.1"
-p:brandHeuristic="b true false"
```

