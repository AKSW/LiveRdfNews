export MAVEN_OPTS="-Xmx6000m"
mvn exec:java -Dexec.mainClass="org.aksw.simba.rdflivenews.index.RefactorIndex"
