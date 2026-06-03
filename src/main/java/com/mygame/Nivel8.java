package com.mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Nivel8 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        Vector3f tChico = new Vector3f(2.0f, 0.5f, 2.0f);     
        Vector3f tMediano = new Vector3f(3.5f, 0.5f, 3.0f);   


        // ==========================================
        // MATRIZ DE 4 LÁSERES
        // ==========================================
        Quaternion rotacionLaser = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        lc.configurarLaser(0, new Vector3f(0f, -24.5f, offsetZ), rotacionLaser, 20f, 0.5f); // Suelo rápido
        lc.configurarLaser(1, new Vector3f(0f,  24.5f, offsetZ), rotacionLaser, 20f, 0.5f); // Techo rápido
        lc.configurarLaser(2, new Vector3f(0f, -15.0f, offsetZ), rotacionLaser, 15f, -1.8f); // Medio-Bajo inverso
        lc.configurarLaser(3, new Vector3f(0f,  15.0f, offsetZ), rotacionLaser, 15f, -2.1f); // Medio-Alto inverso

        // ==========================================
        // ZONA 1: EL PUENTE QUE SE DESMORONA (Z: +14 a +6)
        // ==========================================
        lc.construirSueloHolografico(new Vector3f(-6f, -22f, offsetZ + 14f), tChico, false, true);
        lc.construirSueloHolografico(new Vector3f(6f, 22f, offsetZ + 10f), tChico, true, true);
        lc.construirSueloHolografico(new Vector3f(-6f, -22f, offsetZ + 6f), tChico, false, true);
        
        lc.configurarTarjeta(0, new Vector3f(-6f, -18f, offsetZ + 6f)); // Llave 1 

        // ==========================================
        // ZONA 2: EL COLISEO DE DRONES
        // ==========================================
        // Red dispersa de plataformas
        lc.construirSueloHolografico(new Vector3f(8f, -22f, offsetZ - 2f), tChico, false, true);  // Suelo Der (Seguro, se quema)
        lc.construirSueloHolografico(new Vector3f(-8f, 22f, offsetZ - 3f), tChico, true, true);   // Techo Izq (Seguro, se quema)
        
        lc.construirSueloHolografico(new Vector3f(-3f, -22f, offsetZ - 6f), tMediano, true, false); // Suelo Izq (Trampa)
        lc.construirSueloHolografico(new Vector3f(4f, 22f, offsetZ - 7f), tChico, false, false);    // Techo Der (Trampa)
        
        lc.construirSueloHolografico(new Vector3f(8f, 22f, offsetZ - 10f), tChico, true, true);    // Techo Der Profundo (Seguro, se quema)
        lc.construirSueloHolografico(new Vector3f(-8f, -22f, offsetZ - 10f), tChico, false, true); // Suelo Izq Profundo (Seguro, se quema)
        
       
        lc.configurarTarjeta(1, new Vector3f(0f, 0f, offsetZ - 6f)); 

        // 4 Drones rodeando la zona de la llave
        lc.configurarDron(new Vector3f(12f, -18f, offsetZ - 5f));
        lc.configurarDron(new Vector3f(-12f, 18f, offsetZ - 5f));
        lc.configurarDron(new Vector3f(12f, 18f, offsetZ - 5f));
        lc.configurarDron(new Vector3f(-12f, -18f, offsetZ - 5f));

        // ==========================================
        // ZONA 3
        // ==========================================
        lc.construirSueloHolografico(new Vector3f(0f, -22f, offsetZ - 16f), tMediano, false, true);
        lc.configurarTarjeta(2, new Vector3f(0f, -18f, offsetZ - 16f));
        
        // Dos drones como porteros tapando la salida
        lc.configurarDron(new Vector3f(5f, -18f, offsetZ - 20f));
        lc.configurarDron(new Vector3f(-5f, -18f, offsetZ - 20f));
    }
}
