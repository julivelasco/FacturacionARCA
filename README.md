# Proyecto final – Sistema de facturación

Sistema de facturación con integración a ARCA (homologación, Factura C).

- **Back:** Spring Boot (Java 17, Maven) + PostgreSQL en Docker.
- **Front:** TypeScript + HTML + Tailwind CSS.

## Contenido de la carpeta

```
Proyecto final/
  backend/          proyecto Spring Boot (la carpeta que tiene el pom.xml)
  frontend/         index.html + código TypeScript
  backup_tp.sql     copia de la base de datos con los datos de prueba
  README.md         este archivo
```

## Qué hay que tener instalado

- Docker Desktop
- JDK 17 e IntelliJ IDEA (o Maven por consola)
- VS Code con la extensión **Live Server**
- Node.js (solo si se quiere modificar el front y recompilar con `tsc`)

## Paso 1: levantar la base de datos

Con Docker Desktop abierto, en una terminal (CMD o Git Bash):

```
docker run --name postgres-tp -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -e POSTGRES_DB=postgres -p 5432:5432 -d postgres
```

Los datos (usuario `admin`, clave `admin123`, base `postgres`, puerto `5432`) tienen que ser
exactamente esos, porque son los que usa `application.properties`.

Después, parado en la carpeta donde está `backup_tp.sql`, restaurar los datos:

```
docker exec -i postgres-tp psql -U admin -d postgres < backup_tp.sql
```

(Usar CMD o Git Bash. En PowerShell el `<` no funciona.)

## Paso 2: arrancar el back

1. En IntelliJ: **File > Open** y elegir el archivo `pom.xml` de la carpeta `backend`
   (la que contiene `src`). Elegir **Open as Project**.
2. Esperar a que termine de importar Maven.
3. Abrir `Tp4springApplication.java` y ejecutarla con el play verde.
4. En la consola tiene que aparecer: *"CargaDatos: los datos de prueba ya estaban cargados. No se hizo nada."*
   Eso confirma que la base se restauró bien. El back queda en `http://localhost:8080`.

## Paso 3: abrir el front

1. Abrir la carpeta `frontend` con VS Code.
2. Clic derecho sobre `index.html` > **Open with Live Server**.

Si se modifica algún archivo `.ts`, recompilar con `tsc` (o `tsc -w` para que recompile solo)
desde la carpeta `frontend`.

## Sobre ARCA

El sistema emite facturas con CAE reales de **homologación** (pruebas). Para eso hace falta un
certificado y una clave privada propios, que **no vienen incluidos** (son personales).

- **Sin certificado:** todo el sistema funciona normalmente (facturas, clientes, artículos,
  precios, catálogos). Solo el botón *Informar a ARCA* muestra un mensaje avisando que ARCA
  no está configurado en esta computadora.
- **Con certificado:** definir estas variables de entorno antes de arrancar el back
  (en IntelliJ: Run > Edit Configurations > Environment variables):

```
ARCA_CUIT=<CUIT del emisor, solo números>
ARCA_CLAVE_PRIVADA=<ruta a la clave privada .key>
ARCA_CERTIFICADO=<ruta al certificado .crt>
ARCA_TICKET=<ruta donde guardar el ticket de acceso, un .txt>
```

Las facturas que ya figuran como informadas en la base de datos conservan su CAE.

## Si algo falla

- **El back no conecta a la base:** verificar que el contenedor esté corriendo (`docker ps`)
  y que el puerto `5432` no esté ocupado por otro Postgres.
- **La consola dice "supermercado cargado..." en vez de "ya estaban cargados":** el backup
  no se restauró; el sistema creó datos de prueba nuevos (sin las facturas informadas a ARCA).
  Borrar el contenedor (`docker rm -f postgres-tp`) y repetir el Paso 1.
- **Los archivos Java se ven en naranja en IntelliJ:** se abrió la carpeta equivocada.
  Cerrar el proyecto y abrirlo eligiendo el `pom.xml`.
