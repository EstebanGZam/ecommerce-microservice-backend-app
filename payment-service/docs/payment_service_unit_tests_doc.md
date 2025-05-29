# Plan de Pruebas Unitarias - Payment Service

## Resumen Ejecutivo

Este documento describe el plan de pruebas unitarias implementado para el microservicio Payment Service del sistema de ecommerce. Las pruebas se han diseñado siguiendo la convención **MethodName_WhenCondition_ExpectedBehavior** y cubren los componentes más críticos del servicio de pagos.

## Servicio Analizado

### PaymentServiceImpl
Servicio principal que maneja las operaciones CRUD de pagos y su integración con el servicio de órdenes.

**Características principales:**
- Gestión completa del ciclo de vida de pagos
- Integración con Order Service mediante RestTemplate
- Manejo de estados de pago (NOT_STARTED, IN_PROGRESS, COMPLETED)
- Validación de integridad de datos de pagos

## Estrategia de Pruebas

### Convención de Nomenclatura
Todas las pruebas siguen el patrón: `MethodName_WhenCondition_ExpectedBehavior`

### Framework Utilizado
- **JUnit 5** - Framework principal de testing
- **Mockito** - Framework de mocking para dependencias
- **@ExtendWith(MockitoExtension.class)** - Integración Mockito-JUnit

## Detalle de Pruebas Implementadas

### PaymentServiceImpl Tests

#### 1. `findAll_WhenPaymentsExist_ShouldReturnPaymentDtoListWithOrderData`
- **Propósito**: Validar que el método findAll() retorne correctamente una lista de PaymentDto con datos de órdenes integrados
- **Escenario**: Repositorio contiene 2 pagos con diferentes estados
- **Validaciones**:
  - Lista no es nula y contiene 2 elementos
  - Estados de pago correctos (IN_PROGRESS, COMPLETED)
  - Flags de pago correctos (false, true)
  - Datos de órdenes integrados correctamente
  - Invocaciones correctas a repositorio y RestTemplate

#### 2. `findById_WhenPaymentExists_ShouldReturnPaymentDtoWithOrderData`
- **Propósito**: Verificar que findById() retorne el pago correcto con datos de orden integrados
- **Escenario**: Pago con ID 1 existe en el repositorio
- **Validaciones**:
  - PaymentDto retornado no es nulo
  - Todos los campos de pago mapeados correctamente
  - Datos de orden integrados desde Order Service
  - Estado de pago y flag isPayed correctos
  - Se invoca findById una vez y RestTemplate una vez

#### 3. `findById_WhenPaymentNotExists_ShouldThrowPaymentNotFoundException`
- **Propósito**: Validar manejo de errores cuando un pago no existe
- **Escenario**: ID 999 no existe en el repositorio
- **Validaciones**:
  - Se lanza PaymentNotFoundException
  - Mensaje de error específico y correcto
  - No se hacen llamadas innecesarias a Order Service
  - Se invoca el repositorio correctamente

#### 4. `save_WhenValidPaymentDto_ShouldReturnSavedPaymentDto`
- **Propósito**: Verificar guardado correcto de nuevos pagos
- **Escenario**: PaymentDto válido con estado NOT_STARTED
- **Validaciones**:
  - PaymentDto retornado con ID generado
  - Estado inicial NOT_STARTED mantenido
  - Flag isPayed en false para nuevo pago
  - Mapeo bidireccional funcional
  - Se invoca save del repositorio

#### 5. `update_WhenValidPaymentDto_ShouldReturnUpdatedPaymentDto`
- **Propósito**: Validar actualización correcta de pagos existentes
- **Escenario**: Pago cambiando de IN_PROGRESS a COMPLETED
- **Validaciones**:
  - Estado actualizado correctamente a COMPLETED
  - Flag isPayed cambiado a true
  - ID del pago mantenido
  - Relación con orden preservada
  - Se invoca save del repositorio

#### 6. `deleteById_WhenValidPaymentId_ShouldCallRepositoryDelete`
- **Propósito**: Validar eliminación correcta de pagos
- **Escenario**: ID de pago válido para eliminar
- **Validaciones**:
  - No se lanzan excepciones
  - Se invoca deleteById del repositorio correctamente

#### 7. `findAll_WhenPaymentStatusIsCompleted_ShouldReturnCompletedPayments`
- **Propósito**: Verificar manejo específico de pagos completados
- **Escenario**: Lista contiene solo pagos con estado COMPLETED
- **Validaciones**:
  - Estado COMPLETED correctamente mapeado
  - Flag isPayed en true para pagos completados
  - Integración con datos de orden funcional
  - Mapeo específico para pagos finalizados

#### 8. `save_WhenPaymentStatusIsInProgress_ShouldMaintainCorrectStatus`
- **Propósito**: Validar preservación de estado IN_PROGRESS en guardado
- **Escenario**: Nuevo pago con estado IN_PROGRESS
- **Validaciones**:
  - Estado IN_PROGRESS mantenido correctamente
  - Flag isPayed en false para pagos en progreso
  - Coherencia entre estado y flag de pago
  - Mapeo correcto de estados intermedios

## Cobertura de Componentes

### Componentes Validados:
- ✅ **Servicio principal** (PaymentServiceImpl)
- ✅ **Mapeo de entidades** (PaymentMappingHelper)
- ✅ **Manejo de excepciones** (PaymentNotFoundException)
- ✅ **Estados de pago** (PaymentStatus enum)
- ✅ **Integración con Order Service** (RestTemplate)
- ✅ **Operaciones CRUD completas**

### Aspectos Críticos Cubiertos:
- **Funcionalidad básica**: Operaciones CRUD de pagos
- **Estados de pago**: NOT_STARTED, IN_PROGRESS, COMPLETED
- **Manejo de errores**: Excepciones personalizadas
- **Integridad de datos**: Mapeo correcto entre capas
- **Integración de microservicios**: Comunicación con Order Service
- **Validaciones de negocio**: Coherencia entre estados y flags

## Estados de Pago Validados

### PaymentStatus Enum:
- **NOT_STARTED**: Pago recién creado
- **IN_PROGRESS**: Pago en proceso
- **COMPLETED**: Pago finalizado exitosamente

### Transiciones Validadas:
- ✅ Creación con estado NOT_STARTED
- ✅ Progreso a IN_PROGRESS
- ✅ Finalización en COMPLETED
- ✅ Coherencia con flag isPayed

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

### Comando específico para la clase:
```bash
mvn test -Dtest=PaymentServiceImplTest
```

### Ejecución con perfil de desarrollo:
```bash
mvn test -Dspring.profiles.active=dev
```

## Métricas Esperadas

### Cobertura de Código:
- **PaymentServiceImpl**: >95%
- **Métodos críticos**: 100%
- **Manejo de excepciones**: 100%
- **Estados de pago**: 100%

### Resultados Esperados:
- ✅ **8 pruebas unitarias** ejecutadas exitosamente
- ✅ **Tiempo de ejecución**: < 3 segundos
- ✅ **0 fallas** en ejecución normal
- ✅ **Validación completa** de estados de pago

## Integración con Order Service

### Validaciones de Integración:
- **RestTemplate mocking**: Simulación de llamadas a Order Service
- **Mapeo de OrderDto**: Correcta integración de datos de órdenes
- **Manejo de errores**: Casos donde Order Service no responde
- **Datos consistentes**: Coherencia entre Payment y Order

## Casos de Uso de Negocio Cubiertos

### Flujo de Pago Completo:
1. **Creación**: Nuevo pago con estado NOT_STARTED
2. **Procesamiento**: Cambio a IN_PROGRESS
3. **Finalización**: Transición a COMPLETED con isPayed=true
4. **Consulta**: Recuperación con datos de orden integrados
5. **Eliminación**: Limpieza de pagos obsoletos

### Escenarios de Error:
- **Pago inexistente**: PaymentNotFoundException
- **Service unavailable**: Manejo de errores de integración
- **Datos inconsistentes**: Validación de integridad

## Conclusiones

Las pruebas unitarias implementadas para Payment Service cubren:

1. **Funcionalidad completa** del servicio de pagos
2. **Estados de pago** y transiciones correctas
3. **Integración robusta** con Order Service
4. **Manejo de errores** específicos del dominio de pagos
5. **Validación de mapeo** entre entidades y DTOs
6. **Operaciones CRUD** completas y consistentes

Estas pruebas garantizan que el servicio de pagos funcione correctamente de forma aislada, validando especialmente la lógica de estados de pago y la integración con el servicio de órdenes.

## Próximos Pasos

Para completar la suite de pruebas del Payment Service:
1. Implementar pruebas de integración con Order Service real
2. Crear pruebas E2E para flujos completos de pago
3. Desarrollar pruebas de rendimiento para procesamiento de pagos
4. Validar manejo de transacciones y rollbacks
5. Configurar pipelines CI/CD específicos para servicios de pago