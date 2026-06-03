package com.mygame;

public class Nivel9 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();
        
        // El jugador solo camina por el pasillo hacia la luz.
    }
}
