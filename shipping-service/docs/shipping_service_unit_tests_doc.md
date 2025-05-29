# Plan de Pruebas Unitarias - Shipping Service

## Resumen Ejecutivo

Este documento describe el plan de pruebas unitarias implementado para el microservicio Shipping Service del sistema de ecommerce. Las pruebas se han diseñado siguiendo la convención **MethodName_WhenCondition_ExpectedBehavior** y cubren los componentes más críticos del servicio de envíos a través de la gestión de items de órdenes.

## Servicio Analizado

### OrderItemServiceImpl
Servicio principal que maneja las operaciones CRUD de items de órdenes para el sistema de envíos.

**Características principales:**
- Gestión de items de órdenes con claves compuestas (ProductId + OrderId)
- Integración dual con Product Service y Order Service
- Manejo de cantidades ordenadas para envíos
- Validación de integridad de datos de envío

## Estrategia de Pruebas

### Convención de Nomenclatura
Todas las pruebas siguen el patrón: `MethodName_WhenCondition_ExpectedBehavior`

### Framework Utilizado
- **JUnit 5** - Framework principal de testing
- **Mockito** - Framework de mocking para dependencias
- **@ExtendWith(MockitoExtension.class)** - Integración Mockito-JUnit

## Arquitectura del Shipping Service

### Clave Compuesta (OrderItemId):
```java
public class OrderItemId {
    private Integer productId;
    private Integer orderId;
}
```

### Integraciones:
- **Product Service**: Para obtener detalles de productos
- **Order Service**: Para obtener información de órdenes

## Detalle de Pruebas Implementadas

### OrderItemServiceImpl Tests

#### 1. `findAll_WhenOrderItemsExist_ShouldReturnOrderItemDtoListWithIntegratedData`
- **Propósito**: Validar que findAll() retorne correctamente items de órdenes con datos integrados de productos y órdenes
- **Escenario**: Repositorio contiene 2 items de órdenes diferentes
- **Validaciones**:
  - Lista no es nula y contiene 2 elementos
  - Cantidades ordenadas correctas (2, 1)
  - Datos de productos integrados desde Product Service
  - Datos de órdenes integrados desde Order Service
  - Invocaciones correctas a ambos servicios externos

#### 2. `findById_WhenOrderItemExists_ShouldReturnOrderItemDtoWithIntegratedData`
- **Propósito**: Verificar que findById() con clave compuesta retorne el item correcto con datos integrados
- **Escenario**: Item de orden con clave compuesta (productId=1, orderId=1) existe
- **Validaciones**:
  - OrderItemDto retornado no es nulo
  - Clave compuesta mapeada correctamente
  - Cantidad ordenada correcta (3 unidades)
  - Datos de producto integrados (título, SKU, precio)
  - Datos de orden integrados (descripción, fee)
  - Invocaciones correctas a servicios externos

#### 3. `findById_WhenOrderItemNotExists_ShouldThrowOrderItemNotFoundException`
- **Propósito**: Validar manejo de errores cuando un item de orden no existe
- **Escenario**: Clave compuesta (999, 999) no existe en el repositorio
- **Validaciones**:
  - Se lanza OrderItemNotFoundException
  - Mensaje de error específico y descriptivo
  - No se hacen llamadas innecesarias a servicios externos
  - Se invoca el repositorio correctamente

#### 4. `save_WhenValidOrderItemDto_ShouldReturnSavedOrderItemDto`
- **Propósito**: Verificar guardado correcto de nuevos items de órdenes
- **Escenario**: OrderItemDto válido con productId=2, orderId=2, quantity=5
- **Validaciones**:
  - OrderItemDto retornado con clave compuesta correcta
  - Cantidad ordenada persistida correctamente (5 unidades)
  - Mapeo bidireccional funcional
  - Referencias a producto y orden mantenidas
  - Se invoca save del repositorio

#### 5. `update_WhenValidOrderItemDto_ShouldReturnUpdatedOrderItemDto`
- **Propósito**: Validar actualización correcta de items de órdenes existentes
- **Escenario**: Item existente actualizando cantidad de 3 a 10 unidades
- **Validaciones**:
  - Cantidad actualizada correctamente (10 unidades)
  - Clave compuesta preservada
  - Referencias a producto y orden mantenidas
  - Mapeo de actualización funcional
  - Se invoca save del repositorio

#### 6. `deleteById_WhenValidOrderItemId_ShouldCallRepositoryDelete`
- **Propósito**: Validar eliminación correcta de items de órdenes
- **Escenario**: Clave compuesta válida para eliminar
- **Validaciones**:
  - No se lanzan excepciones
  - Se invoca deleteById con clave compuesta correcta
  - Eliminación por clave compuesta funcional

#### 7. `findAll_WhenMultipleQuantitiesExist_ShouldReturnCorrectQuantities`
- **Propósito**: Verificar manejo correcto de diferentes cantidades y precios
- **Escenario**: Items con cantidades extremas (100 vs 1) y precios diferentes
- **Validaciones**:
  - Cantidades grandes (100 unidades) manejadas correctamente
  - Cantidades pequeñas (1 unidad) preservadas
  - Precios altos ($500.0) y bajos ($5.0) integrados
  - Cálculos de totales coherentes ($50,005.0)
  - Datos de productos específicos correctos

#### 8. `save_WhenOrderItemWithCompositeKey_ShouldHandleCorrectly`
- **Propósito**: Validar manejo específico de claves compuestas en guardado
- **Escenario**: Nuevo item con clave compuesta (productId=5, orderId=3)
- **Validaciones**:
  - Clave compuesta (5,3) manejada correctamente
  - Cantidad específica (7 unidades) persistida
  - Ambos componentes de la clave preservados
  - Integridad referencial mantenida
  - Mapeo de clave compuesta funcional

#### 9. `findById_WhenIntegrationWithExternalServices_ShouldMapDataCorrectly`
- **Propósito**: Validar integración específica con servicios externos
- **Escenario**: Item requiere datos de Product Service y Order Service
- **Validaciones**:
  - Llamadas correctas a Product Service API
  - Llamadas correctas a Order Service API
  - Mapeo correcto de ProductDto
  - Mapeo correcto de OrderDto
  - URLs de integración construidas correctamente

## Cobertura de Componentes

### Componentes Validados:
- ✅ **Servicio principal** (OrderItemServiceImpl)
- ✅ **Mapeo de entidades** (OrderItemMappingHelper)
- ✅ **Manejo de excepciones** (OrderItemNotFoundException)
- ✅ **Clave compuesta** (OrderItemId)
- ✅ **Integración con Product Service** (RestTemplate)
- ✅ **Integración con Order Service** (RestTemplate)
- ✅ **Operaciones CRUD con claves compuestas**

### Aspectos Críticos Cubiertos:
- **Claves compuestas**: Manejo correcto de ProductId + OrderId
- **Cantidades de envío**: Validación de ordered quantities
- **Integración dual**: Comunicación con dos microservicios
- **Manejo de errores**: Excepciones específicas del dominio
- **Integridad referencial**: Coherencia entre items, productos y órdenes
- **Validaciones de negocio**: Cantidades, precios y totales

## Particularidades del Shipping Service

### Clave Compuesta (OrderItemId):
- **Componentes**: ProductId + OrderId
- **Unicidad**: Combinación única para cada item de envío
- **Persistencia**: Manejo especial en JPA con @IdClass

### Integración Dual:
```java
// Product Service Integration
ProductDto product = restTemplate.getForObject(
    PRODUCT_SERVICE_API_URL + "/" + productId, ProductDto.class);

// Order Service Integration  
OrderDto order = restTemplate.getForObject(
    ORDER_SERVICE_API_URL + "/" + orderId, OrderDto.class);
```

### Lógica de Negocio:
- **Cantidades**: Validación de ordered quantities para envío
- **Disponibilidad**: Coherencia con stock de productos
- **Costos**: Integración con fees de órdenes

## Configuración de Dependencias

Para ejecutar estas pruebas, agregar al `pom.xml`:

```xml
<dependencies>
    <!-- Dependencias de testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Para integración REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Ejecución de Pruebas

### Comando Maven:
```bash
mvn test
```

### Comando específico para la clase:
```bash
mvn test -Dtest=OrderItemServiceImplTest
```

### Con perfil específico:
```bash
mvn test -Dspring.profiles.active=dev -Dtest=OrderItemServiceImpl*
```

## Métricas Esperadas

### Cobertura de Código:
- **OrderItemServiceImpl**: >95%
- **Métodos con claves compuestas**: 100%
- **Integración con servicios**: 100%
- **Manejo de cantidades**: 100%

### Resultados Esperados:
- ✅ **9 pruebas unitarias** ejecutadas exitosamente
- ✅ **Tiempo de ejecución**: < 4 segundos
- ✅ **0 fallas** en ejecución normal
- ✅ **Validación completa** de claves compuestas

## Integraciones con Microservicios

### Product Service Integration:
- **Endpoint**: `/api/products/{productId}`
- **Datos**: ProductDto con título, SKU, precio, cantidad
- **Validación**: Mapeo correcto de datos de producto

### Order Service Integration:
- **Endpoint**: `/api/orders/{orderId}`
- **Datos**: OrderDto con fecha, descripción, fee
- **Validación**: Mapeo correcto de datos de orden

### Error Handling:
- **Service unavailable**: Manejo de timeouts
- **Data inconsistency**: Validación de integridad
- **Network issues**: Resilience patterns

## Casos de Uso de Negocio Cubiertos

### Flujo de Envío Completo:
1. **Creación**: Nuevo item de envío con cantidad específica
2. **Consulta**: Recuperación con datos integrados de producto y orden
3. **Actualización**: Modificación de cantidades para envío
4. **Eliminación**: Limpieza de items procesados
5. **Validación**: Coherencia entre cantidades y disponibilidad

### Escenarios de Envío:
- **Envíos bulk**: Cantidades grandes (100+ unidades)
- **Envíos individuales**: Cantidades pequeñas (1 unidad)
- **Productos premium**: Items de alto valor
- **Productos básicos**: Items de bajo costo
- **Órdenes mixtas**: Múltiples items por orden

## Conclusiones

Las pruebas unitarias implementadas para Shipping Service cubren:

1. **Manejo completo** de claves compuestas (OrderItemId)
2. **Integración robusta** con Product y Order Services
3. **Validación de cantidades** para lógica de envíos
4. **Manejo de errores** específicos del dominio de envíos
5. **Operaciones CRUD** con entidades complejas
6. **Mapeo bidireccional** entre capas de aplicación

Estas pruebas garantizan que el servicio de envíos funcione correctamente de forma aislada, validando especialmente el manejo de claves compuestas y la integración dual con otros microservicios.

## Próximos Pasos

Para completar la suite de pruebas del Shipping Service:
1. Implementar pruebas de integración con Product y Order Services reales
2. Crear pruebas E2E para flujos completos de envío
3. Desarrollar pruebas de rendimiento para procesamiento de envíos bulk
4. Validar manejo de transacciones distribuidas
5. Configurar pipelines CI/CD con validación de integraciones
6. Implementar pruebas de stress para manejo de claves compuestas