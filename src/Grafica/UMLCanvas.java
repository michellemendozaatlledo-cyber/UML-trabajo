
package Grafica;
import Logica_de_salida.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class UMLCanvas extends JPanel {
    List<UML_Clase> clasesUML = new ArrayList<>(); //guarda las cajitas
    List<UML_relacion> relaciones = new ArrayList<>(); //guarda las flechas
    
    private UML_Clase claseSeleccionadaArrastre = null; 
    private UML_Clase claseSeleccionadaMenu = null; // Para saber a quién le hicimos clic derecho
    private Point offsetRaton; 
    
    // El menú de clic derecho
    private JPopupMenu menuContextual;

    public UMLCanvas() {
        setBackground(Color.WHITE);
        crearMenuContextual();

        MouseAdapter ratonAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                UML_Clase claseClickeada = obtenerClaseEnPosicion(e.getPoint()); //verifica el click en la caja

                // Si es CLIC DERECHO
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseSeleccionadaMenu = claseClickeada; //guarda la clase del click y ... 
                        menuContextual.show(e.getComponent(), e.getX(), e.getY()); //... abre el contextual
                    }
                } 
                // Si es CLIC IZQUIERDO (Para arrastrar)
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseSeleccionadaArrastre = claseClickeada; //guarda la clase a mover
                        offsetRaton = new Point(e.getX() - claseClickeada.bounds.x, e.getY() - claseClickeada.bounds.y); //distancia entre el raton y la caja a arrastrar
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                claseSeleccionadaArrastre = null;  //indica q ya no estamos seleccionando la caja
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (claseSeleccionadaArrastre != null && SwingUtilities.isLeftMouseButton(e)) { //verifica q se "arrastre" una caja && q sea con el click derecho
                    claseSeleccionadaArrastre.bounds.x = e.getX() - offsetRaton.x; //obtiene las cordenadas de la caja
                    claseSeleccionadaArrastre.bounds.y = e.getY() - offsetRaton.y;
                    repaint(); //vuelve a dibujar en el "lienzo" la caja
                }
            }
        };

        addMouseListener(ratonAdapter); //escucha click
        addMouseMotionListener(ratonAdapter); //escucha movimiento
    }

    private void crearMenuContextual() {
        menuContextual = new JPopupMenu();
        
        JMenuItem itemEditar = new JMenuItem("Editar Nombre");
        JMenuItem itemEliminar = new JMenuItem("Eliminar Clase");

        itemEditar.addActionListener(e -> { //cuando haga click se ejecuta todo lo de adentro
            if (claseSeleccionadaMenu != null) {
                String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", claseSeleccionadaMenu.nombre);
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) { //verifica: q no se cierre, q no tenga espacios y q no este vacio
                    claseSeleccionadaMenu.nombre = nuevoNombre.trim();
                    repaint(); //vuelve a dibujar la caja
                }
            }
        });

        itemEliminar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar '" + claseSeleccionadaMenu.nombre + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) { //si se confirma eliminacion
                    clasesUML.remove(claseSeleccionadaMenu);
                    // Borrar relaciones conectadas a esta clase
                    relaciones.removeIf(rel -> rel.origen == claseSeleccionadaMenu || rel.destino == claseSeleccionadaMenu);
                    repaint();
                }
            }
        });

        menuContextual.add(itemEditar);
        menuContextual.addSeparator(); // Una rayita separadora
        menuContextual.add(itemEliminar);
    }

    private UML_Clase obtenerClaseEnPosicion(Point p) { //contiene las cordenadas donde se hizo click
        // Recorremos de atrás para adelante por si hay cajas encimadas ya q la ultima q se crea es la del frente
        for (int i = clasesUML.size() - 1; i >= 0; i--) { 
            if (clasesUML.get(i).bounds.contains(p)) { //verifica q se seleccione la caja (cordenadas)
                return clasesUML.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //borra lo q se dibujo en el lienzo anterior
        Graphics2D g2d = (Graphics2D) g;  //permite tener herramientas de edicion
        g2d.setStroke(new BasicStroke(2)); //es el grosor de las lineas (2 pixeles)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //renderizado de graficos,suavizado

        // 1. Dibujar Relaciones (Cuerdas y Flechas)
        for (UML_relacion rel : relaciones) { //bucle de lista: recorre la lista de "relaciones", toma una por una la llama "rel" y le aplica el codigo
            int cx1 = (rel.origen.bounds.x) + (rel.origen.bounds.width / 2); //(geometria de una caja: cordenada x,y,ancho,alto) + (busca el centro de la caja (mitad))
            int cy1 = rel.origen.bounds.y + (rel.origen.bounds.height / 2);
            int cx2 = rel.destino.bounds.x + (rel.destino.bounds.width / 2);
            int cy2 = rel.destino.bounds.y + (rel.destino.bounds.height / 2);

            if (rel.tipo == UML_relacion.Tipo.HERENCIA) { //pregunta si la linea presenta una herencia y....
                g2d.setColor(Color.BLUE); //....si es asi, la coloca en color azul
                
                // Calcular el punto exacto donde la línea toca el borde de la caja destino
                Point bordeDestino = obtenerInterseccionBorde(cx1, cy1, cx2, cy2, rel.destino.bounds); //se hace el calculo para q se vea el triangulo
                double angulo = Math.atan2(bordeDestino.y - cy1, bordeDestino.x - cx1); //a donde apunta la flecha
                
                g2d.drawLine(cx1, cy1, bordeDestino.x, bordeDestino.y); //traza la linea azul desde la caja 1 y termina en el borde de la caja 2
                dibujarTriangulo(g2d, bordeDestino.x, bordeDestino.y, angulo); //se dibuja el triangulo de la flecha
            } else { //si no es herencia (solo asociacion normal)
                g2d.setColor(Color.GRAY); //sera una simple linea gris
                g2d.drawLine(cx1, cy1, cx2, cy2); //dibuja una linea recta entre las 2 cajas
            }
        }

        // 2. Dibujar Clases (Cajas)
        for (UML_Clase uml : clasesUML) {
            int altoTitulo = 30; //1
            int altoAtributos = Math.max(30, uml.atributos.size() * 15 + 10); //1
            int altoMetodos = Math.max(30, uml.metodos.size() * 15 + 10); //1
            uml.bounds.height = altoTitulo + altoAtributos + altoMetodos; //1: tamaño de la caja,adecua el tamaño segun los elementos q tenga

            g2d.setColor(new Color(255, 255, 220)); //color de la caja (amarillo claro)
            g2d.fill(uml.bounds); //rellena la caja con el color
            g2d.setColor(Color.BLACK); //color negro para...
            g2d.draw(uml.bounds); //...el contorno de la caja

            g2d.setFont(new Font("Arial", Font.BOLD, 12)); //2 titulo: letra arial, en negrita 12
            g2d.drawString(uml.nombre, uml.bounds.x + 10, uml.bounds.y + 20); //2
            g2d.drawLine(uml.bounds.x, uml.bounds.y + altoTitulo, uml.bounds.x + uml.bounds.width, uml.bounds.y + altoTitulo); //2

            g2d.setFont(new Font("Arial", Font.PLAIN, 12)); //3 atributos: cambia el estilo de letra sin negrita
            int yActual = uml.bounds.y + altoTitulo + 15; //3 
            for (String attr : uml.atributos) { //recorre la lista de atributos y baja un reglon
                g2d.drawString(attr, uml.bounds.x + 10, yActual);
                yActual += 15;
            }
            int lineaSeparadoraY = uml.bounds.y + altoTitulo + altoAtributos; //coloca una linea para separar y...
            g2d.drawLine(uml.bounds.x, lineaSeparadoraY, uml.bounds.x + uml.bounds.width, lineaSeparadoraY); //...la dibuja

            yActual = lineaSeparadoraY + 15;
            for (String met : uml.metodos) { //hace lo mismo con los atributos
                g2d.drawString(met, uml.bounds.x + 10, yActual);
                yActual += 15;
            }
        }
    }

    // Calcula dónde corta la línea con el borde de la caja (Para que la flecha no se esconda)
    private Point obtenerInterseccionBorde(int x1, int y1, int x2, int y2, Rectangle rect) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double w = rect.width / 2.0;
        double h = rect.height / 2.0;

        double crossX = w * Math.signum(dx);
        double crossY = h * Math.signum(dy);

        if (Math.abs(dx * h) > Math.abs(dy * w)) {
            crossY = (dy / dx) * crossX;
        } else {
            crossX = (dx / dy) * crossY;
        }
        return new Point((int) (x2 + crossX), (int) (y2 + crossY));
    }

    private void dibujarTriangulo(Graphics2D g2d, int x, int y, double angulo) {
        int tam = 16; 
        int x2 = (int) (x - tam * Math.cos(angulo - Math.PI / 6));
        int y2 = (int) (y - tam * Math.sin(angulo - Math.PI / 6));
        int x3 = (int) (x - tam * Math.cos(angulo + Math.PI / 6));
        int y3 = (int) (y - tam * Math.sin(angulo + Math.PI / 6));

        Polygon triangulo = new Polygon(new int[]{x, x2, x3}, new int[]{y, y2, y3}, 3);
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(triangulo); 
        g2d.setColor(Color.BLUE);
        g2d.drawPolygon(triangulo); 
    }
}
