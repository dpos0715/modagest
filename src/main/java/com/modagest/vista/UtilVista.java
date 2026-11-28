package com.modagest.vista;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Metodos de apoyo para las pantallas: mensajes al usuario y armado de formularios.
 */
public final class UtilVista {

    private static final String TITULO = "ModaGest";

    // Clase de utilidad: no se instancia
    private UtilVista() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Traduce los errores mas comunes de MySQL a mensajes entendibles.
     * 1062 = valor duplicado (codigo de barras repetido), 1451 = tiene registros relacionados.
     */
    public static void errorBaseDatos(Component padre, SQLException ex) {
        if (ex.getErrorCode() == 1062) {
            error(padre, "Ya existe un registro con ese valor unico (por ejemplo, el código de barras).");
        } else if (ex.getErrorCode() == 1451) {
            error(padre, "No se puede eliminar porque tiene registros relacionados (por ejemplo, ventas).");
        } else {
            error(padre, "Error de base de datos:\n" + ex.getMessage());
        }
    }

    /** Pregunta Si/No. Devuelve true si el usuario confirma. */
    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opcion == JOptionPane.YES_OPTION;
    }

    /**
     * Agrega una etiqueta y su campo a un panel con GridBagLayout.
     * Cada "columna" ocupa dos celdas: la etiqueta y el campo.
     */
    public static void agregarCampo(JPanel panel, String etiqueta, JComponent campo,
                                    int columna, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(4, 6, 4, 6);
        restricciones.anchor = GridBagConstraints.WEST;

        restricciones.gridx = columna * 2;
        restricciones.gridy = fila;
        panel.add(new JLabel(etiqueta), restricciones);

        restricciones.gridx = columna * 2 + 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.weightx = 1.0;
        panel.add(campo, restricciones);
    }
}