FROM repo.flowable.com/docker/flowable/flowable-work:latest

USER root
COPY target/*.jar /additional-classpath/

RUN wget -O sqlserver-bundle.zip https://go.microsoft.com/fwlink/?linkid=2338346 || exit 0
RUN unzip sqlserver-bundle.zip || exit 0

COPY sqlserver-bundle/enu/jars/mssql-jdbc-13.2.1.jre11.jar /additional-classpath/ || exit 0

USER flowable