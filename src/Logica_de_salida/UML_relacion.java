package Logica_de_salida;

public class UML_relacion {
    
    enum Tipo{SIMPLE,HERENCIA}
    //define un tipo de dato enumerado con dos valores constantes
    
    UML_Clase origen;
    UML_Clase destino;
    Tipo tipo;

    public UML_relacion(UML_Clase origen, UML_Clase destino, Tipo tipo) {
        this.origen = origen;
        this.destino = destino;
        this.tipo = tipo;
    }
    
}
