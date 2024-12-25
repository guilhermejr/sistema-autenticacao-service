# syntax=docker/dockerfile:1
# check=skip=SecretsUsedInArgOrEnv
FROM openjdk:21
LABEL maintainer="Guilherme Jr. <falecom@guilhermejr.net>"
ENV TZ=America/Bahia
ARG VAULT_HOST
ARG VAULT_TOKEN
ARG CONFIG_SERVER_USER
ARG CONFIG_SERVER_PASS
ENV VAULT_HOST=${VAULT_HOST}
ENV VAULT_TOKEN=${VAULT_TOKEN}
ENV CONFIG_SERVER_USER=${CONFIG_SERVER_USER}
ENV CONFIG_SERVER_PASS=${CONFIG_SERVER_PASS}
COPY sistema-autenticacao-service.jar sistema-autenticacao-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/sistema-autenticacao-service.jar"]
EXPOSE 9002
