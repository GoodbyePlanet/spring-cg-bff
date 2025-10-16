### Spring authorization server with Spring Cloud Gateway as a BFF

##### Implementation of [BFF pattern](https://www.ietf.org/archive/id/draft-ietf-oauth-browser-based-apps-15.html#name-backend-for-frontend-bff) for browser-based applications

#### Prerequisites

 - java 21 installed

Update **/etc/hosts**
```
127.0.0.1 auth-server
127.0.0.1 secure-resource
```
This is required when running services via docker containers, and until I figure out how to make it running
without changing **hosts** file

#### To start all service
```
./build-and-run.sh
```
This will build all backend modules, create and run docker containers

Build and run **fe-client**
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
