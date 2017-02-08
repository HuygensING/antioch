FROM maven:3.3-jdk-8
ENV wd=/home/alexandria

ADD . /tmp/build
WORKDIR /tmp/build
# build the fat jar
RUN mvn package
RUN mkdir -p ${wd}
RUN mv /tmp/build/alexandria-server/target/alexandria-server-*.jar ${wd}/alexandria-server.jar
RUN rm -rf /tmp/build
#RUN apt-get --purge autoremove -y maven

# create the docker image
RUN useradd -m alexandria
USER alexandria
WORKDIR ${wd}
EXPOSE 2015
ENTRYPOINT java -jar alexandria-server.jar
