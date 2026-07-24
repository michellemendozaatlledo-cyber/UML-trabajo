package Grafica;

import Logica_de_salida.UML_Clase;
import Logica_de_salida.UML_relacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// ============================================================================
// 2. CANVAS Y COMPONENTES VISUALES
// ============================================================================

// la clase UMLCanvas se "hereda" de JPanel, lo que significa que esta clase ES como un lienzo 
public class UMLCanvas extends JPanel {
    
    public List<UML_Clase> clasesUML = new ArrayList<>(); // guarda las cajitas q se van creando en el lienzo
    public List<UML_relacion> relaciones = new ArrayList<>(); // guarda las flechas q se van creando en el lienzo
    
    private UML_Clase claseSeleccionadaArrastre = null; //guarda la clase (caja) q se esta moviendo con el click izq
    private UML_Clase claseSeleccionadaMenu = null; // referencia Para saber a quién le hicimos clic derecho
    private UML_relacion relacionSeleccionadaMenu = null; // referencia Para saber a qué línea le hicimos clic derecho
    private Point offsetRaton; //guarda la distancia exacta donde hiciste click
    
    // El menú de clic derecho
    private JPopupMenu menuContextual; //el menu para las cajas al hacer click der
    private JPopupMenu menuContextualRelacion; // Menú emergente para líneas al hacer click der

    // Variables para el modo de trazado de relaciones
    public boolean modoRelacion = false; // Indica si estamos en "modo dibujo" de trazar lineas
    public UML_relacion.Tipo tipoRelacionPendiente = null; //guarda si haremos una linea SIMPLE o de HERENCIA
    private UML_Clase claseInicioRelacion = null; //guarda la caja inicial de origen
    private Point puntoFinRelacionTemp = null; // dónde apunta el ratón mientras arrastramos

    public UMLCanvas() {
        setBackground(Color.WHITE); // pinta el fondo de color blanco
        crearMenuContextual(); // Llamamos a la función para crear el menu

        // (MouseAdapter) estará atento a todo lo que haga el ratón
        MouseAdapter ratonAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                UML_Clase claseClickeada = obtenerClaseEnPosicion(e.getPoint()); // verifica el click en la caja y obtiene cordenadas

                // si se activo el modo relacion y el click fue izq
                if (modoRelacion && SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) { //verifica q el click este en una caja
                        claseInicioRelacion = claseClickeada; //esa clase sera la de origen...
                        puntoFinRelacionTemp = e.getPoint(); //...y obtiene la coordenada donde se arrastra el mouse lo q da el efecto de estirar la linea 
                    }
                    return; 
                }

                // Si es CLIC DERECHO 
                if (SwingUtilities.isRightMouseButton(e)) { 
                    if (claseClickeada != null) { //verifica q el click este en una caja
                        claseSeleccionadaMenu = claseClickeada; // guarda la clase del click y ... 
                        menuContextual.show(e.getComponent(), e.getX(), e.getY()); // ... abre el contextual
                    } else {
                        // Si no hizo clic en una caja, verifica si hizo clic sobre una línea/flecha
                        UML_relacion relClickeada = obtenerRelacionEnPosicion(e.getPoint());
                        if (relClickeada != null) {  //
                            relacionSeleccionadaMenu = relClickeada; //guarda la linea del click y ...
                            menuContextualRelacion.show(e.getComponent(), e.getX(), e.getY()); //...abre el menu contextual de lineas
                        }
                    }
                } 
                // Si es CLIC IZQUIERDO (Para arrastrar)
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseSeleccionadaArrastre = claseClickeada; // guarda la clase a mover
                        offsetRaton = new Point(e.getX() - claseClickeada.bounds.x, e.getY() - claseClickeada.bounds.y); // distancia entre el raton y la caja a arrastrar
                    }
                }
            }

            // ¿Qué pasa cuando el usuario SUELTA el botón del ratón?
            @Override
            public void mouseReleased(MouseEvent e) {
                // Si estábamos arrastrando una flecha y ya teniamos una clase de origen
                if (modoRelacion && claseInicioRelacion != null) {
                    UML_Clase claseDestino = obtenerClaseEnPosicion(e.getPoint()); // obtiene las coordenadas dónde soltamos el ratón
                    
                    if (claseDestino != null && claseDestino != claseInicioRelacion) { // revisa Si soltaste el ratón sobre otra clase válida y no sobre la misma caja
                        
                        // Guardia de seguridad: Verificamos si ya existe esta relación
                        boolean relacionYaExiste = false;
                        for (UML_relacion rel : relaciones) { //recorre la lista de relaciones y comprueba si...
                            if ((rel.origen == claseInicioRelacion && rel.destino == claseDestino) || //hay una linea entre la de origen y destino
                                (rel.origen == claseDestino && rel.destino == claseInicioRelacion)) { // o de destino a origen
                                relacionYaExiste = true; //si ya hay,  entonces "marca" q ya existe relacion 
                                break;
                            }
                        }

                        if (relacionYaExiste) {
                            // Si ya existe, lanza un aviso de advertencia
                            JOptionPane.showMessageDialog(UMLCanvas.this, "Ya existe una línea o flecha conectando estas dos clases.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        } else {
                            // Si no existe, creamos la flecha oficialmente y la guardamos en la lista de relaciones
                            relaciones.add(new UML_relacion(claseInicioRelacion, claseDestino, tipoRelacionPendiente)); //crea un nuevo objeto al q le da parametros al constructor del molde 
                            
                            // Si acabamos de crear una herencia, avisamos al usuario sobre la clase Madre y el cambio de color
                            if (tipoRelacionPendiente == UML_relacion.Tipo.HERENCIA) {
                                JOptionPane.showMessageDialog(UMLCanvas.this, 
                                    "¡La clase '" + claseDestino.nombre + "' ahora es una MADRE!\nSu caja cambiará a un color único para diferenciarla de las demás.", 
                                    "Herencia Creada", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                    
                    // Apagamos el modo dibujo de líneas para mostrar el estado actual del lienzo
                    modoRelacion = false;
                    claseInicioRelacion = null;
                    puntoFinRelacionTemp = null;
                    repaint(); // Actualiza la pantalla
                    return;
                }

                claseSeleccionadaArrastre = null; // Olvidamos la caja, ya no la estamos arrastrando
            }

            // ¿Qué pasa cuando el usuario MUEVE el ratón MIENTRAS lo tiene presionado?
            @Override
            public void mouseDragged(MouseEvent e) {
                // Si estamos en modo dibujo, hacemos que la punta de la flecha siga al ratón
                if (modoRelacion && claseInicioRelacion != null) {
                    puntoFinRelacionTemp = e.getPoint();
                    repaint(); // redibujar para ver la línea moviéndose en vivo
                    return;
                }

                // Si tenemos una caja y estamos usando el clic izquierdo
                if (claseSeleccionadaArrastre != null && SwingUtilities.isLeftMouseButton(e)) {
                    // Actualizamos las coordenadas (X, Y) de la caja para que siga al ratón.
                    claseSeleccionadaArrastre.bounds.x = e.getX() - offsetRaton.x;
                    claseSeleccionadaArrastre.bounds.y = e.getY() - offsetRaton.y;
                    repaint(); // Ordenamos a la pizarra que borre y vuelva a dibujar todo en la nueva posición
                }
            }
        };

        //se agregan para que empiece a funcionar.
        addMouseListener(ratonAdapter);
        addMouseMotionListener(ratonAdapter);
    }

    // Método que fabrica los menús de clic derecho.
    private void crearMenuContextual() {
        menuContextual = new JPopupMenu(); // Crea el menú de las cajas
        
        JMenuItem itemEditar = new JMenuItem("Editar Nombre"); // Crea la opción Editar
        JMenuItem itemEliminar = new JMenuItem("Eliminar Clase"); // Crea la opción Eliminar

        // opción "Editar Nombre"
        itemEditar.addActionListener(e -> { // cuando haga click se ejecuta todo lo de adentro
            if (claseSeleccionadaMenu != null) { //verifica q hagamos clik der en una caja
                // Pregunta el nuevo nombre 
                String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", claseSeleccionadaMenu.nombre);
                
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) { //valida el nuevo nombre q no este vacio y sin espacios
                    claseSeleccionadaMenu.nombre = nuevoNombre.trim(); //cambia el nombre
                    repaint();
                }
            }
        });

        // opción "Eliminar Clase"
        itemEliminar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                // muestra un mensaje q Pide confirmación antes de borrar.
                int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar '" + claseSeleccionadaMenu.nombre + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) { // Si dijo que sí
                    clasesUML.remove(claseSeleccionadaMenu); // Borra la caja de la lista.
                    // Borra todas las líneas que estaban conectadas a esa caja (para que no queden líneas flotando).
                    relaciones.removeIf(rel -> rel.origen == claseSeleccionadaMenu || rel.destino == claseSeleccionadaMenu);
                    repaint(); // Actualiza el dibujo.
                }
            }
        });

        // Agregamos las opciones al menú contextual de la caja.
        menuContextual.add(itemEditar);
        menuContextual.addSeparator(); // Una linea separadora.
        menuContextual.add(itemEliminar);

        // Creamos el menú contextual de las relaciones (Líneas).
        menuContextualRelacion = new JPopupMenu();
        JMenuItem itemEliminarRel = new JMenuItem("Eliminar Relación"); //se crea su menu item
        itemEliminarRel.addActionListener(e -> {
            if (relacionSeleccionadaMenu != null) { //verifica q estemos seleccionando una linea  
                relaciones.remove(relacionSeleccionadaMenu); // Elimina la línea.
                repaint(); // Actualiza el dibujo.
            }
        });
        menuContextualRelacion.add(itemEliminarRel);
    }

    // Un radar para saber si el ratón está tocando alguna caja (se revisan de la última a la primera por si están encimadas).
    private UML_Clase obtenerClaseEnPosicion(Point p) { // contiene las cordenadas donde se hizo click
        // Recorremos de atrás para adelante por si hay cajas encimadas ya q la ultima q se crea es la del frente
        for (int i = clasesUML.size() - 1; i >= 0; i--) {
            if (clasesUML.get(i).bounds.contains(p)) { // Si la coordenada (X,Y) del ratón está dentro del rectángulo
                return clasesUML.get(i); // Devuelve esa caja.
            }
        }
        return null;
    }

    // saber si el ratón está haciendo clic sobre una línea.
    private UML_relacion obtenerRelacionEnPosicion(Point p) {
        for (UML_relacion rel : relaciones) { 
            // Calcula el centro de la caja origen (x1, y1) y de la caja destino (x2, y2).
            int cx1 = rel.origen.bounds.x + (rel.origen.bounds.width / 2);
            int cy1 = rel.origen.bounds.y + (rel.origen.bounds.height / 2);
            int cx2 = rel.destino.bounds.x + (rel.destino.bounds.width / 2);
            int cy2 = rel.destino.bounds.y + (rel.destino.bounds.height / 2);
            
            // Fórmula matemática para calcular qué tan lejos está el clic de la línea.
            double distancia = java.awt.geom.Line2D.ptSegDist(cx1, cy1, cx2, cy2, p.x, p.y);
            // Si hizo clic a 5 píxeles o menos de la línea, supone que querias darle click a la linea
            if (distancia <= 5.0) { 
                return rel;
            }
        }
        return null;
    }

    //Es el encargado de dibujar absolutamente todo.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // borra lo q se dibujo en el lienzo anterior
        Graphics2D g2d = (Graphics2D) g; // Convertimos el pincel a uno más avanzado (Graphics2D).
        g2d.setStroke(new BasicStroke(2)); // Hacemos que las líneas se dibujen con un grosor de 2 píxeles.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //renderizado para que las líneas no se vean pixeladas (suavizado).

        // Dibuja la línea temporal elástica mientras la arrastramos con el ratón
        if (modoRelacion && claseInicioRelacion != null && puntoFinRelacionTemp != null) { //verifica si estas en proceso de crear una relacion
            int cx1 = claseInicioRelacion.bounds.x + (claseInicioRelacion.bounds.width / 2);  //calcula el centro de la caja origen
            int cy1 = claseInicioRelacion.bounds.y + (claseInicioRelacion.bounds.height / 2); 
           
            if (tipoRelacionPendiente == UML_relacion.Tipo.HERENCIA) {  // Si elegimos herencia ...
                g2d.setColor(Color.BLUE); // ...sera azul
            } else {
                g2d.setColor(Color.GRAY); //sino sera una linea simple y gris
            }
            // Traza la línea desde el centro de la caja origen hasta donde esté el ratón temporalmente
            g2d.drawLine(cx1, cy1, puntoFinRelacionTemp.x, puntoFinRelacionTemp.y);
        }

        // Creamos una lista de las madres únicas que existen en la pizarra para asignarles colores
        List<UML_Clase> listaMadres = new ArrayList<>();
        for (UML_relacion rel : relaciones) { 
            // Si hay una herencia y esta madre aún no está en la lista, la agregamos
            if (rel.tipo == UML_relacion.Tipo.HERENCIA && !listaMadres.contains(rel.destino)) {
                listaMadres.add(rel.destino);
            }
        }

        // Paleta de colores para diferenciar las madres (Celeste, Rosa, Verde, Lila, Naranja)
        Color[] paletaMadres = {
            new Color(220, 240, 255), 
            new Color(255, 220, 220), 
            new Color(220, 255, 220), 
            new Color(240, 220, 255), 
            new Color(255, 235, 205)  
        };

        // 1. DIBUJAMOS TODAS LAS LÍNEAS OFICIALES (Van primero para que las cajas las tapen si pasan por detrás)
        for (UML_relacion rel : relaciones) {
            // Calculamos el centro de las dos cajas que se van a conectar.
            int cx1 = rel.origen.bounds.x + (rel.origen.bounds.width / 2);
            int cy1 = rel.origen.bounds.y + (rel.origen.bounds.height / 2);
            int cx2 = rel.destino.bounds.x + (rel.destino.bounds.width / 2);
            int cy2 = rel.destino.bounds.y + (rel.destino.bounds.height / 2);

            // Si es una línea de herencia
            if (rel.tipo == UML_relacion.Tipo.HERENCIA) {
                g2d.setColor(Color.BLUE); // El pincel será azul.
                // Averiguamos dónde toca la línea con la "pared" de la caja destino para no tapar la flecha.
                Point bordeDestino = obtenerInterseccionBorde(cx1, cy1, cx2, cy2, rel.destino.bounds);
                // Calculamos el ángulo de inclinación de la flecha.
                double angulo = Math.atan2(bordeDestino.y - cy1, bordeDestino.x - cx1);
                
                // Dibujamos la línea azul hasta el borde.
                g2d.drawLine(cx1, cy1, bordeDestino.x, bordeDestino.y);
                // Dibujamos el triángulo apuntando en la dirección correcta.
                dibujarTriangulo(g2d, bordeDestino.x, bordeDestino.y, angulo); 
            } else { // Si es una línea normal
                g2d.setColor(Color.GRAY); // El pincel será gris.
                g2d.drawLine(cx1, cy1, cx2, cy2); // Dibuja una línea de centro a centro.
            }
        }

        // 2. DIBUJAMOS TODAS LAS CAJAS (Clases)
        for (UML_Clase uml : clasesUML) {
            // Calculamos la altura de cada sección dependiendo de cuánto texto tengan.
            int altoTitulo = 30;
            int altoAtributos = Math.max(30, uml.atributos.size() * 15 + 10); // 15 px por cada renglón de texto.
            int altoMetodos = Math.max(30, uml.metodos.size() * 15 + 10);
            uml.bounds.height = altoTitulo + altoAtributos + altoMetodos; // La caja se estira automáticamente.

            Color colorFondo = new Color(255, 255, 220); // Amarillo pastel por defecto
            
            // Verificamos si esta clase está en nuestra lista de madres
            int indexMadre = listaMadres.indexOf(uml);
            if (indexMadre != -1) {
                // Si es madre, le asignamos un color de nuestra paleta basado en su posición
                colorFondo = paletaMadres[indexMadre % paletaMadres.length];
            }

            g2d.setColor(colorFondo); //selecciona el amarillo o color de la paleta
            g2d.fill(uml.bounds); //lo pinta
            
            g2d.setColor(Color.BLACK);//cambia el pincel a negro...
            g2d.draw(uml.bounds); //...y pinta el contorno de negro

            g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Escribimos el nombre de la clase (En Negrita y arial).
            g2d.drawString(uml.nombre, uml.bounds.x + 10, uml.bounds.y + 20);
            // Dibujamos la raya que separa el nombre de los atributos.
            g2d.drawLine(uml.bounds.x, uml.bounds.y + altoTitulo, uml.bounds.x + uml.bounds.width, uml.bounds.y + altoTitulo);

            g2d.setFont(new Font("Arial", Font.PLAIN, 12)); // Cambiamos a letra normal para escribir los atributos.
            int yActual = uml.bounds.y + altoTitulo + 15; 
            for (String attr : uml.atributos) { // Por cada atributo
                g2d.drawString(attr, uml.bounds.x + 10, yActual); // Lo dibuja en la pantalla.
                yActual += 15; // Baja al siguiente renglón.
            }
            // Dibujamos la raya que separa los atributos de los métodos.
            int lineaSeparadoraY = uml.bounds.y + altoTitulo + altoAtributos;
            g2d.drawLine(uml.bounds.x, lineaSeparadoraY, uml.bounds.x + uml.bounds.width, lineaSeparadoraY);

            // Hacemos lo mismo con los métodos, escribiéndolos renglón por renglón.
            yActual = lineaSeparadoraY + 15;
            for (String met : uml.metodos) { //por cada metodo 
                g2d.drawString(met, uml.bounds.x + 10, yActual); //lo dibuja en la pantalla
                yActual += 15; //baja al siguiente reglon
            }
        }
    }
    //PARTE DE CHEL
    // Calcula el punto exacto donde la línea choca con el marco exterior de la caja para que la flecha no quede por debajo.
    private Point obtenerInterseccionBorde(int x1, int y1, int x2, int y2, Rectangle rect) {
        double dx = x1 - x2; // Diferencia horizontal.
        double dy = y1 - y2; // Diferencia vertical.
        double w = rect.width / 2.0; // Mitad del ancho de la caja.
        double h = rect.height / 2.0; // Mitad del alto de la caja.

        double crossX = w * Math.signum(dx); // Lado izquierdo o derecho.
        double crossY = h * Math.signum(dy); // Lado superior o inferior.

        // Decide si la línea cruzará primero por el techo/piso de la caja, o por los muros laterales.
        if (Math.abs(dx * h) > Math.abs(dy * w)) {
            crossY = (dy / dx) * crossX;
        } else {
            crossX = (dx / dy) * crossY;
        }
        return new Point((int) (x2 + crossX), (int) (y2 + crossY)); // Devuelve el punto de choque exacto.
    }

    // Dibuja un triángulo blanco con borde azul (flecha de herencia)
    private void dibujarTriangulo(Graphics2D g2d, int x, int y, double angulo) {
        int tam = 16; // Tamaño de las patitas del triángulo.
        
        // Uso de Seno y Coseno para calcular las dos esquinas traseras del triángulo usando el ángulo de inclinación de la línea.
        int x2 = (int) (x - tam * Math.cos(angulo - Math.PI / 6));
        int y2 = (int) (y - tam * Math.sin(angulo - Math.PI / 6));
        int x3 = (int) (x - tam * Math.cos(angulo + Math.PI / 6));
        int y3 = (int) (y - tam * Math.sin(angulo + Math.PI / 6));

        // Unimos los 3 puntos en una figura cerrada (polígono).
        Polygon triangulo = new Polygon(new int[]{x, x2, x3}, new int[]{y, y2, y3}, 3);
        
        // Lo pintamos de blanco por dentro para que tape la línea gris que pasa por detrás.
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(triangulo); 
        // Le pintamos el contorno azul.
        g2d.setColor(Color.BLUE);
        g2d.drawPolygon(triangulo); 
    }
}