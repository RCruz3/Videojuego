package com.mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Nivel2 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        // 1. Cubos en zonas seguras
        lc.configurarTarjeta(0, new Vector3f(-18f, -10f, offsetZ + 15f));
        lc.configurarTarjeta(1, new Vector3f(18f, 10f, offsetZ - 5f));
        lc.configurarTarjeta(2, new Vector3f(0f, 20f, offsetZ - 20f));

        // 2. Laseres en red 3D
        Quaternion rotX = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y); // Izquierda a Derecha
        Quaternion rotZ = Quaternion.IDENTITY; // Frente a Atrás

        lc.configurarLaser(0, new Vector3f(0, -15f, offsetZ + 10f), rotX);
        lc.configurarLaser(1, new Vector3f(0, 5f,   offsetZ - 2f),  rotX);
        lc.configurarLaser(2, new Vector3f(0, 14f,  offsetZ - 15f), rotX);
        lc.configurarLaser(3, new Vector3f(-10f, -5f, offsetZ),     rotZ);
        lc.configurarLaser(4, new Vector3f(12f, 18f,  offsetZ),     rotZ);

        // 3. 4 plataformas
        lc.configurarPlataforma(0, new Vector3f( 0f,   -18.0f, offsetZ + 18f));
        lc.configurarPlataforma(1, new Vector3f(-15f, -2.0f,  offsetZ + 5f));
        lc.configurarPlataforma(2, new Vector3f( 10f,  8.0f,   offsetZ - 8f));
        lc.configurarPlataforma(3, new Vector3f( 0f,   15.0f,  offsetZ - 18f));
    }
}
