package Grafica; // Indica que este archivo pertenece a la carpeta visual o gráfica de tu proyecto.

// Traemos los moldes de nuestros datos y las herramientas de Java necesarias para dibujar y detectar el ratón
import Logica_de_salida.UML_Clase; // Trae el molde para crear las cajas de las clases
import Logica_de_salida.UML_relacion; // Trae el molde para crear las flechas/líneas
import javax.swing.*; // Herramientas visuales de ventanas y menús.
import java.awt.*; // Herramientas de dibujo (pinceles, colores, coordenadas).
import java.awt.event.*; // Herramientas para detectar clics y movimientos del ratón.
import java.util.ArrayList; // Herramienta para crear listas que pueden crecer o encogerse.
import java.util.List;

// UMLCanvas "hereda" de JPanel, lo que significa que esta clase ES un lienzo o pizarra en blanco
public class UMLCanvas extends JPanel {
    
    // Creamos dos listas principales para guardar todo lo que dibujemos
    public List<UML_Clase> clasesUML = new ArrayList<>(); // Lista donde se guardan todas las cajas (clases).
    public List<UML_relacion> relaciones = new ArrayList<>(); // Lista donde se guardan todas las líneas (relaciones).
    
    // Variables de memoria para saber qué estamos tocando con el ratón en cada momento
    private UML_Clase claseSeleccionadaArrastre = null; // Recuerda qué caja estamos arrastrando
    private UML_Clase claseSeleccionadaMenu = null; // Recuerda a qué caja le hicimos clic derecho
    private UML_relacion relacionSeleccionadaMenu = null; // Recuerda a qué línea le hicimos clic derecho
    private Point offsetRaton; // Recuerda en qué parte exacta de la caja hicimos clic para que el arrastre sea suave
    
    // Variables para los menús desplegables (los que salen al hacer clic derecho)
    private JPopupMenu menuContextual; // El menú para las cajas (Editar/Eliminar)
    private JPopupMenu menuContextualRelacion; // El menú para las líneas (Eliminar)

    // Variables exclusivas para la función de dibujar y arrastrar la línea con el ratón
    public boolean modoRelacion = false; // Indica si estamos en "modo dibujo"
    public UML_relacion.Tipo tipoRelacionPendiente = null; // Guarda si dibujaremos línea normal o herencia
    private UML_Clase claseInicioRelacion = null; // Memoriza la caja de donde sale la flecha
    private Point puntoFinRelacionTemp = null; // Marca a dónde apunta el ratón mientras arrastramos

    // CONSTRUCTOR: Se ejecuta cuando se crea la pizarra por primera vez.
    public UMLCanvas() {
        setBackground(Color.WHITE); // Pintamos el fondo de la pizarra de color blanco
        crearMenuContextual(); // Llamamos a la función que prepara los menús de clic derecho.

        // Creamos un "vigilante" (MouseAdapter) que estará atento a todo lo que haga el ratón
        MouseAdapter ratonAdapter = new MouseAdapter() {
            
            // ¿Qué pasa cuando el usuario PRESIONA un botón del ratón?
            @Override
            public void mousePressed(MouseEvent e) {
                // Buscamos si justo donde hizo clic hay alguna caja.
                UML_Clase claseClickeada = obtenerClaseEnPosicion(e.getPoint());

                // Si el botón de "Crear Relación" activó el modo dibujo, el ratón actúa como lápiz
                if (modoRelacion && SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) {
                        claseInicioRelacion = claseClickeada; // Memoriza desde qué caja empezamos a dibujar
                        puntoFinRelacionTemp = e.getPoint(); // Marca el punto inicial del dibujo
                    }
                    return; // Bloquea el resto de acciones (para no arrastrar la caja por error)
                }

                // Si fue un CLIC DERECHO...
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (claseClickeada != null) { // Si tocó una caja...
                        claseSeleccionadaMenu = claseClickeada; // Memoriza la caja.
                        menuContextual.show(e.getComponent(), e.getX(), e.getY()); // Muestra el menú de la caja.
                    } else { // Si no tocó ninguna caja...
                        // Busca si tocó alguna línea.
                        UML_relacion relClickeada = obtenerRelacionEnPosicion(e.getPoint());
                        if (relClickeada != null) { // Si tocó una línea...
                            relacionSeleccionadaMenu = relClickeada; // Memoriza la línea.
                            menuContextualRelacion.show(e.getComponent(), e.getX(), e.getY()); // Muestra el menú de la línea.
                        }
                    }
                } 
                // Si fue un CLIC IZQUIERDO...
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (claseClickeada != null) { // Si tocó una caja
                        claseSeleccionadaArrastre = claseClickeada; // La memoriza para arrastrarla
                        // Calcula la distancia entre el clic y la esquina de la caja para que no "salte" al moverla.
                        offsetRaton = new Point(e.getX() - claseClickeada.bounds.x, e.getY() - claseClickeada.bounds.y);
                    }
                }
            }

            // ¿Qué pasa cuando el usuario SUELTA el botón del ratón?
            @Override
            public void mouseReleased(MouseEvent e) {
                // Si estábamos arrastrando una flecha y soltamos el botón
                if (modoRelacion && claseInicioRelacion != null) {
                    UML_Clase claseDestino = obtenerClaseEnPosicion(e.getPoint()); // Revisa dónde soltamos el ratón
                    
                    // Si soltaste el ratón sobre otra clase válida (y no sobre la misma caja)
                    if (claseDestino != null && claseDestino != claseInicioRelacion) {
                        
                        // Guardia de seguridad: Verificamos si ya existe esta relación
                        boolean relacionYaExiste = false;
                        for (UML_relacion rel : relaciones) {
                            if ((rel.origen == claseInicioRelacion && rel.destino == claseDestino) || 
                                (rel.origen == claseDestino && rel.destino == claseInicioRelacion)) {
                                relacionYaExiste = true;
                                break;
                            }
                        }

                        if (relacionYaExiste) {
                            // Si ya existe, lanza un aviso.
                            JOptionPane.showMessageDialog(UMLCanvas.this, "Ya existe una línea o flecha conectando estas dos clases.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        } else {
                            // Si no existe, creamos la flecha oficialmente y la guardamos
                            relaciones.add(new UML_relacion(claseInicioRelacion, claseDestino, tipoRelacionPendiente));
                            
                            // Si acabamos de crear una herencia, avisamos al usuario del cambio de color
                            if (tipoRelacionPendiente == UML_relacion.Tipo.HERENCIA) {
                                JOptionPane.showMessageDialog(UMLCanvas.this, 
                                    "¡La clase '" + claseDestino.nombre + "' ahora es una MADRE!\nSu caja cambiará a un color único para diferenciarla de las demás.", 
                                    "Herencia Creada", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                    
                    // Apagamos el modo dibujo de líneas y limpiamos la memoria temporal
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
                    repaint(); // Ordenamos redibujar para ver la línea moviéndose en vivo
                    return;
                }

                // Si tenemos una caja memorizada y estamos usando el clic izquierdo
                if (claseSeleccionadaArrastre != null && SwingUtilities.isLeftMouseButton(e)) {
                    // Actualizamos las coordenadas (X, Y) de la caja para que siga al ratón.
                    claseSeleccionadaArrastre.bounds.x = e.getX() - offsetRaton.x;
                    claseSeleccionadaArrastre.bounds.y = e.getY() - offsetRaton.y;
                    repaint(); // Ordenamos a la pizarra que borre y vuelva a dibujar todo en la nueva posición
                }
            }
        };

        // Le asignamos el vigilante a la pizarra para que empiece a funcionar.
        addMouseListener(ratonAdapter);
        addMouseMotionListener(ratonAdapter);
    }

    // Método que fabrica los menús invisibles que esperan a que hagas clic derecho.
    private void crearMenuContextual() {
        menuContextual = new JPopupMenu(); // Crea el menú de las cajas
        
        JMenuItem itemEditar = new JMenuItem("Editar Nombre"); // Crea la opción Editar
        JMenuItem itemEliminar = new JMenuItem("Eliminar Clase"); // Crea la opción Eliminar

        // Le damos comportamiento a la opción "Editar Nombre"
        itemEditar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                // Pregunta el nuevo nombre mostrando el nombre actual por defecto.
                String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", claseSeleccionadaMenu.nombre);
                // Si escribió algo válido, se lo cambia y vuelve a dibujar.
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    claseSeleccionadaMenu.nombre = nuevoNombre.trim();
                    repaint();
                }
            }
        });

        // Le damos comportamiento a la opción "Eliminar Clase"
        itemEliminar.addActionListener(e -> {
            if (claseSeleccionadaMenu != null) {
                // Pide confirmación antes de borrar.
                int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar '" + claseSeleccionadaMenu.nombre + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) { // Si dijo que sí
                    clasesUML.remove(claseSeleccionadaMenu); // Borra la caja de la lista.
                    // Borra todas las líneas que estaban conectadas a esa caja (para que no queden líneas flotando).
                    relaciones.removeIf(rel -> rel.origen == claseSeleccionadaMenu || rel.destino == claseSeleccionadaMenu);
                    repaint(); // Actualiza el dibujo.
                }
            }
        });

        // Agregamos las opciones al menú de la caja.
        menuContextual.add(itemEditar);
        menuContextual.addSeparator(); // Una rayita de adorno.
        menuContextual.add(itemEliminar);

        // Creamos el menú de las relaciones (Líneas).
        menuContextualRelacion = new JPopupMenu();
        JMenuItem itemEliminarRel = new JMenuItem("Eliminar Relación");
        itemEliminarRel.addActionListener(e -> {
            if (relacionSeleccionadaMenu != null) {
                relaciones.remove(relacionSeleccionadaMenu); // Elimina la línea.
                repaint(); // Actualiza el dibujo.
            }
        });
        menuContextualRelacion.add(itemEliminarRel);
    }

    // Un radar para saber si el ratón está tocando alguna caja (se revisan de la última a la primera por si están encimadas).
    private UML_Clase obtenerClaseEnPosicion(Point p) {
        for (int i = clasesUML.size() - 1; i >= 0; i--) {
            if (clasesUML.get(i).bounds.contains(p)) { // Si la coordenada (X,Y) del ratón está dentro del rectángulo
                return clasesUML.get(i); // Devuelve esa caja.
            }
        }
        return null;
    }

    // Un radar matemático para saber si el ratón está haciendo clic sobre una línea.
    private UML_relacion obtenerRelacionEnPosicion(Point p) {
        for (UML_relacion rel : relaciones) {
            // Calcula el centro de la caja origen (x1, y1) y de la caja destino (x2, y2).
            int cx1 = rel.origen.bounds.x + (rel.origen.bounds.width / 2);
            int cy1 = rel.origen.bounds.y + (rel.origen.bounds.height / 2);
            int cx2 = rel.destino.bounds.x + (rel.destino.bounds.width / 2);
            int cy2 = rel.destino.bounds.y + (rel.destino.bounds.height / 2);
            
            // Fórmula matemática para calcular qué tan lejos está el clic de la línea imaginaria.
            double distancia = java.awt.geom.Line2D.ptSegDist(cx1, cy1, cx2, cy2, p.x, p.y);
            // Si hizo clic a 5 píxeles o menos de la línea, la damos por tocada.
            if (distancia <= 5.0) { 
                return rel;
            }
        }
        return null;
    }

    //Es el encargado de dibujar absolutamente todo.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Limpia la pizarra antes de dibujar.
        Graphics2D g2d = (Graphics2D) g; // Convertimos el pincel a uno más avanzado (Graphics2D).
        g2d.setStroke(new BasicStroke(2)); // Hacemos que las líneas se dibujen con un grosor de 2 píxeles.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Filtro para que las líneas no se vean pixeladas (suavizado).

        // Dibuja la línea temporal elástica mientras la arrastramos con el ratón
        if (modoRelacion && claseInicioRelacion != null && puntoFinRelacionTemp != null) {
            int cx1 = claseInicioRelacion.bounds.x + (claseInicioRelacion.bounds.width / 2);
            int cy1 = claseInicioRelacion.bounds.y + (claseInicioRelacion.bounds.height / 2);
            
            // Si elegimos herencia será azul, si es normal será gris
            if (tipoRelacionPendiente == UML_relacion.Tipo.HERENCIA) {
                g2d.setColor(Color.BLUE);
            } else {
                g2d.setColor(Color.GRAY);
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

        // Paleta de colores para diferenciar las familias/madres (Celeste, Rosa, Verde, Lila, Naranja)
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
                // Si es madre, le asignamos un color único de nuestra paleta basado en su posición
                colorFondo = paletaMadres[indexMadre % paletaMadres.length];
            }

            // Dibujamos el rectángulo con el color correspondiente (amarillo o el color de madre)
            g2d.setColor(colorFondo); 
            g2d.fill(uml.bounds);
            // Le pintamos el borde de negro.
            g2d.setColor(Color.BLACK);
            g2d.draw(uml.bounds); 

            // Escribimos el nombre de la clase (En Negrita).
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(uml.nombre, uml.bounds.x + 10, uml.bounds.y + 20);
            // Dibujamos la raya que separa el nombre de los atributos.
            g2d.drawLine(uml.bounds.x, uml.bounds.y + altoTitulo, uml.bounds.x + uml.bounds.width, uml.bounds.y + altoTitulo);

            // Cambiamos a letra normal para escribir los atributos.
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            int yActual = uml.bounds.y + altoTitulo + 15; // Coordenada de altura inicial para escribir.
            for (String attr : uml.atributos) { // Por cada atributo
                g2d.drawString(attr, uml.bounds.x + 10, yActual); // Lo dibuja en la pantalla.
                yActual += 15; // Baja al siguiente renglón.
            }
            // Dibujamos la raya que separa los atributos de los métodos.
            int lineaSeparadoraY = uml.bounds.y + altoTitulo + altoAtributos;
            g2d.drawLine(uml.bounds.x, lineaSeparadoraY, uml.bounds.x + uml.bounds.width, lineaSeparadoraY);

            // Hacemos lo mismo con los métodos, escribiéndolos renglón por renglón.
            yActual = lineaSeparadoraY + 15;
            for (String met : uml.metodos) {
                g2d.drawString(met, uml.bounds.x + 10, yActual);
                yActual += 15;
            }
        }
    }

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