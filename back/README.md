# ZGZ Emergency Map — Backend

## Requisitos

- **Java 17**
- **Maven** (o usar el wrapper incluido: `./mvnw`)
- **PostgreSQL** accesible desde la máquina donde arranques la aplicación

## Instalación y arranque

Desde el directorio `back/`:

```bash
./mvnw clean package   # o: mvn clean package
./mvnw spring-boot:run # o: mvn spring-boot:run
```

En Windows: `mvnw.cmd` en lugar de `./mvnw`.

## Configuración

La configuración principal está en **`src/main/resources/application.properties`**.

| Variable de entorno | Descripción |
|---------------------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL. Por defecto: `jdbc:postgresql://localhost:5432/zgz_emergency` |
| `POSTGRES_USER` | Usuario de la base de datos (por defecto en propiedades de ejemplo: `juanfran`) |
| `POSTGRES_PASSWORD` | Contraseña del usuario |

Crea la base de datos y usuario en PostgreSQL antes de arrancar, o adapta los valores por defecto a tu entorno local.

**JPA**: `spring.jpa.hibernate.ddl-auto=update` (esquema gestionado por Hibernate según las entidades; revisa esto en entornos compartidos o producción).

## Tests

```bash
./mvnw test
```

Incluye tests de contexto Spring y pruebas unitarias con mocks en el paquete de tests (por ejemplo `JsonConverterServiceTest`).
