
package Grafica;
import Logica_de_salida.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

class UMLCanvas extends JPanel {
    List<UML_Clase> clasesUML = new ArrayList<>();
    List<UML_relacion> relaciones = new ArrayList<>();
    
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
                UML_Clase claseClickeada = obtenerClaseEnPosicion(e.getPoint());

                // Si es CLIC DERECHO
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseSeleccionadaMenu = claseClickeada;
                        menuContextual.show(e.getComponent(), e.getX(), e.getY());
                    }
                } 
                // Si es CLIC IZQUIERDO (Para arrastrar)
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseSeleccionadaArrastre = claseClickeada;
                        offsetRaton = new Point(e.getX() - claseClickeada.bounds.x, e.getY() - claseClickeada.bounds.y);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                claseSeleccionadaArrastre = null; 
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (claseSeleccionadaArrastre != null && SwingUtilities.isLeftMouseButton(e)) {
                    claseSeleccionadaArrastre.bounds.x = e.getX() - offsetRaton.x;
                    claseSeleccionadaArrastre.bounds.y = e.getY() - offsetRaton.y;
                    repaint(); 
                }
            }
        };

        addMouseListener(ratonAdapter);
        addMouseMotionListener(ratonAdapter);
    }

    private void crearMenuContextual() {
        menuContextual = new JPopupMenu();
        
        JMenuItem itemEditar = new JMenuItem("Editar Nombre");
        JMenuItem itemEliminar = new JMenuItem("Eliminar Clase");

        itemEditar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", claseSeleccionadaMenu.nombre);
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    claseSeleccionadaMenu.nombre = nuevoNombre.trim();
                    repaint();
                }
            }
        });

        itemEliminar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar '" + claseSeleccionadaMenu.nombre + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
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

    private UML_Clase obtenerClaseEnPosicion(Point p) {
        // Recorremos de atrás para adelante por si hay cajas encimadas
        for (int i = clasesUML.size() - 1; i >= 0; i--) {
            if (clasesUML.get(i).bounds.contains(p)) {
                return clasesUML.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; 
        g2d.setStroke(new BasicStroke(2)); 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujar Relaciones (Cuerdas y Flechas)
        for (UML_relacion rel : relaciones) {
            int cx1 = rel.origen.bounds.x + (rel.origen.bounds.width / 2);
            int cy1 = rel.origen.bounds.y + (rel.origen.bounds.height / 2);
            int cx2 = rel.destino.bounds.x + (rel.destino.bounds.width / 2);
            int cy2 = rel.destino.bounds.y + (rel.destino.bounds.height / 2);

            if (rel.tipo == UML_relacion.Tipo.HERENCIA) {
                g2d.setColor(Color.BLUE);
                
                // Calcular el punto exacto donde la línea toca el borde de la caja destino
                Point bordeDestino = obtenerInterseccionBorde(cx1, cy1, cx2, cy2, rel.destino.bounds);
                double angulo = Math.atan2(bordeDestino.y - cy1, bordeDestino.x - cx1);
                
                g2d.drawLine(cx1, cy1, bordeDestino.x, bordeDestino.y);
                dibujarTriangulo(g2d, bordeDestino.x, bordeDestino.y, angulo); 
            } else {
                g2d.setColor(Color.GRAY);
                g2d.drawLine(cx1, cy1, cx2, cy2);
            }
        }

        // 2. Dibujar Clases (Cajas)
        for (UML_Clase uml : clasesUML) {
            int altoTitulo = 30;
            int altoAtributos = Math.max(30, uml.atributos.size() * 15 + 10);
            int altoMetodos = Math.max(30, uml.metodos.size() * 15 + 10);
            uml.bounds.height = altoTitulo + altoAtributos + altoMetodos;

            g2d.setColor(new Color(255, 255, 220)); 
            g2d.fill(uml.bounds);
            g2d.setColor(Color.BLACK);
            g2d.draw(uml.bounds); 

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(uml.nombre, uml.bounds.x + 10, uml.bounds.y + 20);
            g2d.drawLine(uml.bounds.x, uml.bounds.y + altoTitulo, uml.bounds.x + uml.bounds.width, uml.bounds.y + altoTitulo);

            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            int yActual = uml.bounds.y + altoTitulo + 15;
            for (String attr : uml.atributos) {
                g2d.drawString(attr, uml.bounds.x + 10, yActual);
                yActual += 15;
            }
            int lineaSeparadoraY = uml.bounds.y + altoTitulo + altoAtributos;
            g2d.drawLine(uml.bounds.x, lineaSeparadoraY, uml.bounds.x + uml.bounds.width, lineaSeparadoraY);

            yActual = lineaSeparadoraY + 15;
            for (String met : uml.metodos) {
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
