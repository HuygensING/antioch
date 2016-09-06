FROM java:8
ENV wd=/home/alexandria

# build the fat jar
RUN java -version
RUN apt-get update && apt-get -y install maven

# install maven overrode the java settings, so set them back to java 8
RUN echo 2 | update-alternatives --config java

RUN java -version
ADD . /tmp/build
WORKDIR /tmp/build
RUN mvn package
RUN mkdir -p ${wd}
RUN mv /tmp/build/alexandria-server/target/alexandria-server-*.jar ${wd}/alexandria-server.jar
RUN rm -rf /tmp/build
#RUN apt-get --purge autoremove -y maven

# create the docker image
RUN useradd -m alexandria
RUN java -version
USER alexandria
WORKDIR ${wd}
EXPOSE 2015
ENTRYPOINT java -jar alexandria-server.jar
