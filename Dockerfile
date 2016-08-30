FROM java:8
RUN useradd -m alexandria
USER alexandria
ADD alexandria-server/target/alexandria-server-1.2-SNAPSHOT.jar /alexandria/alexandria-server.jar
WORKDIR /alexandria
EXPOSE 2015
CMD java -jar alexandria-server.jar
