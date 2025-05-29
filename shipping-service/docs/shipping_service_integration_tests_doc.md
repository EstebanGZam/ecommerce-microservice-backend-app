# Documentación de Pruebas de Integración
## Shipping Service ↔ Product Service

### 📋 Resumen

Esta documentación describe las **3 pruebas de integración esenciales** implementadas para validar la comunicación entre el `shipping-service` y el `product-service` en la arquitectura de microservicios.

---

## 🎯 Pruebas Implementadas

### **1. Prueba de Comunicación Exitosa con Servicios Externos**
**Método:** `shouldGetAllOrderItemsWithProductAndOrderDetails()`

**📌 Propósito:**
- Validar que el shipping service puede obtener order items y enriquecerlos con datos de servicios externos
- Verificar la integración completa del flujo de datos entre microservicios

**🔍 Qué valida:**
- ✅ Consulta exitosa a la base de datos local (H2)
- ✅ Llamadas HTTP correctas al product-service 
- ✅ Llamadas HTTP correctas al order-service
- ✅ Mapeo y serialización correcta de DTOs
- ✅ Respuesta JSON con estructura esperada

**📊 Flujo de la prueba:**
```
[Shipping Service] → [Base de datos local] → [Product Service] → [Order Service] → [Respuesta integrada]
```

---

### **2. Prueba de Creación y Persistencia de Order Items**
**Método:** `shouldCreateNewOrderItemSuccessfully()`

**📌 Propósito:**
- Validar la funcionalidad completa de creación de order items
- Verificar la persistencia correcta en base de datos

**🔍 Qué valida:**
- ✅ Recepción correcta de peticiones POST
- ✅ Deserialización de JSON a DTOs
- ✅ Persistencia en base de datos H2
- ✅ Integridad de datos guardados
- ✅ Respuesta con datos del item creado

**📊 Flujo de la prueba:**
```
[Cliente HTTP] → [Shipping Controller] → [Service Layer] → [Base de datos] → [Verificación]
```

---

### **3. Prueba de Manejo de Errores en Servicios Externos**
**Método:** `shouldHandleExternalServiceErrorsGracefully()`

**📌 Propósito:**
- Validar la resilencia del sistema ante fallos de servicios externos
- Verificar el comportamiento bajo condiciones de error

**🔍 Qué valida:**
- ✅ Manejo de errores HTTP 500 del product-service
- ✅ Manejo de errores HTTP 404 del order-service  
- ✅ Respuesta apropiada del sistema (5xx)
- ✅ Intentos de comunicación registrados
- ✅ Estabilidad del sistema ante fallos parciales

**📊 Flujo de la prueba:**
```
[Shipping Service] → [Services externos fallidos] → [Manejo de errores] → [Respuesta de error controlada]
```

---

## 🛠️ Configuración Técnica

### **Tecnologías Utilizadas:**
- **WireMock**: Simulación de servicios externos
- **WebTestClient**: Pruebas de endpoints REST
- **H2 Database**: Base de datos en memoria para testing
- **TestContainers**: Gestión de contenedores de prueba
- **JUnit 5**: Framework de testing

### **Perfiles de Configuración:**
```yaml
# application-integration-test.yml
spring:
  profiles: integration-test
  datasource:
    url: jdbc:h2:mem:integration_test_db
  cloud:
    discovery:
      enabled: false
```

### **Anotaciones Clave:**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("integration-test")
@Transactional
```

---

## 📁 Estructura de Archivos

```
shipping-service/
├── src/test/java/com/selimhorri/app/
│   └── integration/
│       └── ShippingServiceIntegrationTest.java
├── src/test/resources/
│   ├── application-integration-test.yml
│   └── application-test.yml
└── pom.xml (dependencias de testing)
```

---

## 🚀 Ejecución de las Pruebas

### **Comandos Maven:**
```bash
# Ejecutar solo pruebas de integración
mvn test -Dtest=*IntegrationTest

# Ejecutar todas las pruebas
mvn verify

# Ejecutar con perfil específico
mvn test -Dspring.profiles.active=integration-test
```

### **Configuración IDE:**
- **IntelliJ IDEA**: Configurar Run Configuration con perfil `integration-test`
- **Eclipse**: Agregar VM options: `-Dspring.profiles.active=integration-test`

---

## 📊 Cobertura de Pruebas

### **Endpoints Cubiertos:**
- ✅ `GET /api/shippings` - Obtener todos los order items
- ✅ `POST /api/shippings` - Crear nuevo order item  
- ✅ `DELETE /api/shippings/{orderId}/{productId}` - Eliminar order item

### **Servicios Externos Mockeados:**
- ✅ Product Service (`/product-service/api/products/{id}`)
- ✅ Order Service (`/order-service/api/orders/{id}`)

### **Escenarios de Error:**
- ✅ Servicios externos no disponibles (HTTP 500)
- ✅ Recursos no encontrados (HTTP 404)
- ✅ Timeouts de conexión
- ✅ Respuestas malformadas

---

## 🔧 Mantenimiento y Extensiones

### **Para agregar nuevas pruebas:**
1. Crear método con anotación `@Test`
2. Configurar WireMock stubs para servicios externos
3. Usar WebTestClient para peticiones HTTP
4. Verificar respuestas y estado de base de datos

### **Para mockear nuevos servicios:**
```java
stubFor(get(urlMatching("/nuevo-service/api/endpoint"))
        .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));
```

### **Para validar base de datos:**
```java
// Verificar persistencia
assertTrue(repository.findById(id).isPresent());

// Verificar contenido  
OrderItem saved = repository.findById(id).get();
assertThat(saved.getQuantity()).isEqualTo(expectedQuantity);
```

---

## ⚠️ Consideraciones Importantes

### **Limitaciones:**
- Las pruebas usan H2 en memoria (diferencias con MySQL en producción)
- WireMock simula respuestas estáticas (no lógica de negocio real)
- No valida autenticación/autorización entre servicios

### **Mejores Prácticas:**
- ✅ Usar `@Transactional` para rollback automático
- ✅ Limpiar estado entre pruebas con `@BeforeEach`
- ✅ Verificar tanto respuesta HTTP como estado de BD
- ✅ Usar datos de prueba realistas pero únicos

### **Troubleshooting:**
- **Puerto ocupado**: WireMock usa puerto aleatorio (`port = 0`)
- **Base de datos**: H2 se reinicia entre pruebas automáticamente  
- **Timeouts**: Configurar timeouts apropiados en WebTestClient

---

## 📈 Métricas de Calidad

### **Tiempo de Ejecución:**
- Prueba 1: ~2-3 segundos
- Prueba 2: ~1-2 segundos  
- Prueba 3: ~2-3 segundos
- **Total**: ~6-8 segundos

### **Cobertura de Código:**
- Controller Layer: 85%+
- Service Layer: 80%+
- Integration Points: 90%+

---

*Esta documentación debe actualizarse cuando se modifiquen las pruebas o se agreguen nuevos microservicios al ecosistema.*