### Spring authorization server with Spring Cloud Gateway as a BFF

##### Implementation of [BFF pattern](https://www.ietf.org/archive/id/draft-ietf-oauth-browser-based-apps-15.html#name-backend-for-frontend-bff) for browser-based applications

#### Prerequisites

 - java 21 installed

#### To start all service
```
./build-and-run.sh
```
This will build all backend modules, create and run docker containers

#### Services

- auth-server --- Spring Authorization Server
- gateway --- Spring Cloud Gateway service acting as a BFF
- secure-resource --- Spring Resource Server

#### Databases

- oauth2-db --- MariaDB for storing Spring Authorization Server (registered clients, authorizations, consent screen)
- oauth2-session --- Redis DB for storing Spring Security Sessions --- UI available on http://localhost:8082
