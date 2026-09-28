# Gestión de Clubes

Aplicación web orientada a objetos para la gestion de usuarios, clubes y actividades de dichos clubes. La primera versión incluye registro, inicio de sesión y autorización para tres roles: `USUARIO`, `ADMIN_CLUB` y `ADMIN`.

## Tecnologías

- Java 17 y Spring Boot 4.1.1
- Spring MVC, Security y Data JPA
- Thymeleaf, HTML y CSS
- MySQL 8.4
- Maven y Docker

## Ejecutar en desarrollo

Requisitos: JDK 21, Maven 3.9+ y Docker.

1. Inicia MySQL:

   ```bash
   docker compose up -d mysql
   ```

Puedes comprobar las instalaciones con:

```bash
java -version
mvn -version
docker --version
docker compose version
git --version
```

2. Opcionalmente puedes configura el administrador inicial:

   ```bash
   export ADMIN_EMAIL=admin@clubes.local
   export ADMIN_PASSWORD='UnaClaveSegura123!'
   ```

3. Ejecuta la aplicación:

   ```cmd
   mvn spring-boot:run
   ```

4. Visita [http://localhost:8080](http://localhost:8080).


## Ruta de desarrollo

1. Autenticación y roles.
2. CRUD de clubes y asignación de administradores.
3. Solicitudes de ingreso a clubes.
4. Actividades y participación.
5. Pruebas, CI y despliegue.
