### Spring authorization server with Spring Cloud Gateway as a BFF

##### Implementation of [BFF pattern](https://www.ietf.org/archive/id/draft-ietf-oauth-browser-based-apps-15.html#name-backend-for-frontend-bff) for browser-based applications

#### Prerequisites

 - java 21 installed

#### To start all services
```
./build-and-run.sh
```
This will build all backend modules, create and run docker containers

If you would like to build and run **fe-client**
```
cd fe-client
cp .env.example .env.development
nvm use
yarn
yarn dev
```

#### Services

- **auth-server** --- Spring Authorization Server
- **gateway** --- Spring Cloud Gateway service acting as a BFF (Backend for Frontend)
- **secure-resource** --- Spring Resource Server
- **fe-client** --- React frontend application

- **leaked-passwords-api** - Service for checking if the user password is leaked
    - To start this service cloned it from this [Github repo](https://github.com/GoodbyePlanet/leaked-passwords-api).
    - Then:
```shell
cd leaked-passwords-api
docker build -t leaked-passwords-api .
docker run --env-file .env.development -e APP_ENV=development -e RUNNING_IN_DOCKER=true -p 8083:8083 --network spring-cg-bff_default --name leaked-passwords-api leaked-passwords-api
```

- **passkeys-service** - Service for WebAuthn passwordless authentication
  - To start this service cloned it from this [Github repo](https://github.com/GoodbyePlanet/passkey-service).
  - Then:
```shell
cd passkey-service
make docker-build
make up
docker network connect spring-cg-bff_default passkeys-service # connect to network of apps started by docker-compose from this repository
```

#### Databases

- oauth2-db --- MariaDB for storing Spring Authorization Server (registered clients, authorizations, consent screen)
  - UI available on http://localhost:8080/?server=oauth_db&username=oauth&db=oauth_db
- oauth2-session --- Redis DB for storing Spring Security Sessions
  - UI available on http://localhost:8082

#### Observability

- To start docker containers related to observability
```
cd observability
docker-compose up
```

Grafana UI is present on http://localhost:3000/
Loki is at http://loki:3100
