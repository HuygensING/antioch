FROM java:8
ARG version
ENV wd=/home/alexandria
RUN useradd -m alexandria
USER alexandria
WORKDIR ${wd}
ADD target/alexandria-server-${version}.jar ${wd}/alexandria-server.jar
EXPOSE 2015
ENTRYPOINT java -jar alexandria-server.jar