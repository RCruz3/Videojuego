package com.mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Nivel6 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        Vector3f tChico = new Vector3f(2.0f, 0.5f, 2.0f);     
        Vector3f tMediano = new Vector3f(3.5f, 0.5f, 3.0f);   
        Vector3f tLargo = new Vector3f(5.0f, 0.5f, 2.0f);     
        Vector3f tCuadrado = new Vector3f(3.0f, 0.5f, 3.0f);  

       
        // Laseres (Avanzan  mas rapido para meter mas presion)
        Quaternion rotacionLaser = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        lc.configurarLaser(0, new Vector3f(0f, -24.5f, offsetZ), rotacionLaser, 19f, 1.6f);
        lc.configurarLaser(1, new Vector3f(0f,  24.5f, offsetZ), rotacionLaser, 19f, 1.9f);

        // ==========================================
        // FILA Z 1
        // ==========================================
        // SUELO: Nota cómo las baldosas falsas (true) NO tienen desgaste porque ya te matan (false), pero las seguras (false) SÍ se queman (true).
        lc.construirSueloHolografico(new Vector3f(-18f, -22f, offsetZ + 16f), tCuadrado, true, false); 
        lc.construirSueloHolografico(new Vector3f(-9f, -22f, offsetZ + 16f), tChico, false, true);    
        lc.construirSueloHolografico(new Vector3f(1f, -22f, offsetZ + 16f), tLargo, true, false);      
        lc.construirSueloHolografico(new Vector3f(15f, -22f, offsetZ + 16f), tMediano, true, false);   
        lc.configurarTarjeta(0, new Vector3f(-9f, -19f, offsetZ + 16f)); // Llave 1

        // ==========================================
        // FILA Z 1.5
        // ==========================================
        // TECHO: Aquí las que son `true` son seguras al invertir la gravedad, por lo que a esas les activamos el desgaste térmico.
        lc.construirSueloHolografico(new Vector3f(-16f, 22f, offsetZ + 11f), tMediano, false, false);  
        lc.construirSueloHolografico(new Vector3f(-2f, 22f, offsetZ + 11f), tCuadrado, true, true);    
        lc.construirSueloHolografico(new Vector3f(8f, 22f, offsetZ + 11f), tChico, false, false);     
        lc.construirSueloHolografico(new Vector3f(18f, 22f, offsetZ + 11f), tLargo, true, true);     

        // ==========================================
        // FILA Z 2
        // ==========================================
        // SUELO
        lc.construirSueloHolografico(new Vector3f(-17f, -22f, offsetZ + 4f), tLargo, true, false);    
        lc.construirSueloHolografico(new Vector3f(-4f, -22f, offsetZ + 4f), tMediano, false, true);    
        lc.construirSueloHolografico(new Vector3f(6f, -22f, offsetZ + 4f), tCuadrado, true, false);    
        lc.construirSueloHolografico(new Vector3f(17f, -22f, offsetZ + 4f), tChico, false, true);      

        // ==========================================
        // FILA Z 2.5
        // ==========================================
        // TECHO
        lc.construirSueloHolografico(new Vector3f(-18f, 22f, offsetZ - 2f), tChico, true, true);     
        lc.construirSueloHolografico(new Vector3f(-9f, 22f, offsetZ - 2f), tLargo, false, false);      
        lc.construirSueloHolografico(new Vector3f(3f, 22f, offsetZ - 2f), tMediano, true, true);       
        lc.construirSueloHolografico(new Vector3f(15f, 22f, offsetZ - 2f), tCuadrado, false, false);   
        lc.configurarTarjeta(1, new Vector3f(3f, 18f, offsetZ - 2f)); // Llave 2

        // ==========================================
        // FILA Z 3
        // ==========================================
        // SUELO
        lc.construirSueloHolografico(new Vector3f(-16f, -22f, offsetZ - 9f), tMediano, true, false);   
        lc.construirSueloHolografico(new Vector3f(-5f, -22f, offsetZ - 9f), tChico, true, false);      
        lc.construirSueloHolografico(new Vector3f(4f, -22f, offsetZ - 9f), tLargo, false, true);       
        lc.construirSueloHolografico(new Vector3f(16f, -22f, offsetZ - 9f), tCuadrado, true, false);   

        // ==========================================
        // FILA Z 3.5
        // ==========================================
        // TECHO
        lc.construirSueloHolografico(new Vector3f(-17f, 22f, offsetZ - 16f), tCuadrado, true, true);   
        lc.construirSueloHolografico(new Vector3f(-6f, 22f, offsetZ - 16f), tChico, false, false);     
        lc.construirSueloHolografico(new Vector3f(3f, 22f, offsetZ - 16f), tLargo, true, true);      
        lc.construirSueloHolografico(new Vector3f(16f, 22f, offsetZ - 16f), tMediano, false, false);   
        lc.configurarTarjeta(2, new Vector3f(-17f, 18f, offsetZ - 16f)); // Llave 3

        // ==========================================
        // FILA Z 4
        // ==========================================
        // SUELO
        lc.construirSueloHolografico(new Vector3f(-10f, -22f, offsetZ - 21f), tChico, true, false);    
        lc.construirSueloHolografico(new Vector3f(0f, -22f, offsetZ - 21f), tMediano, false, true);    
        lc.construirSueloHolografico(new Vector3f(10f, -22f, offsetZ - 21f), tChico, true, false);     

        
    }
}