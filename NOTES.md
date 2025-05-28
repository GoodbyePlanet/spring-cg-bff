### Example of using postgres and [UAA](https://docs.cloudfoundry.org/concepts/architecture/uaa.html)
```
postgres:
    image: postgres:latest
    container_name: postgresDB
    environment:
      POSTGRES_PASSWORD: mysecretpassword
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  uaa:
    image: cloudfoundry/uaa
    container_name: uaa
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      UAA_CONFIG_URL: file:/uaa/uaa.yml
    volumes:
      - ./uaa.yml:/uaa/uaa.yml
    networks:
      - default
```

### Example of setting oauth2 client using configuration properties
```
  security:
    oauth2:
      client:
        registration:
          gateway:
            provider: auth-server
            client-id: gateway
            client-secret: secret
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid,profile,resource.read
        provider:
          auth-server:
            issuer-uri: http://auth-server:9000
  cloud:
    gateway:
      routes:
        - id: resource
          uri: http://resource:9000
          predicates:
            - Path=/resource
          filters:
            - TokenRelay=
            - RemoveRequestHeader=Cookie
```
