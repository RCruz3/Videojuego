package com.mygame;

import com.jme3.math.Vector3f;

public class Nivel7 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        Vector3f tChico = new Vector3f(2.0f, 0.5f, 2.0f);     
        Vector3f tMediano = new Vector3f(3.5f, 0.5f, 3.0f);   
        Vector3f tLargo = new Vector3f(5.0f, 0.5f, 2.0f);     

        

        // ==========================================
        // ESTRUCTURA FÍSICA Y TARJETAS
        // ==========================================
        // Zona 1
        lc.construirSueloHolografico(new Vector3f(0f, -22f, offsetZ + 12f), tLargo, false, false);
        lc.configurarTarjeta(0, new Vector3f(0f, -18f, offsetZ + 12f)); 

        // Zona 2
        lc.construirSueloHolografico(new Vector3f(8f, 22f, offsetZ - 2f), tMediano, true, true);
        lc.configurarTarjeta(1, new Vector3f(8f, 18f, offsetZ - 2f)); 
        lc.construirSueloHolografico(new Vector3f(-8f, -22f, offsetZ - 2f), tMediano, false, true);

        // Zona 3
        lc.construirSueloHolografico(new Vector3f(0f, 22f, offsetZ - 14f), tChico, true, false);
        lc.configurarTarjeta(2, new Vector3f(0f, 18f, offsetZ - 14f)); 

        // ==========================================
        // DISTRIBUCIÓN DE DRONES 
        // ==========================================
        
        // SQUAD INFERIOR: 3 Drones abajo 
        // Custodian el suelo de manera a lo largo y ancho de la habitacion
        lc.configurarDron(new Vector3f(-14f, -18f, offsetZ + 15f)); // Lateral izquierdo inicial
        lc.configurarDron(new Vector3f(3f,   -18f, offsetZ + 1f));  // Centro de la sala
        lc.configurarDron(new Vector3f(12f,  -18f, offsetZ - 11f)); // Lateral derecho profundo

        // SQUAD SUPERIOR: 3 Drones arriba 
        // Patrullan el techo controlando las zonas de inversión gravitatoria
        lc.configurarDron(new Vector3f(13f,  18f, offsetZ + 10f));  // Lateral derecho inicial
        lc.configurarDron(new Vector3f(-11f, 18f, offsetZ - 4f));   // Lateral izquierdo medio
        lc.configurarDron(new Vector3f(-1f,  18f, offsetZ - 16f));  // Centro profundo, cerca de la salida

        
    }
}
