package Logica_de_salida;

import javax.swing.*;

import javax.swing.filechooser.FileNameExtensionFilter;
//Filtra los archivos que el usuario puede ver o seleccionar, limitándolos a extensiones específicas
//como(.txt, .jpg, .png).

import java.awt.*;

import java.awt.image.BufferedImage;
//permite almacenar y manipular imágenes directamente en la memoria RAM.

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Herramientas_UML {
    // Método para guardar la imagen
    public static void guardarImagen(JPanel canvas, Component ventana) {
        JFileChooser fileChooser = new JFileChooser();
        //El JFileChooser en Java es un componente de la biblioteca Java Swing que permite mostrar un cuadro 
        //de diálogo nativo.
        
        fileChooser.setDialogTitle("Guardar diagrama como imagen...");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes PNG", "png"));
        //establece un filtro que solo permite visualizar y seleccionar archivos con extensión .png, 
        //mostrando la etiqueta "Imágenes PNG" en el menú desplegable del explorador de archivos.
        
        fileChooser.setSelectedFile(new File("MiDiagrama.png"));
        //define el archivo que aparecerá seleccionado por defecto al abrir el cuadro de diálogo 
        //del JFileChooser

        int seleccion = fileChooser.showSaveDialog(ventana);
        //abre la ventana flotante para guardar archivos y detiene el programa hasta que el usuario elija una
        //opción (Guardar o Cancelar), guardando esa decisión en la variable seleccion.
        
        if (seleccion == JFileChooser.APPROVE_OPTION){
            //El usuario hizo clic en el botón "Guardar".
            
            File archivoGuardar = fileChooser.getSelectedFile();
            //Esta línea toma el archivo que el usuario eligió en la pantalla y lo guarda en la variable 
            //archivoGuardar para que el resto de tu código pueda interactuar con él (por ejemplo, 
            //para escribir texto, guardar una imagen, etc.).
            
            if (!archivoGuardar.getName().toLowerCase().endsWith(".png")) {
                archivoGuardar = new File(archivoGuardar.getParentFile(), archivoGuardar.getName() + ".png");
            }
            //Verifica si el nombre del archivo seleccionado termina en .png
            //(sin importar si está en mayúsculas o minúsculas).
            //Ademas si el usuario olvidó escribir la extensión al guardar, el código toma la ruta de la
            //carpeta contenedora (getParentFile()) y renombra el archivo agregándole el .png al final.
            
            BufferedImage foto = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = foto.createGraphics();
            //Prepara una imagen en blanco en la memoria de la computadora.Cómo funciona: 
            //Mide el ancho(getWidth) y el alto(getHeight) del componente visual (canvas). 
            //El formato TYPE_INT_RGB define que la imagen tendrá colores estándar (Rojo, Verde y Azul). 
            //Luego, crea un "pincel virtual" (g2d) para poder "dibujar" sobre esa imagen vacía.
            
            canvas.paint(g2d); 
            g2d.dispose();
            //Copia el contenido visual actual al lienzo en memoria y libera recursos.
            //Cómo funciona: Le ordena al canvas que se dibuje a sí mismo dentro del objeto foto usando el pincel
            //virtual. g2d.dispose() destruye el pincel de forma segura para que el programa no consuma 
            //memoria innecesaria.

            try {
                javax.imageio.ImageIO.write(foto, "png", archivoGuardar);
                JOptionPane.showMessageDialog(ventana, "¡Éxito! Imagen guardada en:\n" + archivoGuardar.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ventana, "Error al guardar la foto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            //Intenta escribir la imagen en el almacenamiento de la computadora y avisa al usuario el 
            //resultado. Cómo funciona: ImageIO.write convierte los bits de la imagen en un archivo 
            //PNG real en la ruta especificada. Si lo logra, muestra una ventana flotante (JOptionPane)
            //de éxito con la ruta completa. Si ocurre un fallo (por ejemplo, falta de permisos o disco lleno), el bloque catch atrapa el error y muestra un aviso de alerta con la causa del problema.
        }
    }

    // Método para generar el texto en Java
    public static void generarCodigoJava(List<UML_Clase> clasesUML, List<UML_relacion> relaciones, Component ventana) {
        
        System.out.println("=========================================");
        System.out.println("CÓDIGO JAVA (CON FAMILIAS):");
        System.out.println("=========================================\n");
        
        for (UML_Clase clase : clasesUML) {
            //iniciamos un bucle for-each. Por cada objeto UML_Clase dentro de la lista clasesUML, 
            //ejecutará todo el bloque de código a continuación para construir su equivalente
            
            String textoHereda = "";
            for (UML_relacion rel : relaciones) {
                if (rel.tipo == UML_relacion.Tipo.HERENCIA && rel.origen == clase) 
                {
                    textoHereda = " extends " + rel.destino.nombre; 
                }
            }
            //String textoHereda = "";: Prepara una variable vacía por si la clase no hereda de nadie.
            //El bucle interno: Revisa todas las relaciones del diagrama UML.
            //La condición if: Si encuentra una relación que es de tipo HERENCIA y la clase actual 
            //(clase) es el origen de esa flecha de herencia, significa que esta clase es la "hija".
            //Si se cumple, guarda en textoHereda la palabra clave extends seguida del nombre de la 
            //clase padre (el destino de la relación).

            List<String> susHijos = new ArrayList<>();
            for (UML_relacion rel : relaciones) {
                if (rel.tipo == UML_relacion.Tipo.HERENCIA && rel.destino == clase) {
                    susHijos.add(rel.origen.nombre); 
                }
            }
           
            //List<String> susHijos: Crea una lista temporal para guardar los nombres de las clases
            //que heredan de la clase actual.
            //El bucle interno: Vuelve a revisar las relaciones.
            //La condición if: Si hay una relación de HERENCIA pero ahora la clase actual es el destino de 
            //la flecha, significa que es la clase "madre/padre".
            //Añade el nombre de la clase hija (origen.nombre) a la lista susHijos.
            
            System.out.println("public class " + clase.nombre + textoHereda + " {");
            if (!susHijos.isEmpty()) {
                System.out.println("    // ATENCIÓN: Esta es una clase MADRE.");
                System.out.println("    // Sus clases hijas son: " + String.join(", ", susHijos) + "\n");
            }
            //rimera línea: Imprime la apertura de la clase. Ejemplo: public class Perro extends Animal {. 
            //(Si no tiene padre, textoHereda está vacío y solo imprime public class Perro {).
            //Bloque if: Si la lista de susHijos no está vacía, imprime comentarios que dentro del código 
            //generado indicando que es una clase madre, uniendo los nombres de las hijas separados por comas 
            //gracias a String.join().
            
            for (String attr : clase.atributos) {
                System.out.println("    private " + attr.replace("-", "").replace("+", "").trim() + ";");
            }
            if (!clase.atributos.isEmpty()) System.out.println();
            //Recorre la lista de atributos de la clase. 
            //.replace("-", "").replace("+", ""): Elimina los
            //símbolos de visibilidad típicos de UML (donde - significa privado y + público).
            //.trim(): Quita espacios en blanco innecesarios al principio y al final.
            //Fuerza a que todos los atributos sean privados (private), siguiendo las buenas prácticas de encapsulamiento.
            //El if final añade un salto de línea en blanco si hubo atributos, para separar visualmente las variables de los métodos.
            
            for (String met : clase.metodos) {
                System.out.println("    public " + met.replace("-", "").replace("+", "").trim() + " {");
                System.out.println("        // Haz algo aquí");
                System.out.println("    }");
            }
            System.out.println("}\n");
            //Recorre la lista de métodos de la clase.
            //Al igual que con los atributos, limpia los símbolos - o +.
            //Fuerza a que los métodos sean públicos (public), abriendo las llaves {.
            //La última línea cierra la llave de toda la clase }\n y añade un salto de línea adicional
            //antes de procesar la siguiente clase del bucle principal.
            
            
        }
        JOptionPane.showMessageDialog(ventana, "¡Revisa la consola de tu editor! El código ha sido generado.");
    }
}
