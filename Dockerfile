FROM amazoncorretto:17
LABEL org.opencontainers.image.source https://github.com/craftmaster2190/rootstech-classes
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
