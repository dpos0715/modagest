package com.modagest.vista;

import com.modagest.dao.CategoriaDAO;
import com.modagest.dao.ProductoDAO;
import com.modagest.modelo.Categoria;
import com.modagest.modelo.Producto;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Pantalla de gestion de productos: tabla con los productos y
 * formulario para crear, actualizar y eliminar.
 */
public class PanelProductos extends JPanel {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // Modelo de la tabla; se bloquea la edicion directa de las celdas
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Descripción", "Precio", "Categoría"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtPrecio = new JTextField(10);
    private final JComboBox<Categoria> cboCategoria = new JComboBox<>();

    // Productos que se muestran en la tabla (misma posicion que las filas)
    private List<Producto> productos = new ArrayList<>();
    // Id del producto seleccionado; 0 significa que no hay ninguno
    private int idProductoSeleccionado = 0;

    public PanelProductos() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Al pulsar una fila, sus datos pasan al formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarSeleccionEnFormulario();
            }
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearPanelFormulario(), BorderLayout.SOUTH);

        cargarCategorias();
        cargarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(BorderFactory.createTitledBorder("Datos del producto"));
        UtilVista.agregarCampo(campos, "Nombre:", txtNombre, 0, 0);
        UtilVista.agregarCampo(campos, "Descripción:", txtDescripcion, 1, 0);
        UtilVista.agregarCampo(campos, "Precio:", txtPrecio, 0, 1);
        UtilVista.agregarCampo(campos, "Categoría:", cboCategoria, 1, 1);

        JButton btnGuardar = new JButton("Guardar nuevo");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(campos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------
    private void cargarCategorias() {
        try {
            for (Categoria categoria : categoriaDAO.listar()) {
                cboCategoria.addItem(categoria);
            }
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** R - Lee los productos de la base de datos y llena la tabla. */
    private void cargarTabla() {
        try {
            productos = productoDAO.listar();
            modeloTabla.setRowCount(0);
            for (Producto producto : productos) {
                modeloTabla.addRow(new Object[]{
                    producto.getIdProducto(),
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio().toPlainString(),
                    producto.getNombreCategoria()
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
        Producto producto = productos.get(fila);
        idProductoSeleccionado = producto.getIdProducto();
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecio.setText(producto.getPrecio().toPlainString());
        seleccionarCategoria(producto.getIdCategoria());
    }

    private void seleccionarCategoria(int idCategoria) {
        for (int i = 0; i < cboCategoria.getItemCount(); i++) {
            if (cboCategoria.getItemAt(i).getIdCategoria() == idCategoria) {
                cboCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Acciones de los botones (CRUD)
    // ------------------------------------------------------------------

    /** C - Inserta un producto nuevo. */
    private void guardar() {
        if (idProductoSeleccionado != 0) {
            UtilVista.aviso(this, "Hay un producto seleccionado. Pulse «Limpiar» para crear uno nuevo.");
            return;
        }
        Producto producto = leerFormulario();
        if (producto == null) {
            return;
        }
        try {
            productoDAO.insertar(producto);
            UtilVista.info(this, "Producto guardado con id " + producto.getIdProducto() + ".");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** U - Actualiza el producto seleccionado. */
    private void actualizar() {
        if (idProductoSeleccionado == 0) {
            UtilVista.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        Producto producto = leerFormulario();
        if (producto == null) {
            return;
        }
        producto.setIdProducto(idProductoSeleccionado);
        try {
            if (productoDAO.actualizar(producto)) {
                UtilVista.info(this, "Producto actualizado.");
            }
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    /** D - Elimina el producto seleccionado (con confirmacion). */
    private void eliminar() {
        if (idProductoSeleccionado == 0) {
            UtilVista.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        if (!UtilVista.confirmar(this, "¿Eliminar el producto seleccionado y todas sus variantes?")) {
            return;
        }
        try {
            productoDAO.eliminar(idProductoSeleccionado);
            UtilVista.info(this, "Producto eliminado.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            UtilVista.errorBaseDatos(this, ex);
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        idProductoSeleccionado = 0;
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        if (cboCategoria.getItemCount() > 0) {
            cboCategoria.setSelectedIndex(0);
        }
        txtNombre.requestFocus();
    }

    /**
     * Lee y valida el formulario.
     *
     * @return el producto, o null si hay un dato invalido (ya se aviso al usuario)
     */
    private Producto leerFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            UtilVista.aviso(this, "El nombre es obligatorio.");
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            UtilVista.aviso(this, "El precio debe ser un número, por ejemplo 45000.");
            return null;
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            UtilVista.aviso(this, "El precio no puede ser negativo.");
            return null;
        }

        Categoria categoria = (Categoria) cboCategoria.getSelectedItem();
        if (categoria == null) {
            UtilVista.aviso(this, "Seleccione una categoría.");
            return null;
        }

        return new Producto(nombre, txtDescripcion.getText().trim(), precio, categoria.getIdCategoria());
    }
}