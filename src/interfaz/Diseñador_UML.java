package interfaz; // Indica que este archivo vive dentro de la carpeta "interfaz"
// Las siguientes líneas importan las herramientas necesarias de las carpetas de mis compañeros
// buscamos los codigos de antone y coldova
import Grafica.UMLCanvas; // Trae la pizarra donde se dibuja(Antone)
import Logica_de_salida.*;

import javax.swing.*; // Trae las herramientas visuales (botones, ventanas, menús)
import java.awt.*; // Trae herramientas de diseño (tamaños, posiciones, colores)

public class Diseñador_UML extends JFrame {
    
    // Se declara la variable para la pizarra (el lienzo donde se dibuja todo)
    public UMLCanvas canvas;

    // Este es el CONSTRUCTOR. Es lo primero que se ejecuta cuando nace la ventana
    public Diseñador_UML() {
        setTitle("Diseñador UML (Clic derecho para Editar/Eliminar)");
        setSize(1000, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLayout(new BorderLayout()); 

        canvas = new UMLCanvas(); // Fabrica la pizarra real
        add(canvas, BorderLayout.CENTER); // Pega la pizarra justo en el CENTRO de la ventana

        JPanel panelBotones = new JPanel(); // Crea un panel (como una bandeja de plástico) para guardar los botones
        
        // Se fabrican todos los botones y se les pone un texto
        JButton btnCrear = new JButton("Nueva Clase");
        JButton btnAtributo = new JButton("+ Atributo");
        JButton btnMetodo = new JButton("+ Método");
        JButton btnRelacion = new JButton("Crear Relación");
        JButton btnFoto = new JButton("Tomar Foto (Guardar)");
        JButton btnCodigo = new JButton("Generar Código");

        // Se meten los botones dentro de la bandeja (panelBotones) uno por uno
        panelBotones.add(btnCrear);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL)); // Añade una línea vertical de adorno para separar
        panelBotones.add(btnAtributo);
        panelBotones.add(btnMetodo);
        panelBotones.add(btnRelacion);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL)); // Otra línea separadora
        panelBotones.add(btnFoto);
        panelBotones.add(btnCodigo);
        
        // Se agarra la bandeja llena de botones y se pega en la parte de ARRIBA (NORTH) de la ventana
        add(panelBotones, BorderLayout.NORTH); 


        // cuando presiona el boton "Nueva Clase"
        btnCrear.addActionListener(e -> {
            // Muestra un cuadro pidiendo el nombre y lo guarda en la variable 'nombre'
            String nombre = JOptionPane.showInputDialog("¿Cómo se llamará la clase?");
            
            // Verifica que el usuario no haya cancelado (!= null) y no haya dejado espacios en blanco (!isEmpty)
            if (nombre != null && !nombre.trim().isEmpty()) {
                
                // 1. Cuenta cuántas cajitas hay en la pizarra actualmente
                int cantidad = canvas.clasesUML.size(); 
                
                // 2. Calcula las nuevas coordenadas (suma 30 píxeles por cada cajita que ya exista para hacer el efecto cascada)
                int nuevaX = 50 + (cantidad * 30);
                int nuevaY = 50 + (cantidad * 30);
                
                // 3. Fabrica una nueva clase con su nombre y coordenadas, y la añade a la lista de la pizarra
                canvas.clasesUML.add(new UML_Clase(nombre, nuevaX, nuevaY));
                
                // 4. Le da la orden a la pizarra de que vuelva a dibujarse para mostrar la nueva caja
                canvas.repaint(); 
            }
        });

        // Qué pasa al hacer clic en "+ Atributo"
        btnAtributo.addActionListener(e -> {
            // Llama al método elegirClase (abajo) para que el usuario elija a qué clase agregarle el atributo
            UML_Clase sel = elegirClase("¿A qué clase le agregas el atributo?");
            if (sel != null) { // Si eligió una clase
                // Pide el texto del atributo
                String attr = JOptionPane.showInputDialog("Escribe el atributo (Ej: - int edad):");
                if (attr != null) { 
                    sel.atributos.add(attr); // Lo añade a la lista de atributos de esa clase
                    canvas.repaint(); // Actualiza el dibujo
                }
            }
        });

        // "+ Método" hace exactamente lo mismo que el botón de atributo)
        btnMetodo.addActionListener(e -> {
            UML_Clase sel = elegirClase("¿A qué clase le agregas el método?");
            if (sel != null) {
                String met = JOptionPane.showInputDialog("Escribe el método (Ej: + void caminar()):");
                if (met != null) { 
                    sel.metodos.add(met); 
                    canvas.repaint(); 
                }
            }
        });

        //"Crear Relación"
        btnRelacion.addActionListener(e -> {
            // Verifica que existan al menos 2 clases en la pizarra. si no hay lanza un aviso.
            if (canvas.clasesUML.size() < 2) {
                JOptionPane.showMessageDialog(this, "Necesitas al menos 2 clases creadas en la pizarra para hacer una relación.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return; // Corta la ejecución aquí mismo. no hace nada más.
            }

            if (canvas.clasesUML.size() == 2 && !canvas.relaciones.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Estas clases ya tienen relación.\nSi quieres crear otra relación, debes crear otra clase.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return; 
            }

            // Muestra un menú de opciones con dos botones: Línea Normal y Herencia
            String[] opciones = {"Línea Normal", "Es una Herencia (Madre-Hijo)"};
            int resp = JOptionPane.showOptionDialog(this, "¿Qué tipo de relación es?", "Relación", 
                    0, 3, null, opciones, opciones[0]); // 'resp' guarda 0 (Normal) o 1 (Herencia)

            // Si el usuario eligió alguna opción (no canceló)
            if (resp == 0 || resp == 1) {
                // Guarda en la pizarra qué tipo de línea se va a dibujar
                canvas.tipoRelacionPendiente = (resp == 1) ? UML_relacion.Tipo.HERENCIA : UML_relacion.Tipo.SIMPLE;
                // Le avisa a la pizarra que el ratón ahora funciona para dibujar líneas (activa el modo dibujo)
                canvas.modoRelacion = true; 
                
                // Prepara el mensaje de texto con las instrucciones básicas
                String instrucciones = "Modo Relación Activado:\nHaz clic sobre la primera clase, mantén presionado y arrastra hacia la otra clase.";
                
                // Si eligió Herencia, le suma un texto extra explicando hacia dónde arrastrar
                if (resp == 1) {
                    instrucciones += "\n\n NOTA UML: En la herencia, la flecha debe apuntar a la clase superior.\nPor favor, arrastra desde la clase HIJA y suelta sobre la clase MADRE.";
                }
                
                // Muestra el mensaje en la pantalla
                JOptionPane.showMessageDialog(this, instrucciones, "Instrucciones", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // delega el trabajo al archivo Herramientas_UML
        btnFoto.addActionListener(e -> Herramientas_UML.guardarImagen(canvas, this));
        
        // ¿Qué pasa al hacer clic en "Generar Código"? Delega el trabajo a Herramientas_UML enviándole las listas de datos
        btnCodigo.addActionListener(e -> Herramientas_UML.generarCodigoJava(canvas.clasesUML, canvas.relaciones, this));
    }

    //metodos auxiliares
    
    //pregunta a que caja o clase le vamos agregar atributos
    public UML_Clase elegirClase(String mensaje) {
        if (canvas.clasesUML.isEmpty()) return null; //verifica si hay clases, si no hay devuelve nada "null"
        
        // Extrae solo los nombres de todas las clases y los mete en una lista de textos (arreglo)
        String[] nombres = canvas.clasesUML.stream().map(c -> c.nombre).toArray(String[]::new);
        
        // Muestra el menú desplegable con los nombres y guarda lo que el usuario eligió en 'sel'
        String sel = (String) JOptionPane.showInputDialog(this, mensaje, "Elige", 3, null, nombres, nombres[0]);
        
        // Busca en la memoria la clase real que coincida con ese nombre y la devuelve
        for (UML_Clase c : canvas.clasesUML) if (c.nombre.equals(sel)) return c;
        return null; // Por si el usuario cierra la ventana sin elegir nada.
    }

    public static void main(String[] args) {
        // Le dice a Java que construya la ventana principal de forma segura y la haga visible en la pantalla
        SwingUtilities.invokeLater(() -> new Diseñador_UML().setVisible(true));
    }
}