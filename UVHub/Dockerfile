FROM openjdk:13-jdk

WORKDIR /usr/local/uvhub/

ADD ./target/uvhub-bin.tar.gz .

EXPOSE 5080 5060 5760 5757

CMD java -cp "./conf:./lib/*" com.envirover.uvhub.UVHub