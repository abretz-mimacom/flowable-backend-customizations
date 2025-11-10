FROM alpine as downloader
RUN apk add --no-cache wget unzip && \
    wget -O /tmp/microsoft.zip "https://go.microsoft.com/fwlink/?linkid=2338346" && \
    unzip /tmp/microsoft.zip -d /tmp/microsoft

FROM repo.flowable.com/docker/flowable/flowable-work:latest

USER root
COPY target/*.jar /additional-classpath/
COPY --from=downloader /tmp/microsoft/sqljdbc_13.2/enu/jars/mssql-jdbc-13.2.1.jre11.jar /additional-classpath/mssql-jdbc-13.2.1.jre11.jar

USER flowable