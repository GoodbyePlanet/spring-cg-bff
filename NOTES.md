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
