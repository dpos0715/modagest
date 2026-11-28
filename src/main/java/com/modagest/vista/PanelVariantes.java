package com.modagest.vista;

import com.modagest.dao.InventarioDAO;
import com.modagest.dao.ProductoDAO;
import com.modagest.dao.ProveedorDAO;
import com.modagest.dao.VarianteDAO;
import com.modagest.modelo.Producto;
import com.modagest.modelo.Proveedor;
import com.modagest.modelo.Usuario;
import com.modagest.modelo.VarianteProducto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Pantalla de variantes (talla + color + codigo de barras) e inventario.
 * - Administrador y Bodega: pueden crear, actualizar, eliminar y registrar entradas.
 * - Cajero: solo consulta el stock (el formulario no se muestra).
 */
public class PanelVariantes extends JPanel {

    private static final String TEXTO_TODOS = "Todos los productos";
    private static final int COLUMNA_ESTADO = 8;

    private final Usuario usuario;
    private final VarianteDAO varianteDAO = new VarianteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Producto", "Talla", "Color", "Código de barras",
                "Stock", "Mínimo", "Ubicación", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    // Filtro superior: el primer elemento es el texto "Todos", los demas son Producto
    private final JComboBox<Object> cboFiltro = new JComboBox<>();

    // Campos del formulario
    private final JComboBox<Producto> cboProducto = new JComboBox<>();
    private final JTextField txtTalla = new JTextField(8);
    private final JTextField txtColor = new JTextField(12);
    private final JTextField txtCodigo = new JTextField(16);
    private final JTextField txtCantidad = new JTextField(6);
    private final JTextField txtStockMinimo = new JTextField("10", 6);
    private final JTextField txtUbicacion = new JTextField(12);

    private List<VarianteProducto> variantes = new ArrayList<>();
    private int idVarianteSeleccionada = 0;
    private boolean soloStockBajo = false;
    // Evita que el filtro recargue la tabla mientras se estan llenando las listas
    private boolean cargandoCombos = false;

    public PanelVariantes(Usuario usuario, boolean puedeGestionar) {
        this.usuario = usuario;

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarSeleccionEnFormulario();
            }
        });
        configurarColorEstado();

        add(crearBarraFiltro(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        if (puedeGestionar) {
            add(crearPanelFormulario(), BorderLayout.SOUTH);
        }

        recargarProductos();
    }

    // ------------------------------------------------------------------
    // Construccion de la interfaz
    // ------------------------------------------------------------------
    private JPanel crearBarraFiltro() {
        JButton btnStockBajo = new JButton("Ver stock bajo");
        JButton btnTodas = new JButton("Ver todas");

        cboFiltro.addActionListener(e -> {
            if (!cargandoCombos) {
                soloStockBajo = false;
                cargarTabla();
            }
        });
        btnStockBajo.addActionListener(e -> {
            soloStockBajo = true;
            cargarTabla();
        });
        btnTodas.addActionListener(e -> {
            soloStockBajo = false;
            cargandoCombos = true;
            cboFiltro.setSelectedIndex(0);
            cargandoCombos = false;
            cargarTabla();
        });

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.add(new JLabel("Filtrar por producto:"));
        barra.add(cboFiltro);
        barra.add(btnStockBajo);
        barra.add(btnTodas);
        return barra;
    }

    private JPanel crearPanelFormulario() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(BorderFactory.createTitledBorder("Datos de la variante"));
        UtilVista.agregarCampo(campos, "Producto:", cboProducto, 0, 0);
        UtilVista.agregarCampo(campos, "Talla:", txtTalla, 1, 0);
        UtilVista.agregarCampo(campos, "Color:", txtColor, 0, 1);
        UtilVista.agregarCampo(campos, "Código de barras:", txtCodigo, 1, 1);
        UtilVista.agregarCampo(campos, "Cantidad inicial:", txtCantidad, 0, 2);
        UtilVista.agregarCampo(campos, "Stock mínimo:", txtStockMinimo, 1, 2);
        UtilVista.agregarCampo(campos, "Ubicación:", txtUbicacion, 0, 3);

        JButton btnCrear = new JButton("Crear variante");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnEntrada = new JButton("Registrar entrada");
        JButton btnLimpiar = new JButton("Limpiar");

        btnCrear.addActionListener(e -> crearVariante());
        btnActualizar.addActionListener(e -> actualizarVariante());
        btnEliminar.addActionListener(e -> eliminarVariante());
        btnEntrada.addActionListener(e -> registrarEntrada());
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        botones.add(btnCrear);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnEntrada);
        botones.add(btnLimpiar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(campos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    /** Pinta el estado en verde (Disponible) o rojo (Requerido Pedido / Agotado). */
    private void configurarColorEstado() {
        tabla.getColumnModel().getColumn(COLUMNA_ESTADO).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tablaOrigen, Object valor,
                    boolean seleccionada, boolean conFoco, int fila, int columna) {
                Component celda = super.getTableCellRendererComponent(
                        tablaOrigen, valor, seleccionada, conFoco, fila, columna);
                if (seleccionada) {
                    celda.setForeground(tablaOrigen.getSelectionForeground());
                } else if ("Disponible".equals(String.valueOf(valor))) {
                    celda.setForeground(new Color(0x059669)); // verde esmeralda
                } else {
                    celda.setForeground(new Color(0x9B1C1C)); // rojo borgoña
                }
                return celda;
            }
        });
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------

    /**
     * Vuelve a llenar las listas de productos y la tabla.
     * PrincipalFrame lo llama cada vez que se abre esta pestana,
     * para ver los productos que se acaban de crear.
     */
    public void recargarProductos() {
        cargandoCombos = true;
        try {
            List<Producto> productos = productoDAO.listar();
            cboFiltro.removeAllItems();
            cboFiltro.addItem(TEXTO_TODOS);
            cboProducto.removeAllItems();
            for (Producto producto : productos) {
                cboFiltro.addItem(producto);
                cboProducto.addItem(producto);
            }
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        } finally {
            cargandoCombos = false;
        }
        soloStockBajo = false;
        cargarTabla();
        limpiar();
    }

    /** R - Llena la tabla segun el filtro: todas, por producto o solo stock bajo. */
    private void cargarTabla() {
        try {
            if (soloStockBajo) {
                variantes = varianteDAO.listarStockBajo();
            } else {
                Object filtro = cboFiltro.getSelectedItem();
                if (filtro instanceof Producto) {
                    variantes = varianteDAO.listarPorProducto(((Producto) filtro).getIdProducto());
                } else {
                    variantes = varianteDAO.listar();
                }
            }

            modeloTabla.setRowCount(0);
            for (VarianteProducto variante : variantes) {
                modeloTabla.addRow(new Object[]{
                    variante.getIdVariante(),
                    variante.getNombreProducto(),
                    variante.getTalla(),
                    variante.getColor(),
                    variante.getCodigoBarras(),
                    variante.getCantidad(),
                    variante.getStockMinimo(),
                    variante.getUbicacion(),
                    variante.getEstado()
                });
            }
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    private void mostrarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        VarianteProducto variante = variantes.get(fila);
        idVarianteSeleccionada = variante.getIdVariante();

        seleccionarProducto(variante.getIdProducto());
        txtTalla.setText(variante.getTalla());
        txtColor.setText(variante.getColor());
        txtCodigo.setText(variante.getCodigoBarras());
        txtCantidad.setText(String.valueOf(variante.getCantidad()));
        txtStockMinimo.setText(String.valueOf(variante.getStockMinimo()));
        txtUbicacion.setText(variante.getUbicacion());

        // Al editar no se cambia el producto ni la cantidad:
        // la cantidad solo se modifica con entradas de mercancia y ventas
        cboProducto.setEnabled(false);
        txtCantidad.setEnabled(false);
    }

    private void seleccionarProducto(int idProducto) {
        for (int i = 0; i < cboProducto.getItemCount(); i++) {
            if (cboProducto.getItemAt(i).getIdProducto() == idProducto) {
                cboProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Acciones de los botones
    // ------------------------------------------------------------------

    /** C - Crea una variante nueva junto con su inventario inicial. */
    private void crearVariante() {
        if (idVarianteSeleccionada != 0) {
            UtilVista.aviso(this, "Hay una variante seleccionada. Pulse «Limpiar» para crear una nueva.");
            return;
        }
        Producto producto = (Producto) cboProducto.getSelectedItem();
        if (producto == null) {
            UtilVista.aviso(this, "Primero registre un producto en la pestaña Productos.");
            return;
        }

        String talla = txtTalla.getText().trim();
        String color = txtColor.getText().trim();
        String codigo = txtCodigo.getText().trim();
        if (talla.isEmpty() || color.isEmpty() || codigo.isEmpty()) {
            UtilVista.aviso(this, "Talla, color y código de barras son obligatorios.");
            return;
        }

        Integer cantidad = leerEntero(txtCantidad, "La cantidad inicial");
        if (cantidad == null) {
            return;
        }
        Integer stockMinimo = leerEntero(txtStockMinimo, "El stock mínimo");
        if (stockMinimo == null) {
            return;
        }

        VarianteProducto variante = new VarianteProducto(producto.getIdProducto(), talla, color, codigo);
        try {
            varianteDAO.insertar(variante, cantidad, stockMinimo, txtUbicacion.getText().trim());
            UtilVista.info(this, "Variante creada con id " + variante.getIdVariante() + ".");
            cargarTabla();
            limpiar();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** U - Actualiza talla, color, codigo, stock minimo y ubicacion. */
    private void actualizarVariante() {
        if (idVarianteSeleccionada == 0) {
            UtilVista.aviso(this, "Seleccione una variante de la tabla.");
            return;
        }
        String talla = txtTalla.getText().trim();
        String color = txtColor.getText().trim();
        String codigo = txtCodigo.getText().trim();
        if (talla.isEmpty() || color.isEmpty() || codigo.isEmpty()) {
            UtilVista.aviso(this, "Talla, color y código de barras son obligatorios.");
            return;
        }
        Integer stockMinimo = leerEntero(txtStockMinimo, "El stock mínimo");
        if (stockMinimo == null) {
            return;
        }

        VarianteProducto variante = new VarianteProducto();
        variante.setIdVariante(idVarianteSeleccionada);
        variante.setTalla(talla);
        variante.setColor(color);
        variante.setCodigoBarras(codigo);
        variante.setStockMinimo(stockMinimo);
        variante.setUbicacion(txtUbicacion.getText().trim());

        try {
            if (varianteDAO.actualizar(variante)) {
                UtilVista.info(this, "Variante actualizada.");
            }
            cargarTabla();
            limpiar();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** D - Elimina la variante seleccionada (con confirmacion). */
    private void eliminarVariante() {
        if (idVarianteSeleccionada == 0) {
            UtilVista.aviso(this, "Seleccione una variante de la tabla.");
            return;
        }
        if (!UtilVista.confirmar(this, "¿Eliminar la variante seleccionada y su inventario?")) {
            return;
        }
        try {
            varianteDAO.eliminar(idVarianteSeleccionada);
            UtilVista.info(this, "Variante eliminada.");
            cargarTabla();
            limpiar();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** HU-003 - Registra la entrada de mercancia de la variante seleccionada. */
    private void registrarEntrada() {
        if (idVarianteSeleccionada == 0) {
            UtilVista.aviso(this, "Seleccione una variante de la tabla.");
            return;
        }

        List<Proveedor> proveedores;
        try {
            proveedores = proveedorDAO.listar();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
            return;
        }
        if (proveedores.isEmpty()) {
            UtilVista.aviso(this, "No hay proveedores registrados.");
            return;
        }

        // Cuadro de dialogo con cantidad y proveedor
        JTextField txtCantidadEntrada = new JTextField(8);
        JComboBox<Proveedor> cboProveedor = new JComboBox<>(proveedores.toArray(new Proveedor[0]));
        JPanel panel = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(panel, "Cantidad recibida:", txtCantidadEntrada, 0, 0);
        UtilVista.agregarCampo(panel, "Proveedor:", cboProveedor, 0, 1);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Registrar entrada de mercancía",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidadEntrada.getText().trim());
        } catch (NumberFormatException e) {
            UtilVista.aviso(this, "La cantidad debe ser un número entero.");
            return;
        }
        if (cantidad <= 0) {
            UtilVista.aviso(this, "La cantidad debe ser mayor a cero.");
            return;
        }

        Proveedor proveedor = (Proveedor) cboProveedor.getSelectedItem();
        try {
            inventarioDAO.registrarEntrada(
                    idVarianteSeleccionada, cantidad, proveedor.getIdProveedor(), usuario.getIdUsuario());
            UtilVista.info(this, "Entrada registrada. El stock y las alertas se actualizaron.");
            cargarTabla();
            limpiar();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        idVarianteSeleccionada = 0;
        cboProducto.setEnabled(true);
        txtCantidad.setEnabled(true);
        if (cboProducto.getItemCount() > 0) {
            cboProducto.setSelectedIndex(0);
        }
        txtTalla.setText("");
        txtColor.setText("");
        txtCodigo.setText("");
        txtCantidad.setText("");
        txtStockMinimo.setText("10");
        txtUbicacion.setText("");
    }

    /**
     * Lee un entero no negativo de un campo.
     *
     * @return el numero, o null si es invalido (ya se aviso al usuario)
     */
    private Integer leerEntero(JTextField campo, String nombreCampo) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor < 0) {
                UtilVista.aviso(this, nombreCampo + " no puede ser negativo.");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            UtilVista.aviso(this, nombreCampo + " debe ser un número entero.");
            return null;
        }
    }
}