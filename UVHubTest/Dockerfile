FROM openjdk:8-jre

WORKDIR /usr/local/uvhub-test/

ADD ./target/uvhub-test-bin.tar.gz .

CMD java -cp "./conf:./lib/*" com.envirover.uvhub.TestRunner