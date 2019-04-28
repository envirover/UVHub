FROM openjdk:8-jre

WORKDIR /usr/local/uvhub/

ADD ./target/uvhub-bin.tar.gz .

EXPOSE 8080 5060 5760 5757 8000

CMD java -cp "./conf:./lib/*" com.envirover.uvhub.UVHub