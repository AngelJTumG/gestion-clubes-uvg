# Gestión de Clubes

Plataforma web desarrollada en Java mediante programación orientada a objetos para la gestión de clubes, solicitudes de ingreso y actividades.

El proyecto utiliza la arquitectura Modelo-Vista-Controlador (MVC) y está siendo desarrollado de forma colaborativa con GitHub.

> [!IMPORTANT]
> El código fuente en desarrollo se encuentra actualmente en la rama [`develop`](../../tree/develop).
>
> La rama `main` está reservada para versiones estables, revisadas y preparadas para publicación.

## Estado del proyecto

El proyecto se encuentra en desarrollo.

Roles iniciales:

- `USUARIO`
- `ADMIN_CLUB`
- `ADMIN`

## Consultar el código

Puedes acceder al código más reciente desde la rama:

### [`Ver rama develop`](../../tree/develop)

También puedes seleccionar la rama `develop` desde el menú de ramas de GitHub.

## Clonar el proyecto

Clona el repositorio:

```bash
git clone URL_DEL_REPOSITORIO
```

Reemplaza `URL_DEL_REPOSITORIO` por el enlace real del repositorio.

Entra en la carpeta:

```bash
cd gestion-clubes
```

Descarga la información de todas las ramas:

```bash
git fetch origin
```

Cambia a la rama de desarrollo:

```bash
git switch develop
```

Actualiza el código:

```bash
git pull origin develop
```

Comprueba la rama actual:

```bash
git branch
```

El resultado debe mostrar:

```text
* develop
  main
```

## Tecnologías principales

- Java 17
- Spring Boot 4.1.1
- Spring MVC
- Spring Security
- Spring Data JPA
- Thymeleaf
- HTML, CSS 
- MySQL 8.4
- Maven
- GitHub
- Docker y Docker Compose
- Railway para el futuro despliegue

### Rama `main`

Contiene únicamente versiones estables, revisadas y listas para desplegar.

No se debe trabajar directamente sobre esta rama.

### Rama `develop`

Contiene el código integrado de la versión que se encuentra actualmente en desarrollo.

Esta es la rama que los integrantes deben utilizar como base para crear nuevas funcionalidades.
