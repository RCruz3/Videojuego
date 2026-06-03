package com.mygame;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Nivel4 implements INivel {

    @Override
    public void construirNivel(LevelController lc, float offsetZ) {
        lc.limpiarNivel();

        // Color distintivo para el Nivel 4: Naranja/Amarillo de "Zona de Peligro"
        ColorRGBA colorPeligro = new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f); 

        // Los láseres nacen apuntando en Z. Creamos rotaciones para acostarlos o pararlos.
        Quaternion rotHorizontal = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y); // Eje X (Izquierda a Derecha)
        Quaternion rotVertical = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);   // Eje Y (Arriba a Abajo)

        // ==========================================
        // OBSTÁCULO 1: "El Salto de Entrada" (Z = +15)
        // ==========================================
        // [LÁSER 0] Horizontal cruzando todo el suelo a la altura de las rodillas.
        lc.configurarLaser(0, new Vector3f(0f, -22f, offsetZ + 15f), rotHorizontal);
        
        // Tarjeta 0: En el techo. Obliga al jugador a invertir la gravedad desde el inicio.
        lc.configurarTarjeta(0, new Vector3f(0f, 22f, offsetZ + 15f));

        // ==========================================
        // OBSTÁCULO 2: "El Cuello de Botella" (Z = +5)
        // ==========================================
        // Muro 1: Bloquea todo el suelo, asegurando que el jugador siga en el techo.
        lc.construirMuroHolografico(new Vector3f(0f, -15f, offsetZ + 5f), new Vector3f(25f, 10f, 1f), colorPeligro);
        
        // Muro 2: Bloquea la mitad IZQUIERDA del techo.
        lc.construirMuroHolografico(new Vector3f(-12.5f, 15f, offsetZ + 5f), new Vector3f(12.5f, 10f, 1f), colorPeligro);
        
        // [LÁSER 1] Vertical dividiendo exactamente el centro de la habitación.
        lc.configurarLaser(1, new Vector3f(12f, 0f, offsetZ + 5f), rotVertical);
        
        // Tarjeta 1: A salvo en el techo, del lado derecho. (Única ruta posible).
        lc.configurarTarjeta(1, new Vector3f(15f, 22f, offsetZ));

        // ==========================================
        // OBSTÁCULO 3: "La Reja de Caída" (Z = -8)
        // ==========================================
        // Muro 3: De pronto, el techo se bloquea por completo. El jugador DEBE dejarse caer.
        lc.construirMuroHolografico(new Vector3f(0f, 15f, offsetZ - 8f), new Vector3f(25f, 10f, 1f), colorPeligro);
        
        // [LÁSER 2 y 3] En plena caída, hay dos barras de láser horizontales en el aire. 
        // El jugador debe caer rápido o ajustar su posición para no achicharrarse al bajar.
        lc.configurarLaser(2, new Vector3f(0f, -21f, offsetZ - 8f), rotHorizontal);
        lc.configurarLaser(3, new Vector3f(0f, -5f, offsetZ - 8f), rotHorizontal);

        // ==========================================
        // OBSTÁCULO 4: "La Custodia Final" (Z = -18)
        // ==========================================
        // Muro 4: Bloquea la mitad DERECHA del suelo.
        lc.construirMuroHolografico(new Vector3f(12.5f, -15f, offsetZ - 18f), new Vector3f(12.5f, 10f, 1f), colorPeligro);
        
        // [LÁSER 4] Vertical a la izquierda. Deja un pasillo muy estrecho para pasar caminando.
        lc.configurarLaser(4, new Vector3f(-10f, 0f, offsetZ - 18f), rotVertical);
        //lc.configurarLaser(5, new Vector3f(0f, -30f, offsetZ - 8f), rotHorizontal);
        
        // Tarjeta 2: En la esquina izquierda del suelo, justo detrás del láser vertical.
        lc.configurarTarjeta(2, new Vector3f(-20f, -22f, offsetZ - 20f));
    }
}
