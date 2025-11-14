# PDGIGS - Sistema de Gestión de Partituras Musicales

API RESTful reactiva para la gestión de partituras musicales en formato PDF, construida con Spring WebFlux y MongoDB.

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.4.0** (WebFlux - Reactive)
- **MongoDB 7.0** (Reactive)
- **Docker & Docker Compose**
- **SpringDoc OpenAPI 2.3.0** (Swagger UI)
- **Maven**
- **Lombok**

## 📋 Funcionalidades (CRUD Completo)

- ✅ **CREATE**: Subir partituras en PDF con metadata (título, autor, estilo musical)
- ✅ **READ**: Obtener y descargar partituras por ID
- ✅ **UPDATE**: Actualizar metadata de partituras existentes
- ✅ **DELETE**: Eliminar partituras y sus archivos asociados

## 🛠️ Requisitos Previos

- Docker y Docker Compose instalados
- Java 21 (opcional, solo si deseas ejecutar sin Docker)
- Maven 3.9+ (opcional, solo si deseas compilar manualmente)

## 🐳 Ejecución con Docker

### 1. Compilar el proyecto

```bash
mvn clean package -DskipTests
```

### 2. Levantar los servicios con Docker Compose

```bash
docker-compose up -d
```

Esto iniciará:
- **MongoDB** en el puerto `27017`
- **PDGIGS API** en el puerto `8080`

### 3. Verificar que los contenedores estén corriendo

```bash
docker-compose ps
```

### 4. Ver logs

```bash
# Logs de la aplicación
docker-compose logs -f pdgigs-app

# Logs de MongoDB
docker-compose logs -f mongodb
```

### 5. Detener los servicios

```bash
docker-compose down
```

### 6. Detener y eliminar volúmenes (limpieza completa)

```bash
docker-compose down -v
```

## 📖 Documentación API (Swagger)

Una vez que la aplicación esté corriendo, accede a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🧪 Probar los Endpoints

### 1. Subir una partitura (CREATE)

```bash
curl -X POST "http://localhost:8080/api/scores" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@partitura.pdf" \
  -F "title=Concierto No. 5" \
  -F "author=Mozart" \
  -F "musicalStyle=Clásico"
```

**Respuesta esperada (201 Created):**
```json
{
  "id": "674b8e1234567890abcdef12",
  "title": "Concierto No. 5",
  "author": "Mozart",
  "musicalStyle": "Clásico",
  "fileSize": 245678
}
```

### 2. Obtener una partitura por ID (READ)

```bash
curl -X GET "http://localhost:8080/api/scores/{id}"
```

### 3. Actualizar metadata (UPDATE)

```bash
curl -X PATCH "http://localhost:8080/api/scores/{id}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Concierto No. 5 - Edición Revisada",
    "author": "Wolfgang Amadeus Mozart",
    "musicalStyle": "Clásico Vienés"
  }'
```

### 4. Eliminar una partitura (DELETE)

```bash
curl -X DELETE "http://localhost:8080/api/scores/{id}"
```

**Respuesta esperada (204 No Content)**

### 5. Partitura no encontrada (404)

```bash
curl -X DELETE "http://localhost:8080/api/scores/P-99"
```

**Respuesta esperada (404 Not Found):**
```json
{
  "message": "Score with ID P-99 not found.",
  "status": 404
}
```

## 🏗️ Estructura del Proyecto (Arquitectura Hexagonal)

```
src/
├── main/
│   ├── java/com/pdgigs/
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   ├── input/          # Casos de uso
│   │   │   │   └── output/         # Puertos de salida
│   │   │   └── service/            # Servicios de aplicación
│   │   ├── domain/
│   │   │   ├── exception/          # Excepciones de dominio
│   │   │   └── model/              # Modelos de dominio
│   │   └── infrastructure/
│   │       ├── adapter/
│   │       │   ├── input/rest/     # Controladores REST
│   │       │   └── output/         # Adaptadores de persistencia
│   │       └── config/             # Configuraciones
│   └── resources/
│       └── application.yml
└── test/                            # Tests unitarios e integración
```

## 🧪 Ejecutar Tests

```bash
mvn test
```

## 📊 Historias de Usuario Implementadas

- ✅ **HU-1**: Subir partituras con validación de formato y tamaño
- ✅ **HU-2**: Descargar partituras por ID
- ✅ **HU-3**: Actualizar metadata de partituras
- ✅ **HU-4**: Eliminar partituras por ID

## 🔐 Validaciones Implementadas

- ✅ Formato de archivo: Solo PDF permitidos (415 Unsupported Media Type)
- ✅ Tamaño máximo: 10MB (413 Payload Too Large)
- ✅ ID no encontrado: 404 Not Found

## 📝 Licencia

MIT License

## 👨‍💻 Autor

**adolcc** - [GitHub](https://github.com/adolcc)