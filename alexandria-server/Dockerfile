FROM java:8
ENV wd=/home/alexandria
RUN useradd -m alexandria
USER alexandria
RUN mkdir -p /home/alexandria/.alexandria
WORKDIR ${wd}
ADD target/alexandria-server.jar ${wd}/
EXPOSE 2015
ENTRYPOINT java -jar alexandria-server.jar
