See the [generated Maven site](http://huygensING.github.io/alexandria) for documentation on Alexandria.
------

##### (cheat-sheet to publish the site):

###### Dry-run of publishing the site to github.io via the `gh-pages` branch of the project:
	mvn scm-publish:publish-scm -Dscmpublish.dryRun=true

###### Generating the site and publishing it:
	mvn clean verify site:site site:stage scm-publish:publish-scm

###### Generating a single-jar version of the server and running it:
	mvn package -am -pl alexandria-server
	java -jar alexandria-server/target/alexandria-server-${version}.jar
