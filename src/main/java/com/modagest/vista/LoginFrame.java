package com.modagest.vista;

import com.modagest.dao.UsuarioDAO;
import com.modagest.modelo.Usuario;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Ventana de inicio de sesion. Valida el usuario con la base de datos
 * y abre la ventana principal segun su rol.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        super("ModaGest - Inicio de sesión");
        construirInterfaz();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null); // centra la ventana en la pantalla
    }

    private void construirInterfaz() {
        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("ModaGest", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitulo = new JLabel("Sistema de gestión para tienda de ropa", SwingConstants.CENTER);

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(4, 4, 4, 4);
        restricciones.gridx = 0;
        restricciones.gridwidth = 2;
        restricciones.fill = GridBagConstraints.HORIZONTAL;

        restricciones.gridy = 0;
        contenido.add(titulo, restricciones);
        restricciones.gridy = 1;
        contenido.add(subtitulo, restricciones);

        UtilVista.agregarCampo(contenido, "Correo:", txtCorreo, 0, 2);
        UtilVista.agregarCampo(contenido, "Contraseña:", txtClave, 0, 3);

        restricciones.gridy = 4;
        restricciones.insets = new Insets(16, 4, 4, 4);
        contenido.add(btnIngresar, restricciones);

        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar); // Enter equivale a pulsar "Ingresar"

        setContentPane(contenido);
    }

    /** Valida las credenciales y abre la ventana principal. */
    private void ingresar() {
        String correo = txtCorreo.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (correo.isEmpty() || clave.isEmpty()) {
            UtilVista.aviso(this, "Escriba su correo y su contraseña.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(correo, clave);

            if (usuario == null) {
                UtilVista.error(this, "Correo o contraseña incorrectos.");
                txtClave.setText("");
                return;
            }

            new PrincipalFrame(usuario).setVisible(true);
            dispose(); // cierra la ventana de login

        } catch (SQLException ex) {
            UtilVista.error(this, "No se pudo conectar con la base de datos.\n"
                    + "Verifique que MySQL esté encendido en XAMPP.\n\n" + ex.getMessage());
        }
    }

    /** Punto de arranque de toda la aplicacion. */
    public static void main(String[] args) {
        try {
            // Usa el aspecto visual de Windows en lugar del estilo por defecto de Java
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Si falla, se usa el aspecto por defecto y la aplicacion funciona igual
        }
        // Las ventanas Swing se crean en el hilo de eventos
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}