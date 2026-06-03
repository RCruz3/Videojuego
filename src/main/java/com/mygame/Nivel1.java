package com.mygame;

import com.jme3.math.Vector3f;

public class Nivel1 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        // 1. Tarjeta fácil (En el piso, justo al entrar)
        lc.configurarTarjeta(0, new Vector3f(15f, -22f, offsetZ + 20f));

        // 2. Las 8 Plataformas 
        // Las posiciones en X estan mezcladas para que se muevan "al azar"
        lc.configurarPlataforma(0, new Vector3f(-10f, -22f, offsetZ + 18f));
        lc.configurarPlataforma(1, new Vector3f(  8f, -18f, offsetZ + 12f));
        lc.configurarPlataforma(2, new Vector3f(-12f, -14f, offsetZ + 6f));
        lc.configurarPlataforma(3, new Vector3f( 14f, -10f, offsetZ + 0f));
        lc.configurarPlataforma(4, new Vector3f( -5f,  -6f, offsetZ - 6f));
        lc.configurarPlataforma(5, new Vector3f( 10f,  -2f, offsetZ - 12f));
        lc.configurarPlataforma(6, new Vector3f( -8f,   2f, offsetZ - 18f));
        lc.configurarPlataforma(7, new Vector3f(  0f,   6f, offsetZ - 24f));

        // 3. El Dispositivo de Gravedad (A mitad de camino)
        // Lo ponemos flotando sobre la Plataforma 4, en una posición segura.
        lc.configurarDispositivo(new Vector3f(-5f, -3f, offsetZ - 6f));

        // 4. Tarjeta media (Al final del recorrido de plataformas)
        // Flotando sobre la Plataforma 7, al fondo de la habitacion.
        lc.configurarTarjeta(1, new Vector3f(0f, 9f, offsetZ - 24f));

        // 5. Tarjeta dificil (En el Techo)
        // Cuando se obtiene el dispositivo (a mitad de camino), se puedes invertir la gravedad para  tomar la tarjeta
        lc.configurarTarjeta(2, new Vector3f(0f, 22f, offsetZ - 12f));
    }
}