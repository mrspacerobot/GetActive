# GetActive

## Project Overview

GetActive is a Spring Boot application designed to help users find and join sports activities in their area. The application allows users to create, search, and join various sports activities, providing a platform for people to stay active and connect with others who share similar interests.

## Technologies Used

- **Spring Boot**: Framework for building Java-based applications.
- **Hibernate**: ORM framework for database interactions.
- **PostgreSQL**: Relational database management system.
- **Docker**: Containerization platform.
- **Gradle**: Build automation tool.
- **Flyway**: Database migration tool.
- **JUnit**: Testing framework.
- **Spotless**: Code formatting tool.

## Setup Instructions

### Prerequisites

- Java 21
- Docker
- Docker Compose
- PostgreSQL

### Cloning the Repository

```bash
git clone https://github.com/mrspacerobot/GetActive.git
cd GetActive
```

### Running the Application

1. Build the application using Gradle:

```bash
./gradlew build
```

2. Start the application using Docker Compose:

```bash
docker-compose up --build -d
```

### Stopping the Application

To stop the application, run:

```bash
docker-compose down
```

## Database Configuration

The application uses PostgreSQL as the database. Flyway is used for database migrations. The necessary environment variables for database configuration are set in the `docker-compose.yml` file.

## API Endpoints

The main API endpoints are defined in `src/main/java/com/sportsync/activity/ActivityController.java`. Here are some of the key endpoints:

- `POST /api/activities`: Create a new activity.
- `GET /api/activities`: Get a list of activities with optional filtering.
- `POST /api/activities/filter`: Filter activities based on various criteria.
- `POST /api/activities/{activityId}/join`: Join an activity.

## Testing

The project uses JUnit for testing. To run the tests, use the following command:

```bash
./gradlew test
```

## Code Formatting

The project uses Spotless for code formatting. To apply the formatting rules, run:

```bash
./gradlew spotlessApply
```

## Contributing

We welcome contributions to the project. To contribute, please follow these guidelines:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Make your changes and commit them with descriptive commit messages.
4. Push your changes to your forked repository.
5. Create a pull request to the main repository.

## License

This project is licensed under the MIT License.
