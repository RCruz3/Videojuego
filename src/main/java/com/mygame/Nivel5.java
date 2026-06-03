package com.mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Nivel5 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        // Tamaños de plataformas
        Vector3f tChico = new Vector3f(2.0f, 0.5f, 2.0f);     // 4x4 metros
        Vector3f tMediano = new Vector3f(3.5f, 0.5f, 3.0f);   // 7x6 metros
        Vector3f tLargo = new Vector3f(5.0f, 0.5f, 2.0f);     // 10x4 metros
        Vector3f tCuadrado = new Vector3f(3.0f, 0.5f, 3.0f);  // 6x6 metros

        // ================
        // LÁSERES MÓVILES
        // =================
        Quaternion rotacionLaser = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        lc.configurarLaser(0, new Vector3f(0f, -24.5f, offsetZ), rotacionLaser, 19f, 2.0f);
        lc.configurarLaser(1, new Vector3f(0f,  24.5f, offsetZ), rotacionLaser, 19f, 2.0f);

        // =========
        // FILA Z 1
        // =========
        // --- SUELO ---
        lc.construirSueloHolografico(new Vector3f(-18f, -22f, offsetZ + 16f), tCuadrado, true); 
        lc.construirSueloHolografico(new Vector3f(-9f, -22f, offsetZ + 16f), tChico, false);    
        lc.construirSueloHolografico(new Vector3f(1f, -22f, offsetZ + 16f), tLargo, true);      
        lc.construirSueloHolografico(new Vector3f(15f, -22f, offsetZ + 16f), tMediano, true);   
        
        // LLAVE 1: Resguarda en el único bloque seguro del suelo inicial (Izquierda)
        lc.configurarTarjeta(0, new Vector3f(-9f, -19f, offsetZ + 16f));

        // ===========
        // FILA Z 1.5
        // ===========
        // --- TECHO ---
        lc.construirSueloHolografico(new Vector3f(-16f, 22f, offsetZ + 11f), tMediano, false);  
        lc.construirSueloHolografico(new Vector3f(-2f, 22f, offsetZ + 11f), tCuadrado, true);   
        lc.construirSueloHolografico(new Vector3f(8f, 22f, offsetZ + 11f), tChico, false);      
        lc.construirSueloHolografico(new Vector3f(18f, 22f, offsetZ + 11f), tLargo, false);     

        // =========
        // FILA Z 2
        // =========
        // --- SUELO ---
        lc.construirSueloHolografico(new Vector3f(-17f, -22f, offsetZ + 4f), tLargo, true);     
        lc.construirSueloHolografico(new Vector3f(-4f, -22f, offsetZ + 4f), tMediano, false);    
        lc.construirSueloHolografico(new Vector3f(6f, -22f, offsetZ + 4f), tCuadrado, true);    
        lc.construirSueloHolografico(new Vector3f(17f, -22f, offsetZ + 4f), tChico, true);      

        // ============
        // FILA Z 2.5
        // ============
        // --- TECHO ---
        lc.construirSueloHolografico(new Vector3f(-18f, 22f, offsetZ - 2f), tChico, false);     
        lc.construirSueloHolografico(new Vector3f(-9f, 22f, offsetZ - 2f), tLargo, false);      
        lc.construirSueloHolografico(new Vector3f(3f, 22f, offsetZ - 2f), tMediano, true);      
        lc.construirSueloHolografico(new Vector3f(15f, 22f, offsetZ - 2f), tCuadrado, false);   
        
        // LLAVE 2: Flotando bajo la plataforma segura del techo derecho central
        lc.configurarTarjeta(1, new Vector3f(3f, 18f, offsetZ - 2f));

        // =========
        // FILA Z 3
        // =========
        // --- SUELO ---
        lc.construirSueloHolografico(new Vector3f(-16f, -22f, offsetZ - 9f), tMediano, false);   
        lc.construirSueloHolografico(new Vector3f(-5f, -22f, offsetZ - 9f), tChico, true);      
        lc.construirSueloHolografico(new Vector3f(4f, -22f, offsetZ - 9f), tLargo, true);       
        lc.construirSueloHolografico(new Vector3f(16f, -22f, offsetZ - 9f), tCuadrado, false);   

        // =========
        // FILA Z 3.5
        // =========
        // --- TECHO ---
        lc.construirSueloHolografico(new Vector3f(-17f, 22f, offsetZ - 16f), tCuadrado, false);  
        lc.construirSueloHolografico(new Vector3f(-6f, 22f, offsetZ - 16f), tChico, false);     
        lc.construirSueloHolografico(new Vector3f(3f, 22f, offsetZ - 16f), tLargo, false);      
        lc.construirSueloHolografico(new Vector3f(16f, 22f, offsetZ - 16f), tMediano, true);   
        
        // LLAVE 3: Escondida al revés en el extremo izquierdo profundo del techo
        lc.configurarTarjeta(2, new Vector3f(-17f, 18f, offsetZ - 16f));

        // =========
        // FILA Z 4
        // =========
        // --- SUELO ---
        lc.construirSueloHolografico(new Vector3f(-10f, -22f, offsetZ - 21f), tChico, true);    
        lc.construirSueloHolografico(new Vector3f(0f, -22f, offsetZ - 21f), tMediano, false);    
        lc.construirSueloHolografico(new Vector3f(10f, -22f, offsetZ - 21f), tChico, true);     

        
    }
}