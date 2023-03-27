# baut auf dieser auf.
FROM openjdk:17-slim
# Sie laufen immer im root und wir fügen dann einen neuen benutzer hinzu. erste
RUN addgroup --system spring && adduser --ingroup spring --system spring
USER spring:spring
COPY target/*.jar app.jar
# welches programm wird gestartet. builden mit maven package. auch mit terminal im cmd "mvnw.cmd package" ausführen
ENTRYPOINT ["java","-jar", "app.jar"]