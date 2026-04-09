# Jahnavi API

A production-ready Spring Boot REST API designed for Angular application integration.

## Features

- RESTful API with full CRUD operations
- CORS configuration for Angular (default: `http://localhost:4200`)
- Pagination, sorting, and search support
- Input validation with detailed error responses
- OpenAPI/Swagger documentation
- JaCoCo code coverage with 80% minimum threshold
- SonarQube integration for code quality
- Docker multi-stage build
- CI/CD pipelines (GitHub Actions + Jenkins)
- H2 (dev) and PostgreSQL (prod) database support
- Spring Boot Actuator health endpoints

## Tech Stack

- **Java 17** + **Spring Boot 3.2.5**
- **Spring Data JPA** with H2/PostgreSQL
- **SpringDoc OpenAPI** for API documentation
- **JaCoCo** for code coverage
- **SonarQube** for static analysis
- **Docker** for containerization

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (optional)

### Run Locally

```bash
# Build the project
mvn clean install

# Run the application (dev profile with H2)
mvn spring-boot:run

# Run with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Run Tests

```bash
# Unit tests
mvn test

# Unit + Integration tests
mvn verify

# Generate coverage report
mvn verify jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Docker

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build manually
docker build -t jahnavi-api .
docker run -p 8080:8080 jahnavi-api
```

## API Endpoints

| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| GET    | `/api/v1/items`                 | Get all items (paginated)|
| GET    | `/api/v1/items/{id}`            | Get item by ID           |
| POST   | `/api/v1/items`                 | Create a new item        |
| PUT    | `/api/v1/items/{id}`            | Update an item           |
| DELETE | `/api/v1/items/{id}`            | Delete an item           |
| GET    | `/api/v1/items/status/{status}` | Filter by status         |
| GET    | `/api/v1/items/search?keyword=` | Search items             |
| GET    | `/api/v1/health`                | Health check             |

### Swagger UI

Access API documentation at: `http://localhost:8080/swagger-ui.html`

## Angular Integration

Configure your Angular `environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

Example Angular service:

```typescript
@Injectable({ providedIn: 'root' })
export class ItemService {
  private apiUrl = environment.apiUrl + '/items';

  constructor(private http: HttpClient) {}

  getItems(page = 0, size = 10) {
    return this.http.get(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getItem(id: number) {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createItem(item: any) {
    return this.http.post(this.apiUrl, item);
  }

  updateItem(id: number, item: any) {
    return this.http.put(`${this.apiUrl}/${id}`, item);
  }

  deleteItem(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
```

## CI/CD

### GitHub Actions
- **CI**: Runs on every push/PR — builds, tests, generates coverage
- **CD**: Deploys to staging/production on main branch merges

### Jenkins
- Full pipeline with build, test, coverage, SonarQube, Docker, and deployment stages

### SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN
```

## Project Structure

```
src/
├── main/
│   ├── java/com/jahnavi/api/
│   │   ├── config/          # CORS, OpenAPI configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── exception/       # Custom exceptions & global handler
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/
    └── java/com/jahnavi/api/
        ├── controller/      # Controller unit tests
        ├── service/         # Service unit tests
        └── integration/     # Integration tests
```

## License

MIT
