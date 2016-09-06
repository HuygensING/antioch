See the [generated Maven site](http://huygensING.github.io/alexandria) for documentation on Alexandria.
------

##### (cheat-sheet to publish the site):

###### Dry-run of publishing the site to github.io via the `gh-pages` branch of the project:
	mvn scm-publish:publish-scm -Dscmpublish.dryRun=true

###### Generating the site and publishing it:
    mvn clean verify site:site site:stage scm-publish:publish-scm

###### Alternatively, use the docker image:
    docker run -v ${workdir}:/home/alexandria/.alexandria -p${native_port}:2015 huygensing/alexandria

where workdir is a local directory to persist the db on, and native_port is the port you want the server to be accessed on.

`huygensing/alexandria` is the image of latest release
for the development version, use `huygensing/alexandria:develop` 