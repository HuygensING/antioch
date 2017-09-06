FROM java:8
ENV wd=/home/antioch
RUN useradd -m antioch
USER antioch
RUN mkdir -p /home/antioch/.antioch
WORKDIR ${wd}
ADD target/antioch-server.jar ${wd}/
EXPOSE 2015
ENTRYPOINT java -jar antioch-server.jar
