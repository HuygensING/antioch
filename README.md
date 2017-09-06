See the [generated Maven site](http://huygensING.github.io/antioch) for documentation on Antioch.
------

##### (cheat-sheet to publish the site):

###### Dry-run of publishing the site to github.io via the `gh-pages` branch of the project:
	mvn scm-publish:publish-scm -Dscmpublish.dryRun=true

###### Generating the site and publishing it:
    mvn clean verify site:site site:stage scm-publish:publish-scm

###### Alternatively, use the docker image:
    docker run -v ${workdir}:/home/alexandria/.alexandria -p${native_port}:2015 huygensing/alexandria-server:latest-develop

where workdir is a local directory to persist the db on, and native_port is the port you want the server to be accessed on.
 