# APFA Software
This contains all the Java code that is being used when running the programm. Java 1.9 is required. Software such as Eclipse or intellij will import the full code without problems.

Run the program by running nickrunner in the java program which is included in the 'full_program'.

Contents of this directory:

/src 		The source-files. 

/cls		The class-files.

/lib 		The libraries used. (this is excluding the external-libraries which are included inside the 'full_program'  )

/doc 		The Javadoc generated. 

/data		The data used.  

com.damirvandic.sparker.algorithms.Algorithms$BirchBasedClustering microTvs data/TVs-all-merged.json results ./results_birch/ -b 2 -p:T="rd 0.05 0.1 0.05" -p:alpha="rd 0 0.1 0.1" -p:beta="rd 0 0.1 0.1" -p:kappa="rd 0 1 1" -p:lambda="rd 0 1 1" -p:gamma="rd 0 0.1 0.1" -p:mu="rd 0 0.1 0.1" -p:brandHeuristic="b true false"


com.damirvandic.sparker.algorithms.Algorithms$BirchBasedClustering microTvs data/TVs-all-merged.json results ./results_birch/ -b 1 -ch 3 -p:T="rd 0.05 0.21 0.05" -p:alpha="rd 0.5 0.72 0.1" -p:beta="rd 0 0.21 0.1" -p:kappa="d 1" -p:lambda="d 1" -p:gamma="rd 0.6 0.91 0.1" -p:mu="rd 0.6 0.91 0.1" -p:brandHeuristic="b true false"