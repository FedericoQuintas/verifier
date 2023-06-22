From openjdk:17

copy ./target/verifier-0.0.1.jar verifier-0.0.1.jar

ENTRYPOINT ["java","-jar","verifier-0.0.1.jar"]
