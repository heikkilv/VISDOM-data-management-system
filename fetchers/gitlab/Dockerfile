FROM hseeberger/scala-sbt:8u282_1.5.1_2.12.13

RUN mkdir -p /gitlab-fetcher
WORKDIR /gitlab-fetcher
COPY / /gitlab-fetcher/

RUN sbt stage

CMD /gitlab-fetcher/target/universal/stage/bin/gitlab-data-fetcher
