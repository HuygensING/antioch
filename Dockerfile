FROM maven:3.3-jdk-8
ENV wd=/home/antioch

ADD . /tmp/build
WORKDIR /tmp/build
# build the fat jar
RUN mvn package
RUN mkdir -p ${wd}
RUN mv /tmp/build/antioch-server/target/antioch-server-*.jar ${wd}/antioch-server.jar
RUN rm -rf /tmp/build
#RUN apt-get --purge autoremove -y maven

# create the docker image
RUN useradd -m antioch
USER antioch
WORKDIR ${wd}
EXPOSE 2015
ENTRYPOINT java -jar antioch-server.jar
