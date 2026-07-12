package Logica_de_salida;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class UML_Clase {
    
    String nombre;
    List<String> atributos = new ArrayList<>();
    List<String> metodos = new ArrayList<>();
    Rectangle bounds;
    
    public UML_Clase(String nombre,int x,int y){
        this.nombre = nombre;
        this.bounds = new Rectangle(x,y,160,100); 
    }
            
}
