FROM openjdk:11
LABEL maintainer="Guilherme Jr. <falecom@guilhermejr.net>"
ENV TZ=America/Bahia
ARG VAULT_HOST
ARG VAULT_TOKEN
ENV VAULT_HOST=${VAULT_HOST}
ENV VAULT_TOKEN=${VAULT_TOKEN}
COPY sistema-autenticacao-service.jar sistema-autenticacao-service.jar
ENTRYPOINT ["java","-jar","/sistema-autenticacao-service.jar"]
EXPOSE 9002