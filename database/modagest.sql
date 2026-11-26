-- =====================================================================
-- ModaGest - Sistema de Gestion para Tienda de Ropa
-- Script de base de datos (MySQL / MariaDB)
-- Evidencia: GA7-220501096-AA2-EV01
-- Autor: Kenan David Posada Neira
-- Basado en el modelo logico GA4-220501095-AA1-EV01
-- =====================================================================

-- 1. Crear la base de datos (si ya existe, se borra para empezar limpio)
DROP DATABASE IF EXISTS modagest;
CREATE DATABASE modagest
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE modagest;

-- =====================================================================
-- 2. TABLAS DE SEGURIDAD (roles y usuarios)
-- =====================================================================
CREATE TABLE Rol (
    idRol       INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200)
) ENGINE=InnoDB;

CREATE TABLE Usuario (
    idUsuario  INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    apellido   VARCHAR(100) NOT NULL,
    correo     VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,          -- se guarda cifrada (hash), nunca en texto plano
    estado     BOOLEAN NOT NULL DEFAULT TRUE,  -- TRUE = activo
    idRol      INT NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (idRol) REFERENCES Rol(idRol)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. CATALOGO E INVENTARIO (HU-003, HU-004, HU-005)
-- =====================================================================
CREATE TABLE Categoria (
    idCategoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE Producto (
    idProducto  INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    idCategoria INT NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (idCategoria) REFERENCES Categoria(idCategoria)
) ENGINE=InnoDB;

-- Una variante = una combinacion talla + color con su propio codigo de barras
CREATE TABLE VarianteProducto (
    idVariante   INT AUTO_INCREMENT PRIMARY KEY,
    idProducto   INT NOT NULL,
    talla        VARCHAR(10) NOT NULL,
    color        VARCHAR(30) NOT NULL,
    codigoBarras VARCHAR(50) NOT NULL UNIQUE,
    -- Si se elimina el producto, se eliminan sus variantes
    CONSTRAINT fk_variante_producto FOREIGN KEY (idProducto)
        REFERENCES Producto(idProducto) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Inventario (
    idInventario INT AUTO_INCREMENT PRIMARY KEY,
    idVariante   INT NOT NULL UNIQUE,           -- una variante tiene un solo registro de inventario
    cantidad     INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    stockMinimo  INT NOT NULL DEFAULT 10,       -- umbral para la alerta (HU-004)
    ubicacion    VARCHAR(100),
    CONSTRAINT fk_inventario_variante FOREIGN KEY (idVariante)
        REFERENCES VarianteProducto(idVariante) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Proveedor (
    idProveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    telefono    VARCHAR(20),
    correo      VARCHAR(100),
    direccion   VARCHAR(150)
) ENGINE=InnoDB;

CREATE TABLE EntradaInventario (
    idEntrada   INT AUTO_INCREMENT PRIMARY KEY,
    fecha       DATE NOT NULL,
    cantidad    INT NOT NULL CHECK (cantidad > 0),
    idProveedor INT NOT NULL,
    idVariante  INT NOT NULL,
    idUsuario   INT NOT NULL,
    CONSTRAINT fk_entrada_proveedor FOREIGN KEY (idProveedor) REFERENCES Proveedor(idProveedor),
    CONSTRAINT fk_entrada_variante  FOREIGN KEY (idVariante)  REFERENCES VarianteProducto(idVariante),
    CONSTRAINT fk_entrada_usuario   FOREIGN KEY (idUsuario)   REFERENCES Usuario(idUsuario)
) ENGINE=InnoDB;

CREATE TABLE AlertaStock (
    idAlerta     INT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATE NOT NULL,
    estado       VARCHAR(30) NOT NULL DEFAULT 'Requerido Pedido',
    idInventario INT NOT NULL,
    CONSTRAINT fk_alerta_inventario FOREIGN KEY (idInventario)
        REFERENCES Inventario(idInventario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 4. VENTAS (HU-001, HU-002, HU-006)
-- =====================================================================
CREATE TABLE Cliente (
    idCliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellido  VARCHAR(100),
    documento VARCHAR(20),
    telefono  VARCHAR(20),
    correo    VARCHAR(100)
) ENGINE=InnoDB;

CREATE TABLE Venta (
    idVenta   INT AUTO_INCREMENT PRIMARY KEY,
    fecha     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total     DECIMAL(10,2) NOT NULL,
    idCliente INT NULL,                          -- puede ser venta sin cliente registrado
    idUsuario INT NOT NULL,                      -- el cajero que la registro
    CONSTRAINT fk_venta_cliente FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario)
) ENGINE=InnoDB;

CREATE TABLE DetalleVenta (
    idDetalle      INT AUTO_INCREMENT PRIMARY KEY,
    idVenta        INT NOT NULL,
    idVariante     INT NOT NULL,
    cantidad       INT NOT NULL CHECK (cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (idVenta)    REFERENCES Venta(idVenta) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_variante FOREIGN KEY (idVariante) REFERENCES VarianteProducto(idVariante)
) ENGINE=InnoDB;

CREATE TABLE Pago (
    idPago  INT AUTO_INCREMENT PRIMARY KEY,
    metodo  VARCHAR(30) NOT NULL,                -- Efectivo, Tarjeta, Bono
    valor   DECIMAL(10,2) NOT NULL,
    idVenta INT NOT NULL,
    CONSTRAINT fk_pago_venta FOREIGN KEY (idVenta) REFERENCES Venta(idVenta) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Devolucion (
    idDevolucion INT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATE NOT NULL,
    motivo       TEXT,
    idVenta      INT NOT NULL,
    CONSTRAINT fk_devolucion_venta FOREIGN KEY (idVenta) REFERENCES Venta(idVenta)
) ENGINE=InnoDB;

CREATE TABLE Reporte (
    idReporte INT AUTO_INCREMENT PRIMARY KEY,
    tipo      VARCHAR(50) NOT NULL,
    fecha     DATE NOT NULL,
    idUsuario INT NOT NULL,
    CONSTRAINT fk_reporte_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. DATOS DE PRUEBA
-- =====================================================================

-- Roles
INSERT INTO Rol (nombre, descripcion) VALUES
    ('Administrador', 'Supervisa operaciones, genera reportes y configura el sistema'),
    ('Cajero',        'Registra ventas y consulta stock'),
    ('Bodega',        'Gestiona inventario, variantes y entradas de mercancia');

-- Usuarios (contrasenas cifradas con SHA-256 usando la funcion SHA2 de MySQL)
--   admin@modagest.com  -> admin123
--   cajero@modagest.com -> cajero123
--   bodega@modagest.com -> bodega123
INSERT INTO Usuario (nombre, apellido, correo, contrasena, estado, idRol) VALUES
    ('Kenan',  'Posada', 'admin@modagest.com',  SHA2('admin123', 256),  TRUE, 1),
    ('Carlos', 'Perez',  'cajero@modagest.com', SHA2('cajero123', 256), TRUE, 2),
    ('Laura',  'Gomez',  'bodega@modagest.com', SHA2('bodega123', 256), TRUE, 3);

-- Categorias
INSERT INTO Categoria (nombre, descripcion) VALUES
    ('Camisas',    'Camisas y camisetas'),
    ('Pantalones', 'Jeans y pantalones'),
    ('Chaquetas',  'Chaquetas y abrigos'),
    ('Accesorios', 'Cinturones, gorras y otros');

-- Proveedores
INSERT INTO Proveedor (nombre, telefono, correo, direccion) VALUES
    ('Textiles del Norte S.A.S.', '3101234567', 'ventas@textilesnorte.com', 'Calle 80 # 20-15, Bogota'),
    ('Confecciones Andinas',      '3209876543', 'pedidos@andinas.com',      'Carrera 7 # 45-10, Bogota');

-- Cliente de prueba
INSERT INTO Cliente (nombre, apellido, documento, telefono, correo) VALUES
    ('Maria', 'Rodriguez', '1012345678', '3001112233', 'maria@correo.com');

-- Productos
INSERT INTO Producto (nombre, descripcion, precio, idCategoria) VALUES
    ('Camiseta Algodon Modelo X', 'Camiseta 100% algodon cuello redondo', 45000.00, 1),
    ('Jean Slim Fit',             'Jean corte slim, tela elastica',       120000.00, 2),
    ('Chaqueta Cortavientos',     'Chaqueta ligera impermeable',          150000.00, 3);

-- Variantes (talla + color + codigo de barras unico)
INSERT INTO VarianteProducto (idProducto, talla, color, codigoBarras) VALUES
    (1, 'M',  'Rojo',  '7701000000011'),
    (1, 'L',  'Rojo',  '7701000000012'),
    (1, 'M',  'Azul',  '7701000000013'),
    (2, '32', 'Azul',  '7701000000021'),
    (2, '34', 'Negro', '7701000000022'),
    (3, 'M',  'Negro', '7701000000031');

-- Inventario inicial (algunas variantes quedan bajo el minimo para probar la alerta)
INSERT INTO Inventario (idVariante, cantidad, stockMinimo, ubicacion) VALUES
    (1, 25, 10, 'Tienda'),
    (2,  8, 10, 'Bodega'),   -- stock bajo
    (3, 15, 10, 'Tienda'),
    (4,  5, 10, 'Bodega'),   -- stock bajo
    (5, 30, 10, 'Tienda'),
    (6, 12, 10, 'Bodega');

-- Entradas de mercancia de ejemplo
INSERT INTO EntradaInventario (fecha, cantidad, idProveedor, idVariante, idUsuario) VALUES
    (CURDATE(), 25, 1, 1, 3),
    (CURDATE(), 30, 2, 5, 3);

-- Alertas de stock para las variantes que ya estan bajo el minimo
INSERT INTO AlertaStock (fecha, estado, idInventario) VALUES
    (CURDATE(), 'Requerido Pedido', 2),
    (CURDATE(), 'Requerido Pedido', 4);

-- =====================================================================
-- 6. VERIFICACION RAPIDA (opcional: ejecutar despues de importar)
-- =====================================================================
-- SELECT p.nombre, v.talla, v.color, v.codigoBarras, i.cantidad, i.stockMinimo
-- FROM VarianteProducto v
-- JOIN Producto p   ON p.idProducto = v.idProducto
-- JOIN Inventario i ON i.idVariante = v.idVariante;
