FROM openjdk:11-jdk-slim

WORKDIR /usr/local/uvhub-test/

ADD ./target/uvhub-test-bin.tar.gz .

CMD java -cp "./conf:./lib/*" com.envirover.TestRunner