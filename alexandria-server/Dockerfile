FROM java:8
ARG version
RUN useradd -m alexandria
USER alexandria
ADD target/alexandria-server-${version}.jar /alexandria/alexandria-server.jar
WORKDIR /alexandria
EXPOSE 2015
CMD java -jar alexandria-server.jar
