package Logica_de_salida;

public class UML_relacion {
    
    public enum Tipo{SIMPLE,HERENCIA}
    //define un tipo de dato enumerado con dos valores constantes
    
    public UML_Clase origen;
    public UML_Clase destino;
    public Tipo tipo;

    public UML_relacion(UML_Clase origen, UML_Clase destino, Tipo tipo) {
        this.origen = origen;
        this.destino = destino;
        this.tipo = tipo;
    }
    
}
