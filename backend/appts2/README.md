# ERP Adapter

Spring Boot integration adapter for ERP/CRM data. It queries the external ERP source through its configured JDBC/ODBC driver, normalizes the results, and exposes HTTP endpoints for the Order Platform API.

## Configuration

The adapter needs its application MySQL database and ERP connection settings. `.env.example` lists the variables but is not loaded automatically; configure them through the shell, IDE, or Compose environment.

ERP settings:

- `APP_ERP_ODBC_DRIVER_CLASS_NAME`
- `APP_ERP_ODBC_URL`
- `APP_ERP_ODBC_USERNAME`
- `APP_ERP_ODBC_PASSWORD`

Database settings:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The ERP driver and connection details are provided by the ERP environment and are not included in this repository.

## Test and run

```bash
mvn test
mvn spring-boot:run
```

See the [repository README](../../README.md) for the architecture and complete local setup.
