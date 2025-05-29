# Plan de Pruebas Unitarias - Favourite Service

## Información General

- **Servicio**: favourite-service
- **Clase bajo prueba**: `FavouriteServiceImpl`
- **Framework de pruebas**: JUnit 5 + Mockito
- **Patrón de nomenclatura**: `MethodName_WhenCondition_ExpectedBehavior`

## Objetivos de las Pruebas

Las pruebas unitarias tienen como objetivo validar la lógica de negocio del servicio de favoritos de manera aislada, garantizando que:

1. Los métodos principales del servicio funcionan correctamente
2. Se manejan apropiadamente los casos de error
3. Las interacciones con dependencias externas son correctas
4. Los datos se transforman y mapean correctamente

## Cobertura de Pruebas

### 1. **findAll_WhenFavouritesExist_ShouldReturnListWithExternalData**

**Propósito**: Validar que el método `findAll()` recupera correctamente todos los favoritos y enriquece los datos con información externa.

**Escenario**:
- El repositorio contiene favoritos
- Los servicios externos (user-service y product-service) responden correctamente

**Validaciones**:
- Se retorna una lista no nula
- El tamaño de la lista es correcto
- Los datos básicos del favorito son correctos
- Los datos externos (usuario y producto) están presentes
- Se realizan las llamadas correctas al repositorio y servicios externos

**Componentes Validados**:
- Lógica de recuperación de datos
- Mapeo de entidades a DTOs
- Integración con servicios externos via RestTemplate

---

### 2. **findById_WhenFavouriteExists_ShouldReturnFavouriteWithExternalData**

**Propósito**: Validar la recuperación de un favorito específico por ID incluyendo datos externos.

**Escenario**:
- Existe un favorito con el ID proporcionado
- Los servicios externos responden correctamente

**Validaciones**:
- Se retorna el favorito correcto
- Todos los campos están correctamente mapeados
- Los datos externos están presentes y son correctos
- Se realizan las llamadas correctas a las dependencias

**Componentes Validados**:
- Búsqueda por ID compuesto
- Mapeo de datos
- Enriquecimiento con datos externos

---

### 3. **findById_WhenFavouriteNotExists_ShouldThrowFavouriteNotFoundException**

**Propósito**: Validar el manejo de errores cuando se busca un favorito que no existe.

**Escenario**:
- No existe un favorito con el ID proporcionado

**Validaciones**:
- Se lanza la excepción `FavouriteNotFoundException`
- El mensaje de error contiene información relevante
- No se realizan llamadas innecesarias a servicios externos

**Componentes Validados**:
- Manejo de casos excepcionales
- Propagación correcta de errores
- Optimización de llamadas externas

---

### 4. **save_WhenValidFavouriteDto_ShouldReturnSavedFavouriteDto**

**Propósito**: Validar la funcionalidad de guardado de nuevos favoritos.

**Escenario**:
- Se proporciona un FavouriteDto válido para guardar

**Validaciones**:
- El favorito se guarda correctamente
- Los datos retornados coinciden con los enviados
- Se realiza la llamada correcta al repositorio

**Componentes Validados**:
- Lógica de persistencia
- Mapeo DTO → Entity → DTO
- Interacción con el repositorio

---

### 5. **deleteById_WhenValidFavouriteId_ShouldCallRepositoryDelete**

**Propósito**: Validar la funcionalidad de eliminación de favoritos.

**Escenario**:
- Se proporciona un ID válido para eliminar

**Validaciones**:
- Se realiza la llamada correcta al método de eliminación del repositorio
- El ID se pasa correctamente

**Componentes Validados**:
- Lógica de eliminación
- Delegación correcta al repositorio

## Configuración de Pruebas

### Dependencias Mock
- `FavouriteRepository`: Simulación de la capa de persistencia
- `RestTemplate`: Simulación de llamadas a servicios externos

### Datos de Prueba
- Usuario de prueba: ID 1, nombre "John Doe"
- Producto de prueba: ID 1, título "Test Product"
- Fecha de "like": 15 de enero de 2024, 10:30:00

### Annotations Utilizadas
- `@ExtendWith(MockitoExtension.class)`: Habilita Mockito
- `@Mock`: Crea mocks de las dependencias
- `@InjectMocks`: Inyecta los mocks en la clase bajo prueba

## Estrategia de Mocking

1. **Repositorio**: Se mockea para controlar los datos retornados y verificar interacciones
2. **RestTemplate**: Se mockea para simular respuestas de servicios externos sin realizar llamadas reales
3. **Datos de entrada**: Se crean objetos de prueba consistentes para todos los casos

## Métricas de Cobertura Esperadas

- **Cobertura de líneas**: >90% de las líneas del servicio
- **Cobertura de métodos**: 100% de los métodos públicos principales
- **Cobertura de ramas**: >85% de las decisiones lógicas

## Casos de Borde Considerados

1. **Repositorio vacío**: Lista vacía de favoritos
2. **Entidad no encontrada**: ID inexistente
3. **Servicios externos**: Respuestas nulas o errores (pueden expandirse en pruebas futuras)

## Beneficios de las Pruebas Implementadas

1. **Confiabilidad**: Garantizan que la lógica central funciona correctamente
2. **Regresión**: Detectan cambios que rompan funcionalidad existente
3. **Documentación**: Sirven como documentación viva del comportamiento esperado
4. **Refactoring**: Permiten modificar implementación manteniendo funcionalidad
5. **Calidad**: Mejoran la calidad general del código

## Recomendaciones para Expansión

Para futuras iteraciones del plan de pruebas, se recomienda:

1. **Pruebas de integración**: Para validar la interacción real con servicios externos
2. **Pruebas de rendimiento**: Para validar comportamiento bajo carga
3. **Pruebas de casos edge**: Manejo de datos nulos, formatos inválidos, etc.
4. **Pruebas de transacciones**: Comportamiento en escenarios de rollback
5. **Pruebas de concurrencia**: Validar thread-safety si es aplicable