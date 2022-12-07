## secret-santa app

Simple app for matching a bunch of friends so that everyone gets 1 gift, preferably from someone outside their group[^1].

## run

### application-secret.yml

You need to configure mail server parameters in `application-secret.yml` config file. An example below:

```yaml
spring:
  mail:
    host: ssl0.ovh.net
    port: 587
    username: me@example.com
    password: "secret"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.debug: true
      mail.smtp.socketFactory.port: 465
      mail.smtp.socketFactory.class: javax.net.ssl.SSLSocketFactory
```

```shell
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## development

```shell
# run all tests (requires application-secret.yml)
./gradlew test
```

[^1]: A group in the context of that app are for example spouses or siblings.
