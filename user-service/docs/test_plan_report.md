# Plan de Pruebas Unitarias - User Service

## 📋 Información General

**Proyecto:** E-commerce Microservice Backend  
**Microservicio:** User Service  
**Tipo de Pruebas:** Unitarias  
**Framework:** JUnit 5 + Mockito  
**Fecha:** Mayo 2025  

---

## 🎯 Objetivos

- Validar la funcionalidad de los servicios de negocio del microservicio User Service
- Asegurar el manejo correcto de excepciones y casos límite
- Verificar la integración correcta con la capa de persistencia
- Garantizar cobertura de código superior al 85%

---

## 🏗️ Arquitectura de Pruebas

### Patrón de Nomenclatura
```
MethodName_WhenCondition_ExpectedBehavior
```

### Categorías de Pruebas
- **✅ Casos Positivos:** Flujo normal y exitoso
- **❌ Casos Negativos:** Manejo de errores y excepciones
- **🔄 Casos Interesantes:** Edge cases y situaciones especiales

### Herramientas Utilizadas
- **JUnit 5:** Framework principal de pruebas
- **Mockito:** Mocking y stubbing
- **AssertJ:** Assertions fluidas (opcional)
- **Spring Boot Test:** Configuración de contexto de pruebas

---

## 🧪 Servicios Bajo Prueba

### 1. UserServiceImpl

| Método | Casos Positivos | Casos Negativos | Casos Interesantes | Total |
|--------|----------------|-----------------|-------------------|-------|
| `findAll()` | 1 | 1 | 1 | 3 |
| `findById()` | 1 | 1 | 1 | 3 |
| `save()` | 1 | 1 | 1 | 3 |
| `update()` (simple) | 1 | 1 | 0 | 2 |
| `update()` (con ID) | 1 | 1 | 0 | 2 |
| `deleteById()` | 1 | 1 | 1 | 3 |
| `findByUsername()` | 1 | 1 | 1 | 3 |
| **Total UserService** | **7** | **7** | **5** | **19** |

#### Escenarios Específicos Validados:
- ✅ Recuperación exitosa de usuarios
- ❌ Usuario no encontrado (UserObjectNotFoundException)
- 🔄 Manejo de listas vacías y duplicados
- 🔄 Validación con parámetros nulos
- 🔄 Propagación de excepciones del repositorio

### 2. CredentialServiceImpl

| Método | Casos Positivos | Casos Negativos | Casos Interesantes | Total |
|--------|----------------|-----------------|-------------------|-------|
| `findAll()` | 1 | 1 | 1 | 3 |
| `findById()` | 1 | 1 | 1 | 3 |
| `save()` | 1 | 1 | 1 | 3 |
| `update()` (simple) | 1 | 1 | 0 | 2 |
| `update()` (con ID) | 1 | 1 | 0 | 2 |
| `deleteById()` | 1 | 1 | 1 | 3 |
| `findByUsername()` | 1 | 1 | 1 | 3 |
| **Total CredentialService** | **7** | **7** | **5** | **19** |

#### Escenarios Específicos Validados:
- ✅ Manejo de diferentes roles (USER/ADMIN)
- ❌ Credencial no encontrada (CredentialNotFoundException)
- 🔄 Estados de cuenta (enabled/disabled, locked/unlocked)
- 🔄 Validación de constrains de base de datos
- 🔄 Manejo de usernames vacíos

### 3. AddressServiceImpl

| Método | Casos Positivos | Casos Negativos | Casos Interesantes | Total |
|--------|----------------|-----------------|-------------------|-------|
| `findAll()` | 1 | 1 | 1 | 3 |
| `findById()` | 1 | 1 | 1 | 3 |
| `save()` | 1 | 1 | 1 | 3 |
| `update()` (simple) | 1 | 1 | 0 | 2 |
| `update()` (con ID) | 1 | 1 | 0 | 2 |
| `deleteById()` | 1 | 1 | 1 | 3 |
| **Total AddressService** | **6** | **6** | **4** | **16** |

#### Escenarios Específicos Validados:
- ✅ Direcciones con datos completos
- ❌ Dirección no encontrada (AddressNotFoundException)
- 🔄 Direcciones con datos mínimos (solo ciudad)
- 🔄 Relación bidireccional con User
- 🔄 Constraints de llaves foráneas

### 4. VerificationTokenServiceImpl

| Método | Casos Positivos | Casos Negativos | Casos Interesantes | Total |
|--------|----------------|-----------------|-------------------|-------|
| `findAll()` | 1 | 1 | 1 | 3 |
| `findById()` | 1 | 1 | 1 | 3 |
| `save()` | 1 | 1 | 1 | 3 |
| `update()` (simple) | 1 | 1 | 0 | 2 |
| `update()` (con ID) | 1 | 1 | 0 | 2 |
| `deleteById()` | 1 | 1 | 1 | 3 |
| **Total VerificationTokenService** | **6** | **6** | **4** | **16** |

#### Escenarios Específicos Validados:
- ✅ Tokens válidos con fechas futuras
- ❌ Token no encontrado (VerificationTokenNotFoundException)
- 🔄 Tokens expirados (fechas pasadas)
- 🔄 Relación con credenciales
- 🔄 Cleanup de tokens

---

## 📊 Resumen Estadístico

### Distribución por Tipo de Prueba
- **Casos Positivos:** 26 pruebas (37%)
- **Casos Negativos:** 26 pruebas (37%)
- **Casos Interesantes:** 18 pruebas (26%)
- **Total:** **70 pruebas unitarias**

### Distribución por Servicio
- **UserService:** 19 pruebas (27%)
- **CredentialService:** 19 pruebas (27%)
- **AddressService:** 16 pruebas (23%)
- **VerificationTokenService:** 16 pruebas (23%)

### Cobertura Esperada
- **Líneas de código:** >85%
- **Métodos:** 100%
- **Clases de servicio:** 100%

---

## 🔧 Configuración Técnica

### Dependencias Maven
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

### Anotaciones Utilizadas
- `@ExtendWith(MockitoExtension.class)`
- `@Mock` para repositorios
- `@InjectMocks` para servicios
- `@BeforeEach` para configuración inicial

---

## ✅ Criterios de Aceptación

### Funcionales
- [x] Todas las operaciones CRUD validadas
- [x] Manejo correcto de excepciones específicas
- [x] Validación de relaciones entre entidades
- [x] Casos límite cubiertos

### No Funcionales
- [x] Tiempo de ejecución < 10 segundos
- [x] Sin dependencias externas (base de datos real)
- [x] Pruebas aisladas e independientes
- [x] Nomenclatura consistente y descriptiva

---

## 🚀 Ejecución

### Comandos Maven
```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas con reporte de cobertura
mvn test jacoco:report

# Ejecutar solo pruebas de servicios
mvn test -Dtest="*ServiceImplTest"
```

### Estructura de Archivos
```
src/test/java/com/selimhorri/app/service/impl/
├── UserServiceImplTest.java
├── CredentialServiceImplTest.java
├── AddressServiceImplTest.java
└── VerificationTokenServiceImplTest.java
```

---

## 📈 Métricas de Calidad

### Objetivos de Coverage
| Métrica | Objetivo | Esperado |
|---------|----------|----------|
| Line Coverage | >85% | ~90% |
| Branch Coverage | >80% | ~85% |
| Method Coverage | 100% | 100% |
| Class Coverage | 100% | 100% |

### Tiempos de Ejecución Esperados
- **Por clase de prueba:** <3 segundos
- **Total suite:** <10 segundos
- **Por método individual:** <100ms

---

## 🎯 Beneficios Obtenidos

### Detección Temprana de Errores
- Validación de lógica de negocio
- Verificación de manejo de excepciones
- Comprobación de integraciones con repositorios

### Documentación Viva
- Las pruebas sirven como especificación
- Ejemplos de uso de cada método
- Comportamientos esperados documentados

### Refactoring Seguro
- Confianza para modificar código
- Detección automática de regresiones
- Mantenimiento simplificado

### Calidad de Código
- Mejor diseño por testabilidad
- Separación clara de responsabilidades
- Código más robusto y confiable

---

## 📝 Conclusiones

El plan de pruebas unitarias implementado para el User Service proporciona una cobertura completa de la funcionalidad crítica del microservicio. Con **70 pruebas unitarias** distribuidas estratégicamente entre los 4 servicios principales, se garantiza la validación de casos normales, excepcionales y límite.

La implementación sigue las mejores prácticas de la industria utilizando patrones AAA (Arrange-Act-Assert), nomenclatura descriptiva y mocking apropiado. Esto establece una base sólida para el desarrollo orientado por pruebas y facilita el mantenimiento futuro del sistema.

---

**Autor:** Equipo de Desarrollo  
**Última actualización:** Mayo 2025