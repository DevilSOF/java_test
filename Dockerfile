FROM openjdk:14-jdk-slim
RUN mkdir /myapp
RUN apt-get update -qq && apt-get install -y unzip && apt-get install -y curl
RUN curl -sL https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar --output ./myapp/junit-4.12.jar
RUN curl -sL https://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar --output ./myapp/hamcrest-core-1.3.jar
RUN curl -sL http://mirror.linux-ia64.org/apache//commons/io/binaries/commons-io-2.6-bin.zip --output commons-io-2.6-bin.zip
RUN unzip -o commons-io-2.6-bin.zip
RUN cp -f ./commons-io-2.6/commons-io-2.6.jar ./myapp/commons-io-2.6.jar
WORKDIR /myapp
COPY . /myapp
RUN javac -cp .:hamcrest-core-1.3.jar:junit-4.12.jar:commons-io-2.6.jar TestData.java
CMD ["java", "-cp", ".:hamcrest-core-1.3.jar:junit-4.12.jar:commons-io-2.6.jar", "org.junit.runner.JUnitCore", "TestData"]
