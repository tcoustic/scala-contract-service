# Scala 3 Microservice

A production-grade RESTful microservice built with Scala 3 and functional programming principles.

## Features & Architecture

* **Contract-First Design**: Endpoints are defined using Tapir as the single source of truth for routing and auto-generated API documentation.
* **Thread-Safe Concurrency**: Atomic, in-memory state management using Cats Effect `Ref` with zero race conditions.
* **Tested**: Integration tests using MUnit and `munit-cats-effect` without binding to external network ports.
* **CI/CD Built-In**: Automated test runner configured via GitHub Actions.

## Tech Stack

* **Language**: Scala 3
* **Effect System**: Cats Effect 3
* **HTTP Framework**: Http4s
* **API Specification**: Tapir
* **JSON Serialization**: Circe
* **Testing**: MUnit & munit-cats-effect

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Fetch a user by ID (returns 404 if missing) |
| `POST` | `/api/users` | Create a new user (server auto-generates ID) |
| `GET` | `/docs` | Interactive Swagger UI documentation |

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 17 or higher
* sbt (Scala Build Tool)

### Building the Project

Compile the application and fetch dependencies:

````bash
sbt compile
````

### Running Tests

Run the MUnit test suite locally:

````bash
sbt test
````

### Running the Application

Start the local server:

````bash
sbt run
````

Once running, navigate to `http://localhost:8080/docs` in your browser to view the interactive API documentation.

## Project Structure

````text
src/
├── main/
│   └── scala/
│       └── com/portfolio/app/
│           ├── Main.scala              # Application entry point
│           ├── api/
│           │   ├── UserEndpoints.scala # Tapir API contracts
│           │   └── UserRoutes.scala    # Endpoint handlers & Swagger integration
│           ├── domain/
│           │   └── User.scala          # Domain models and request/response types
│           └── repository/
│               └── UserRepo.scala      # Atomic in-memory data store
└── test/
    └── scala/
        └── com/portfolio/app/
            └── api/
                └── UserApiSpec.scala   # MUnit integration test suite
````

## Continuous Integration

GitHub Actions automatically runs `sbt test` on pushes to the `master` branch and incoming pull requests (`.github/workflows/ci.yml`).