package com.mygame;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.scene.Spatial;

/**
 * CollisionController - Gestor Asíncrono de Colisiones (Observer Pattern).
 * Actúa como un "Listener" que se suscribe al motor físico (Bullet Physics) 
 * para detectar e interpretar interacciones entre los RigidBodyControls del entorno.
 * Mantiene el Principio de Responsabilidad Única delegando las acciones al GameState.
 */
public class CollisionController implements PhysicsCollisionListener {
    private GameState estadoJuego;
    private PlayerController controladorJugador;
    private LevelController controladorNivel;

    // Inyección de dependencias para mantener el acceso a los controladores principales
    public CollisionController(GameState estadoJuego, PlayerController controladorJugador, LevelController controladorNivel) {
        this.estadoJuego = estadoJuego;
        this.controladorJugador = controladorJugador;
        this.controladorNivel = controladorNivel;
    }

    /**
     * Callback invocado automáticamente por el PhysicsSpace en cada frame 
     * donde se detecta una intersección de Hitboxes (AABB o envolventes complejas).
     */
    @Override
    public void collision(PhysicsCollisionEvent eventoChoque) {
        // ==========================================
        // MANEJO DE EXCEPCIONES Y TOLERANCIA A FALLOS
        // ==========================================
        // Se implementa un bloque Try-Catch como "Escudo de Ejecución".
        // Previene la caída abrupta del hilo principal (Crash) causada por colisiones 
        // fantasma (NullPointerException) cuando el Garbage Collector destruye un nodo 
        // en el mismo milisegundo en que Bullet Physics intenta reportar su colisión.
        try {
            Spatial nodeA = eventoChoque.getNodeA();
            Spatial nodeB = eventoChoque.getNodeB();

            // Validación de seguridad: Si algún objeto fue eliminado de la memoria, se ignora.
            if (nodeA == null || nodeB == null) return;

            String nombreA = nodeA.getName();
            String nombreB = nodeB.getName();

            // ==========================================
            // EVALUACIÓN DE INTERACCIONES DEL JUGADOR
            // ==========================================
            if (nombreA.equals("NodoJugador") || nombreB.equals("NodoJugador")) {
                // Identificamos cuál de los dos nodos es el objeto externo
                String objetoChocado = nombreA.equals("NodoJugador") ? nombreB : nombreA;

                // Detección de contacto con plataformas kinemáticas
                if (objetoChocado.startsWith("PlataformaLuz")) {
                    controladorJugador.sobrePlataforma = true;
                    return;
                }

                // Detección de colisión letal (Condición de Derrota)
                if (objetoChocado.startsWith("RayoLaser")) {
                    estadoJuego.manejarImpactoLaser();
                    return;
                }

                // Parseo de Strings para identificar metadatos dinámicos de las llaves
                if (objetoChocado.startsWith("TarjetaDatos_")) {
                    int indiceTarjeta = Integer.parseInt(objetoChocado.split("_")[1]);
                    if (controladorNivel.tarjetasActivas[indiceTarjeta]) {
                        estadoJuego.registrarLlaveRecolectada(indiceTarjeta);
                    }
                }
                
                // Desbloqueo de mecánica principal (Dispositivo de gravedad)
                if (objetoChocado.equals("DispositivoGravedad")) {
                    estadoJuego.obtenerModuloGravedad();
                }
            }
        } catch (Exception e) {
            // Se ignora la colisión fantasma y el game loop mantiene sus TPS/FPS estables.
        }
    }
}


