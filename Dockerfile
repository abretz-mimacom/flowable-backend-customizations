FROM repo.flowable.com/docker/flowable/flowable-work:latest

USER root
COPY target/*.jar /additional-classpath/

USER flowable