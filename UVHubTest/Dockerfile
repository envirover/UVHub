FROM openjdk:11-jre-slim

WORKDIR /usr/local/uvhub-test/

ADD ./target/uvhub-test-bin.tar.gz .

CMD java -cp "./conf:./lib/*" com.envirover.TestRunner