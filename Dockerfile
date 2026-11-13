FROM amazoncorretto:21
LABEL maintainer="Guilherme Jr. <falecom@guilhermejr.net>"
ENV TZ=America/Bahia
ARG VAULT_HOST
ENV VAULT_HOST=${VAULT_HOST}
COPY sistema-autenticacao-service.jar sistema-autenticacao-service.jar
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -fsS http://localhost:9002/autenticacao-service/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/sistema-autenticacao-service.jar"]
EXPOSE 9002
