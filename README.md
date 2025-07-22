# Beer Catalogue Application

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Running the application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Next steps](#next-steps)

## Overview
This is a simple traditional SpringBoot Restful API tasked with managing manufacturers and their beers. Giving users the ability to fetch beers and manufacturers in pages using sorting and filtering if needed.
For simplicity an in-memory database has been used using H2. The database is initialized with some dummy data so you can start trying endpoints once the app is deployed. This data is initialized in config/DataInitializer.java.
The app includes some basic authentication and distinguishes between admin and manufacturer roles. Although GET endpoints are public and accessible for everyone. The OpenApi documentation explains what is accessible for every role. This are the accounts created by DataInitializer:
```
    Admin {username: "admin", password: "adminpass"}
    Brewery1 {username: "brewery1", password: "brewpass"}
    Brewery2 {username: "brewery2", password: "brewpass2"}
```

---

## Features

- Manage manufacturers and beers
- Role-based authentication (admin, manufacturer)
- Integration and unit tests included
- Dockerized application
- Kubernetes deployment using Minikube

---

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker
- Minikube 
- kubectl 

---
## Running the application

#### Running Locally

1. Build the application:

   ```bash
   mvn clean package
   ```
2. Run the application

   ```bash
   mvn spring-boot:run
   ```
3. Run tests

	```bash
	mvn test
	```

#### Run from Docker

1. Build the image

   ```bash
   docker build -t beercatalogue .
   ```
   
2. Run it

   ```bash
   docker run -p 8080:8080 beercatalogue
   ```
   
#### Docker & Kubernetes Setup
To run the app on Minikube, you must build the Docker image inside Minikube's Docker daemon:

```bash
eval $(minikube -p minikube docker-env)
docker build -t beercatalogue:latest .
```

Then deploy to kubernetes:
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## API Endpoints

For more information about the API Endpoints remember that you can access the OpenAPI documentation once the app is running by opening "/swagger-ui/index.html" from your browser. For your convienience here are some curl commands to use one the API:

Fetching a page of manufacturers
   ```bash
    curl -X GET 'http://localhost:8080/api/manufacturers'
   ```

Fetching a page of beers sorted by abv and descending
   ```bash
    curl -X GET 'http://localhost:8080/api/beers?sortBy=abv&dir=desc'
   ```

Create a new manufacturer using the admin role
```bash
curl -X POST http://localhost:8080/api/manufacturers \
	-u admin:adminpass \
	-H "Content-Type: application/json" \
	-d '{"name":"BrewDog","country":"UK"}'    
```

Modify a the name and country of a manufacturer using that manufacturer's account
```bash
curl -v -X PUT http://localhost:8080/api/manufacturers/1 \
-u brewery1:brewpass \
-H "Content-Type: application/json" \
-d '{"name":"BrewDog 2","country":"Japan"}'  
```

Creat a new beer using a manufacturer's account
```bash
curl -v -X POST http://localhost:8080/api/beers \
	-u brewery1:brewpass \
	-H "Content-Type: application/json" \
	-d '{
	"name": "Punk MAX",
	"abv": 25,
	"type": "IPA",
	"description": "A hoppy, tropical IPA",
	"manufacturerId": 1
}'
```

## Next steps

#### Authentication
Currently the application uses a very basic form of authentication. Although Users are an entity from the database they can't be created, updated or destroyed. Also, said Users' roles should probably also be an entity to properly scale this project.
Using JWT for authentication would be a perfect next step enabling token based authentication.

#### Persistence
Using a persistent DB is the logical choice given the nature of this application. Something like PostgreSQL could be used and maybe deployed on the cloud.

#### Schemas
I've created diferent return schemas (Detail, Summary) just to showcase their use. It is probable that the Summary schemas should contain more info, but as the system scales they'll probable be needed.

#### Images
Images could be added to both Beers (promotional image of the beer for example) or Manufacturers (logos). 
This could be done (for simplicity) by directly adding a byte[] field for any Entity. But this would probably lead to performance and scalability problems on the long term. Using a Cloud Storage for said images is probably the most scalable and durable solution.
