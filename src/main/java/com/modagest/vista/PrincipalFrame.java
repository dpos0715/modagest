package com.modagest.vista;

import com.modagest.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Ventana principal. Muestra las pestanas segun el rol del usuario:
 * - Administrador y Bodega: Productos + Variantes e inventario.
 * - Cajero: solo Consulta de stock (HU-002).
 */
public class PrincipalFrame extends JFrame {

    private PanelVariantes panelVariantes;

    public PrincipalFrame(Usuario usuario) {
        super("ModaGest - Gestión de tienda de ropa");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(crearEncabezado(usuario), BorderLayout.NORTH);
        add(crearPestanas(usuario), BorderLayout.CENTER);
    }

    /** Barra superior oscura (gris carbon de la paleta de ModaGest). */
    private JPanel crearEncabezado(Usuario usuario) {
        JLabel titulo = new JLabel("ModaGest");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JLabel lblUsuario = new JLabel(usuario.getNombreCompleto() + "  |  " + usuario.getNombreRol());
        lblUsuario.setForeground(new Color(0xD1D5DB));

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        derecha.add(lblUsuario);
        derecha.add(btnSalir);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(0x1E2229));
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JTabbedPane crearPestanas(Usuario usuario) {
        boolean puedeGestionar = usuario.esAdministrador() || usuario.esBodega();

        JTabbedPane pestanas = new JTabbedPane();
        if (puedeGestionar) {
            pestanas.addTab("Productos", new PanelProductos());
        }

        panelVariantes = new PanelVariantes(usuario, puedeGestionar);
        pestanas.addTab(puedeGestionar ? "Variantes e inventario" : "Consulta de stock", panelVariantes);

        // Al abrir la pestana de variantes, se refrescan los productos recien creados
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelVariantes) {
                panelVariantes.recargarProductos();
            }
        });
        return pestanas;
    }
}