# Plan de Pruebas Unitarias - Order Service

## Resumen Ejecutivo

Este documento describe el plan de pruebas unitarias implementado para el microservicio Order Service del sistema de ecommerce. Las pruebas se han diseñado siguiendo la convención **MethodName_WhenCondition_ExpectedBehavior** y cubren los componentes más críticos del servicio.

## Servicios Analizados

### 1. OrderServiceImpl
Servicio principal que maneja las operaciones CRUD de órdenes.

### 2. CartServiceImpl  
Servicio que gestiona los carritos de compra y su integración con el servicio de usuarios.

## Estrategia de Pruebas

### Convención de Nomenclatura
Todas las pruebas siguen el patrón: `MethodName_WhenCondition_ExpectedBehavior`

### Framework Utilizado
- **JUnit 5** - Framework principal de testing
- **Mockito** - Framework de mocking para dependencias
- **@ExtendWith(MockitoExtension.class)** - Integración Mockito-JUnit

## Detalle de Pruebas Implementadas

### OrderServiceImpl Tests

#### 1. `findAll_WhenOrdersExist_ShouldReturnOrderDtoList`
- **Propósito**: Validar que el método findAll() retorne correctamente una lista de OrderDto cuando existen órdenes
- **Escenario**: Repositorio contiene 2 órdenes
- **Validaciones**:
  - Lista no es nula
  - Contiene 2 elementos
  - Datos mapeados correctamente (descripción, precio)
  - Se invoca el repositorio una vez

#### 2. `findById_WhenOrderExists_ShouldReturnOrderDto`
- **Propósito**: Verificar que findById() retorne la orden correcta cuando existe
- **Escenario**: Orden con ID 1 existe en el repositorio
- **Validaciones**:
  - OrderDto retornado no es nulo
  - Todos los campos mapeados correctamente
  - Relación con Cart mapeada correctamente
  - Se invoca findById una vez

#### 3. `findById_WhenOrderNotExists_ShouldThrowOrderNotFoundException`
- **Propósito**: Validar manejo de errores cuando una orden no existe
- **Escenario**: ID 999 no existe en el repositorio
- **Validaciones**:
  - Se lanza OrderNotFoundException
  - Mensaje de error correcto
  - Se invoca el repositorio correctamente

#### 4. `save_WhenValidOrderDto_ShouldReturnSavedOrderDto`
- **Propósito**: Verificar guardado correcto de nuevas órdenes
- **Escenario**: OrderDto válido para guardar
- **Validaciones**:
  - OrderDto retornado con ID generado
  - Datos persistidos correctamente
  - Mapeo bidireccional funcional
  - Se invoca save del repositorio

#### 5. `deleteById_WhenOrderExists_ShouldDeleteOrder`
- **Propósito**: Validar eliminación correcta de órdenes
- **Escenario**: Orden existente para eliminar
- **Validaciones**:
  - No se lanzan excepciones
  - Se busca la orden antes de eliminar
  - Se invoca delete del repositorio

### CartServiceImpl Tests

#### 6. `findAll_WhenCartsExist_ShouldReturnCartDtoListWithUserData`
- **Propósito**: Validar recuperación de carritos con datos de usuario integrados
- **Escenario**: 2 carritos con diferentes usuarios
- **Validaciones**:
  - Lista de carritos correcta
  - Integración con servicio de usuarios
  - Datos de usuario mapeados correctamente
  - Invocaciones a RestTemplate correctas

#### 7. `findById_WhenCartExists_ShouldReturnCartDtoWithUserData`
- **Propósito**: Verificar búsqueda individual de carrito con datos de usuario
- **Escenario**: Carrito existente con usuario válido
- **Validaciones**:
  - CartDto retornado correctamente
  - Datos de usuario integrados
  - Mapeo completo de entidad a DTO

#### 8. `findById_WhenCartNotExists_ShouldThrowCartNotFoundException`
- **Propósito**: Validar manejo de errores para carritos inexistentes
- **Escenario**: ID de carrito no válido
- **Validaciones**:
  - CartNotFoundException lanzada
  - Mensaje de error apropiado
  - No se hacen llamadas innecesarias a servicios externos

#### 9. `save_WhenValidCartDto_ShouldReturnSavedCartDto`
- **Propósito**: Verificar guardado correcto de carritos
- **Escenario**: CartDto válido para persistir
- **Validaciones**:
  - CartDto retornado con ID generado
  - Datos correctamente mapeados
  - Invocación correcta al repositorio

#### 10. `deleteById_WhenValidCartId_ShouldCallRepositoryDelete`
- **Propósito**: Validar eliminación de carritos
- **Escenario**: ID de carrito válido
- **Validaciones**:
  - No se lanzan excepciones
  - Se invoca deleteById del repositorio

## Cobertura de Componentes

### Componentes Validados:
- ✅ **Servicios principales** (OrderServiceImpl, CartServiceImpl)
- ✅ **Mapeo de entidades** (OrderMappingHelper, CartMappingHelper)
- ✅ **Manejo de excepciones** (OrderNotFoundException, CartNotFoundException)
- ✅ **Integración entre servicios** (RestTemplate para usuarios)
- ✅ **Operaciones CRUD completas**

### Aspectos Críticos Cubiertos:
- **Funcionalidad básica**: CRUD operations
- **Manejo de errores**: Excepciones personalizadas
- **Integridad de datos**: Mapeo correcto entre capas
- **Integración de servicios**: Comunicación con otros microservicios
- **Validaciones de negocio**: Casos límite y escenarios de error

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
    
    <!-- Mockito ya incluido en spring-boot-starter-test -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Ejecución de Pruebas

### Comando Maven:
```bash
mvn test
```

### Comando específico para las clases:
```bash
mvn test -Dtest=OrderServiceImplTest
mvn test -Dtest=CartServiceImplTest
```

## Métricas Esperadas

### Cobertura de Código:
- **Servicios principales**: >90%
- **Métodos críticos**: 100%
- **Manejo de excepciones**: 100%

### Resultados Esperados:
- ✅ **10 pruebas unitarias** ejecutadas exitosamente
- ✅ **Tiempo de ejecución**: < 5 segundos
- ✅ **0 fallas** en ejecución normal
- ✅ **Validación completa** de componentes individuales

## Conclusiones

Las pruebas unitarias implementadas cubren los aspectos más críticos del microservicio Order Service:

1. **Funcionalidad principal** de los servicios más importantes
2. **Manejo robusto de errores** con excepciones personalizadas  
3. **Integración correcta** entre capas de la aplicación
4. **Validación de mapeo** entre entidades y DTOs
5. **Comunicación entre microservicios** mediante RestTemplate

Estas pruebas garantizan que los componentes individuales funcionen correctamente de forma aislada, proporcionando una base sólida para pruebas de integración posteriores.

## Próximos Pasos

Para completar la suite de pruebas:
1. Implementar pruebas de integración para validar comunicación entre servicios
2. Crear pruebas E2E para flujos completos de usuario
3. Desarrollar pruebas de rendimiento con Locust
4. Configurar pipelines CI/CD con estas pruebas como prerequisito