package Logica_de_salida;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
//-La importación java.awt.Rectangle permite utilizar una clase que define un área rectangular en un espacio
//de coordenadas bidimensional. Se utiliza principalmente para modelar, gestionar y manipular formas 
//rectangulares en el desarrollo de interfaces gráficas (GUI) o juegos en Java. 

public class UML_Clase {
    
    public String nombre;
    public List<String> atributos = new ArrayList<>();
    public List<String> metodos = new ArrayList<>();
    public Rectangle bounds;
    
    public UML_Clase(String nombre,int x,int y){
        this.nombre = nombre;
        this.bounds = new Rectangle(x,y,160,100); 
        //crea un nuevo objeto rectangular y le asigan una nueva propiedad, define la base y la altura,
        //acompañado de los pixeles para cada uno de ellos
    }
            
}
