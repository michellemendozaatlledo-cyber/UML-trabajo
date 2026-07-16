
package interfaz;
import Grafica.UMLCanvas;
import Logica_de_salida.*;
import javax.swing.*;
import java.awt.*;
public class Diseñador_UML extends JFrame{
    public UMLCanvas canvas;

    public Diseñador_UML() {
        setTitle("Diseñador UML (Clic derecho para Editar/Eliminar)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new UMLCanvas();
        add(canvas, BorderLayout.CENTER); 

        JPanel panelBotones = new JPanel();
        
        JButton btnCrear = new JButton("Nueva Clase");
        JButton btnAtributo = new JButton("+ Atributo");
        JButton btnMetodo = new JButton("+ Método");
        JButton btnRelacion = new JButton("Crear Relación");
        JButton btnFoto = new JButton("Tomar Foto (Guardar)");
        JButton btnCodigo = new JButton("Generar Código");

        panelBotones.add(btnCrear);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL)); 
        panelBotones.add(btnAtributo);
        panelBotones.add(btnMetodo);
        panelBotones.add(btnRelacion);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnFoto);
        panelBotones.add(btnCodigo);
        
        add(panelBotones, BorderLayout.NORTH); 

        btnCrear.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("¿Cómo se llamará la clase?");
            if (nombre != null && !nombre.trim().isEmpty()) {
                canvas.clasesUML.add(new UML_Clase(nombre, 50, 50));
                canvas.repaint(); 
            }
        });

        btnAtributo.addActionListener(e -> {
            UML_Clase sel = elegirClase("¿A qué clase le agregas el atributo?");
            if (sel != null) {
                String attr = JOptionPane.showInputDialog("Escribe el atributo (Ej: - int edad):");
                if (attr != null) { sel.atributos.add(attr); canvas.repaint(); }
            }
        });

        btnMetodo.addActionListener(e -> {
            UML_Clase sel = elegirClase("¿A qué clase le agregas el método?");
            if (sel != null) {
                String met = JOptionPane.showInputDialog("Escribe el método (Ej: + void caminar()):");
                if (met != null) { sel.metodos.add(met); canvas.repaint(); }
            }
        });

        btnRelacion.addActionListener(e -> {
            UML_Clase origen = elegirClase("Elige al HIJO (el que hereda / apunta):");
            if (origen == null) return;
            UML_Clase destino = elegirClase("Elige a la MADRE (de quien hereda / recibe):");
            if (destino == null || origen == destino) return;

            String[] opciones = {"Línea Normal", "Es una Herencia (Madre-Hijo)"};
            int resp = JOptionPane.showOptionDialog(this, "¿Qué tipo de relación es?", "Relación", 
                    0, 3, null, opciones, opciones[0]);

            UML_relacion.Tipo tipo = (resp == 1) ? UML_relacion.Tipo.HERENCIA : UML_relacion.Tipo.SIMPLE;
            canvas.relaciones.add(new UML_relacion(origen, destino, tipo));
            canvas.repaint();
        });

     
        btnFoto.addActionListener(e -> Herramientas_UML.guardarImagen(canvas, this));
        btnCodigo.addActionListener(e -> Herramientas_UML.generarCodigoJava(canvas.clasesUML, canvas.relaciones, this));
    }

    public UML_Clase elegirClase(String mensaje) {
        if (canvas.clasesUML.isEmpty()) return null;
        String[] nombres = canvas.clasesUML.stream().map(c -> c.nombre).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, mensaje, "Elige", 3, null, nombres, nombres[0]);
        for (UML_Clase c : canvas.clasesUML) if (c.nombre.equals(sel)) return c;
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Diseñador_UML().setVisible(true));
    }
}
