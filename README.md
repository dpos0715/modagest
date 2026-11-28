# ModaGest - Sistema de Gestión para Tienda de Ropa

Evidencia **GA7-220501096-AA2-EV01** - Codificación de módulos del software.
Programa: Análisis y Desarrollo de Software - SENA (Ficha 3336083).
Aprendiz: Kenan David Posada Neira. Instructor: Oscar Fernando Moreno Contreras.

## Descripción

Aplicación de escritorio en Java que gestiona el **inventario con variantes (talla y color)** de una tienda de ropa.
Se conecta a una base de datos MySQL mediante **JDBC** e implementa el **CRUD** completo de productos y variantes,
el registro de entradas de mercancía y las alertas de stock mínimo.

## Historias de usuario cubiertas

| Historia | Descripción | Dónde se implementa |
|---|---|---|
| HU-002 | Consulta de stock por talla y color | `PanelVariantes` (rol Cajero: solo consulta) |
| HU-003 | Registro de entrada de mercancía | `InventarioDAO.registrarEntrada` |
| HU-004 | Alerta de stock mínimo | `InventarioDAO.sincronizarAlerta` + botón "Ver stock bajo" |
| HU-005 | Gestión de variantes de producto | `VarianteDAO` + `PanelVariantes` |

Además: inicio de sesión con roles (Administrador, Cajero, Bodega) y CRUD de productos.
Los módulos de Ventas (HU-001) y Reporte de arqueo (HU-006) quedan para una siguiente iteración.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Interfaz | Swing |
| Acceso a datos | JDBC (`mysql-connector-j` 8.4.0) |
| Base de datos | MySQL (XAMPP) |
| Gestor de proyecto | Maven |
| IDE | Apache NetBeans |
| Versionamiento | Git y GitHub |

## Arquitectura en capas

```
com.modagest
├── modelo   -> clases con los datos (Producto, VarianteProducto, Usuario...)
├── dao      -> acceso a datos: todo el SQL y el uso de JDBC
├── util     -> ConexionBD (conexión JDBC) y pruebas por consola
└── vista    -> ventanas Swing (login, productos, variantes)
```

La vista nunca escribe SQL: llama a los DAO, y los DAO usan `ConexionBD` para hablar con MySQL.

## Requisitos previos

- JDK 21
- XAMPP con MySQL encendido (puerto 3306, usuario `root` sin contraseña)
- Apache NetBeans (o cualquier IDE con soporte Maven)

## Instalación y ejecución

1. Encender **MySQL** desde el panel de control de XAMPP.
2. Abrir `http://localhost/phpmyadmin`, ir a la pestaña **Importar** y cargar `database/modagest.sql`.
   El script crea la base de datos `modagest`, sus 15 tablas y los datos de prueba.
3. Abrir el proyecto en NetBeans (**File > Open Project**) y ejecutar **Clean and Build**
   para que Maven descargue el driver JDBC.
4. Ejecutar la clase `com.modagest.vista.LoginFrame` (clic derecho > Run File).

Si la configuración de MySQL es distinta, se cambia en `com.modagest.util.ConexionBD`
(constantes `URL_BASE_DATOS`, `USUARIO` y `CLAVE`).

## Usuarios de prueba

| Rol | Correo | Contraseña | Puede hacer |
|---|---|---|---|
| Administrador | admin@modagest.com | admin123 | Productos, variantes, entradas |
| Bodega | bodega@modagest.com | bodega123 | Productos, variantes, entradas |
| Cajero | cajero@modagest.com | cajero123 | Solo consultar stock |

Las contraseñas se guardan cifradas con SHA-256 en la base de datos.

## Cumplimiento de la lista de chequeo

| Indicador | Evidencia en el proyecto |
|---|---|
| Conexión con JDBC | `com.modagest.util.ConexionBD` usa `DriverManager.getConnection`; todos los DAO usan `PreparedStatement` |
| CRUD | `ProductoDAO` y `VarianteDAO`: `insertar` (C), `listar`/`buscarPor...` (R), `actualizar` (U), `eliminar` (D) |
| Herramientas de versionamiento | Repositorio Git alojado en GitHub, con un commit por cada avance |
| Estándar de codificación | Convenciones de Java descritas abajo |

## Estándar de codificación

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas | `com.modagest.dao` |
| Clases | PascalCase | `VarianteProducto`, `ProductoDAO` |
| Métodos | camelCase, empiezan con un verbo | `buscarPorCodigoBarras`, `registrarEntrada` |
| Variables y atributos | camelCase, nombres descriptivos | `stockMinimo`, `idVariante` |
| Constantes | MAYÚSCULAS con guion bajo | `URL_BASE_DATOS`, `SQL_LISTAR` |
| Componentes Swing | prefijo según el tipo | `txtNombre`, `btnGuardar`, `cboCategoria`, `tabla` |

Otras prácticas aplicadas:

- Atributos `private` con getters y setters.
- Consultas con `PreparedStatement` y parámetros `?` (evita inyección SQL).
- `try-with-resources` para cerrar conexiones automáticamente.
- Transacciones (`commit` / `rollback`) cuando una operación escribe en varias tablas.
- Comentarios Javadoc en las clases y los métodos públicos.
- Una responsabilidad por clase: modelo, acceso a datos o vista.

## Base de datos

El modelo está basado en el diseño lógico de la evidencia GA4-220501095-AA1-EV01:
15 tablas en Tercera Forma Normal. Tablas principales de este módulo:
`Producto`, `VarianteProducto` (talla, color y código de barras único), `Inventario`,
`EntradaInventario`, `AlertaStock`, `Proveedor`, `Usuario` y `Rol`.

## Pruebas por consola

Las clases `PruebaConexion`, `PruebaProductoDAO` y `PruebaVarianteDAO` ejecutan el flujo
completo del CRUD sin interfaz y muestran el resultado en la consola.
