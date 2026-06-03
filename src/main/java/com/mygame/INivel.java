package com.mygame;

public interface INivel {
    /**
     * Construye el nivel instanciando los obstáculos necesarios.
     * @param lc El LevelController que provee las herramientas de posicionamiento.
     * @param offsetZ La profundidad base de la habitación actual.
     */
    void construirNivel(LevelController lc, float offsetZ);
}
