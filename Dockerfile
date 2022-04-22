FROM ubuntu:20.04
LABEL maintainer "Ammar Ammar <ammar257ammar@gmail.com>"

RUN apt-get update && \
	apt-get install -y wget unzip openjdk-8-jdk && \
	apt-get install -y ant && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;
	
# Fix certificate issues accodring to:
# https://bugs.launchpad.net/ubuntu/+source/ca-certificates-java/+bug/983302
RUN apt-get update && \
	apt-get install -y ca-certificates-java && \
	apt-get clean && \
	update-ca-certificates -f && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

WORKDIR /build

COPY build.xml .

COPY src/ ./src/

RUN wget "https://github.com/PathVisio/pathvisio/releases/download/v3.3.0/pathvisio-bundles-3.3.0.zip" -O lib.zip && \
	mkdir -p lib/ && \
	unzip lib.zip -d lib/ && \
	rm lib.zip

RUN ant

WORKDIR /app

RUN	cp -rf /build/dist/WPEnrichmentTool.jar /app/WPEnrichmentTool.jar && \
	cp -rf /build/dist/lib/ /app/lib/ && \
	rm -rf /build


ENTRYPOINT ["java","-jar","/app/WPEnrichmentTool.jar"]
CMD ["-h"]
