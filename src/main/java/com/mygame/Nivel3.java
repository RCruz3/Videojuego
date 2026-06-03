package com.mygame;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class Nivel3 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        ColorRGBA colorBarrera = new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f); // Cyan Neón

        // ==========================================
        // 1. LOS MUROS LONGITUDINALES (Recortados)
        // ==========================================
        
        // Al reducir la escala Z a 15f (30 unidades totales), 
        // dejamos las primeras 10 unidades libres en la entrada y 10 en la salida.
        lc.construirMuroHolografico(new Vector3f(0f, -15f, offsetZ), new Vector3f(1f, 10f, 15f), colorBarrera);
        lc.construirMuroHolografico(new Vector3f(0f, 15f, offsetZ), new Vector3f(1f, 10f, 15f), colorBarrera);

        // ==========================================
        // 2. LOS MUROS TRANSVERSALES (El Zig-Zag)
        // ==========================================
        
        // Bloquea Suelo-Izquierdo. (Fuerza subir al Techo-Izquierdo).
        lc.construirMuroHolografico(new Vector3f(-12.5f, -15f, offsetZ + 10f), new Vector3f(12.5f, 10f, 1f), colorBarrera);

        // Bloquea Techo-Izquierdo. (Fuerza hacer el "Strafe" por el centro hacia Techo-Derecho).
        lc.construirMuroHolografico(new Vector3f(-12.5f, 15f, offsetZ + 2f), new Vector3f(12.5f, 10f, 1f), colorBarrera);

        // Bloquea Techo-Derecho. (Fuerza dejarse caer al Suelo-Derecho).
        lc.construirMuroHolografico(new Vector3f(12.5f, 15f, offsetZ - 8f), new Vector3f(12.5f, 10f, 1f), colorBarrera);

        // Bloquea Suelo-Derecho justo donde termina el muro divisor central.
        lc.construirMuroHolografico(new Vector3f(12.5f, -15f, offsetZ - 15f), new Vector3f(12.5f, 10f, 1f), colorBarrera);

        // ==========================================
        // 3. TARJETAS DE DATOS (Ruta ajustada)
        // ==========================================
        
        // Al inicio, en la zona segura Izquierda, justo antes del primer bloqueo.
        lc.configurarTarjeta(0, new Vector3f(-15f, -22f, offsetZ + 18f)); 
        
        // En el Techo Derecho, justo después de cruzar flotando.
        lc.configurarTarjeta(1, new Vector3f(15f, 22f, offsetZ)); 
        
        // En la zona de salida, en el suelo.
        lc.configurarTarjeta(2, new Vector3f(-15f, -22f, offsetZ - 20f)); 
    }
}
