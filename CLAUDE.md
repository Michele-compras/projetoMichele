# Project: projeto

Spring Boot 4.0.3 application using Java 25 and Maven.
Sistema de Ficha Técnica para itens importados (tecidos e acessórios).

## Commands

```bash
# Build (skip tests) — requires SSL workaround
MAVEN_OPTS="-Dmaven.resolver.transport=wagon" ./mvnw.cmd clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Run
MAVEN_OPTS="-Dmaven.resolver.transport=wagon" ./mvnw.cmd spring-boot:run -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Compile only
MAVEN_OPTS="-Dmaven.resolver.transport=wagon" ./mvnw.cmd clean compile -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Run all tests
MAVEN_OPTS="-Dmaven.resolver.transport=wagon" ./mvnw.cmd test -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
```

## SSL Note
This machine has a corporate proxy/firewall that intercepts HTTPS. Maven requires `MAVEN_OPTS="-Dmaven.resolver.transport=wagon"` plus wagon SSL flags to download dependencies.

## Structure

- **Package:** `com.example.projeto`
- **Entry point:** `src/main/java/com/example/projeto/ProjetoApplication.java`
- **Tests:** `src/test/java/com/example/projeto/`
- **Build config:** `pom.xml`
- **Database:** H2 file-based at `./data/fichatecnica` (H2 console at `/h2-console`)
- **Templates:** `src/main/resources/templates/`

## Package Layout

```
com.example.projeto
├── ProjetoApplication.java
├── config/WebConfig.java
├── controller/FichaTecnicaController.java
├── model/FichaTecnica.java, TipoItem.java, StatusAmostra.java
├── repository/FichaTecnicaRepository.java
└── service/FichaTecnicaService.java
```

## Conventions

- Use `./mvnw.cmd` (Maven Wrapper) instead of a global `mvn` install
- Follow standard Maven directory layout (`src/main/java`, `src/test/java`)
- Jakarta EE imports (`jakarta.persistence.*`, not `javax.persistence.*`)
- Interface in Portuguese (BR)
