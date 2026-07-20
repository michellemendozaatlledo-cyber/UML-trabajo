package interfaz; // Indica que este archivo vive dentro de la carpeta "interfaz"
//  buscamos los codigos de antone y coldova
import Grafica.UMLCanvas; // Trae la pizarra interactiva donde vamos a dibujar(codigo de antone)
import Logica_de_salida.UML_Clase; // Trae el molde o plantilla para crear las cajitas (clases).
import Logica_de_salida.UML_relacion; // Trae la plantilla para trazar las líneas que conectan las cajas.
import Logica_de_salida.Herramientas_UML; // Trae funciones extra, como la cámara de fotos o el generador de código.

import javax.swing.*; // Trae las herramientas visuales de Java (botones, ventanas, menús de diálogo).
import java.awt.*; // Trae las reglas de diseño (colores, tamaños, posiciones).

public class Diseñador_UML extends JFrame {
    
    // Declaramos nuestra pizarra principal para que todo el programa la conozca
    public UMLCanvas canvas;


    public Diseñador_UML() {
        setTitle("Diseñador UML (Clic derecho para Editar/Eliminar)"); 
        setSize(1000, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLayout(new BorderLayout()); 
        
        canvas = new UMLCanvas(); //fabrica la pizarra real
        add(canvas, BorderLayout.CENTER); // Pegamos la pizarra en el CENTRO exacto de nuestra ventana.

        JPanel panelBotones = new JPanel(); // Creamos una bandeja o estante para organizar nuestros controles.
        
        // Fabricamos las etiquetas de cada botón que el usuario va a poder tocar.
        JButton btnCrear = new JButton("Nueva Clase");
        JButton btnAtributo = new JButton("+ Atributo");
        JButton btnMetodo = new JButton("+ Método");
        JButton btnRelacion = new JButton("Crear Relación");
        JButton btnFoto = new JButton("Tomar Foto (Guardar)");
        JButton btnCodigo = new JButton("Generar Código");

        // Colocamos ordenadamente cada botón dentro de nuestra bandeja (panelBotones).
        panelBotones.add(btnCrear);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL)); // Ponemos una barrita vertical para separar visualmente.
        panelBotones.add(btnAtributo);
        panelBotones.add(btnMetodo);
        panelBotones.add(btnRelacion);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL)); // Otra barrita separadora.
        panelBotones.add(btnFoto);
        panelBotones.add(btnCodigo);
        
        add(panelBotones, BorderLayout.NORTH); // Colocamos la bandeja llena de botones en la parte SUPERIOR (Norte) de la ventana.

        // Cuando toquen "Nueva Clase":
        btnCrear.addActionListener(e -> {
            //Muestra un cuadro pidiendo el nombre y lo guarda en la variable 'nombre'
            String nombre = JOptionPane.showInputDialog("¿Cómo se llamará la clase?");
            
            //verificamos que el usuario no haya cancelado (!= null) y no haya dejado espacios en blanco (!isEmpty)
            if (nombre != null && !nombre.trim().isEmpty()) {
                
                // Contamos cuántas cajas hay actualmente en la pizarra
                int cantidad = canvas.clasesUML.size(); 
                
                // Calculamos dónde poner la nueva caja. sumamos 30 píxeles por cada caja existente para que no se amontonen.
                int nuevaX = 50 + (cantidad * 30);
                int nuevaY = 50 + (cantidad * 30);
                
                // Construimos la nueva caja con su nombre y coordenadas, y la agregamos a la colección.
                canvas.clasesUML.add(new UML_Clase(nombre, nuevaX, nuevaY));
                
                // Pedimos a la pizarra que borre y vuelva a dibujar todo para que aparezca la nueva caja.
                canvas.repaint(); 
            }
        });

        // Cuando toquen "+ Atributo":
        btnAtributo.addActionListener(e -> {
            // Usamos nuestra herramienta auxiliar para pedirle al usuario que elija una de las clases existentes.
            UML_Clase sel = elegirClase("¿A qué clase le agregas el atributo?");
            if (sel != null) { // Si realmente eligió una clase
                // Le pedimos que escriba el texto del atributoop
                String attr = JOptionPane.showInputDialog("Escribe el atributo (Ej: - int edad):");
                if (attr != null) { 
                    sel.atributos.add(attr); // Lo anotamos en la lista de atributos de esa clase
                    canvas.repaint(); // Refrescamos el dibujo
                }
            }
        });

        // Cuando toquen "+ Método": (Misma lógica didáctica que el atributo)
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

        // Cuando toquen "Crear Relación":
        btnRelacion.addActionListener(e -> {
            // Preguntamos quién es el inicio de la flecha. Si cancela, cortamos la ejecución (return).
            UML_Clase origen = elegirClase("Elige al HIJO (el que hereda / apunta):");
            if (origen == null) return;
            
            // Preguntamos quién es el final de la flecha. Si cancela o elige la misma caja, cortamos la ejecución.
            UML_Clase destino = elegirClase("Elige a la MADRE (de quien hereda / recibe):");
            if (destino == null || origen == destino) return;

            // Creamos una variable de control asumiendo inicialmente que no existe la línea.
            boolean relacionYaExiste = false;
            
            // Pasamos lista por todas las líneas dibujadas para ver si ya hay una conexión entre estas dos clases.
            for (UML_relacion rel : canvas.relaciones) {
                // Comparamos los extremos sin importar el orden (A conectado con B, o B conectado con A).
                if ((rel.origen == origen && rel.destino == destino) || 
                    (rel.origen == destino && rel.destino == origen)) {
                    relacionYaExiste = true; // Si encontramos coincidencia, activamos la alarma.
                    break; // Dejamos de buscar, ya tenemos la respuesta.
                }
            }

            // Si la alarma de seguridad está activa, mostramos una advertencia y detenemos el proceso.
            if (relacionYaExiste) {
                JOptionPane.showMessageDialog(this, "Ya existe una línea o flecha conectando estas dos clases.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Damos a elegir el estilo visual de la línea mediante un menú de opciones.
            String[] opciones = {"Línea Normal", "Es una Herencia (Madre-Hijo)"};
            int resp = JOptionPane.showOptionDialog(this, "¿Qué tipo de relación es?", "Relación", 
                    0, 3, null, opciones, opciones[0]); // 'resp' guardará 0 (Normal) o 1 (Herencia).

            // Traducimos la respuesta numérica al tipo de línea que entiende nuestro código.
            UML_relacion.Tipo tipo = (resp == 1) ? UML_relacion.Tipo.HERENCIA : UML_relacion.Tipo.SIMPLE;
            
            // Trazamos la línea conectando los dos puntos elegidos.
            canvas.relaciones.add(new UML_relacion(origen, destino, tipo));
            canvas.repaint(); // Actualizamos la vista.
        });

        // Cuando toquen la foto: Le pasamos la tarea al archivo externo de Herramientas.
        btnFoto.addActionListener(e -> Herramientas_UML.guardarImagen(canvas, this));
        
        // Cuando pidan código: Le mandamos nuestras listas de datos a Herramientas para que las procese y muestre en consola.
        btnCodigo.addActionListener(e -> Herramientas_UML.generarCodigoJava(canvas.clasesUML, canvas.relaciones, this));
    }

    //pregunta a que caja o clase le vamos agregar atributos
    public UML_Clase elegirClase(String mensaje) {
        if (canvas.clasesUML.isEmpty()) return null;//verifica si hay clases, si no hay devuelve nada "null"
        
  // Extrae solo los nombres de todas las clases y los mete en una lista de textos (arreglo)
        String[] nombres = canvas.clasesUML.stream().map(c -> c.nombre).toArray(String[]::new);
        
       // Muestra el menú desplegable con los nombres y guarda lo que el usuario eligió en 'sel'
        String sel = (String) JOptionPane.showInputDialog(this, mensaje, "Elige", 3, null, nombres, nombres[0]);
        
        // Busca en la memoria la clase real que coincida con ese nombre y la devuelve
        for (UML_Clase c : canvas.clasesUML) if (c.nombre.equals(sel)) return c;
        
        return null; // Por si el usuario cierra la ventana sin elegir nada.
    }

    public static void main(String[] args) {
        // Esta es la chispa inicial. Le pedimos a Java que ensamble y encienda la interfaz de manera segura.
        SwingUtilities.invokeLater(() -> new Diseñador_UML().setVisible(true));
    }
}