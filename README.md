LiveRdfNews
===========

Realtime generation of RDF news


# Install

1. `git clone git@github.com:AKSW/LiveRdfNews.git`
2. download https://github.com/gerbsen/maven-repo/raw/master/snapshots/com/github/gerbsen/utk/1.0.5/utk-1.0.5.jar
3. then run:
	```
	mvn install:install-file -DgroupId=com.github.gerbsen -DartifactId=utk -Dversion=1.0.5 -Dpackaging=jar -Dfile=utk-1.0.5.jar
	
	mvn -Dmaven.test.skip=true install
	```

