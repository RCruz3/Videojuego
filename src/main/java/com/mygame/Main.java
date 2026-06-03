package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * Clase principal del proyecto APEX.
 * Hereda de SimpleApplication para inicializar el motor gráfico jMonkeyEngine.
 * Actúa como el punto de entrada y gestor principal de los estados del juego (State Management).
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        new Main().start(); // Inicia el bucle principal (Game Loop) del motor 3D
    }

    @Override
    public void simpleInitApp() {
        // 1. CONFIGURACIÓN INICIAL DE CÁMARA Y UI
        
        // Se desactiva la cámara de vuelo libre por defecto para tener control sobre la cámara en primera persona del jugador.
        flyCam.setEnabled(false);
        
        // Se muestra el cursor en pantalla temporalmente para la interacción 
        // con la interfaz gráfica de la pantalla de inicio ortogonal (2D).
        inputManager.setCursorVisible(true);

        
        // INICIALIZACIÓN DEL MOTOR DE FÍSICAS
        
        // Se instancia BulletAppState para manejar las colisiones, alteración de masa, gravedad y cinemática de los RigidBodyControls dentro del entorno 3D.
        BulletAppState estadoFisicas = new BulletAppState();
        stateManager.attach(estadoFisicas);

        // GESTIÓN DE ESTADOS (GAME STATE)
         
        // instanciación de niveles y controladores, separando la mecánica del núcleo base.
        GameState estadoJuego = new GameState(this, estadoFisicas);
        stateManager.attach(estadoJuego);

        // INTERACCIÓN DEL USUARIO (INPUT EVENT HANDLING)
        // Mapeo de eventos: Se asigna la tecla ENTER (estándar y teclado numérico)a la acción "IniciarSimulacion".
        inputManager.addMapping("IniciarSimulacion", 
            new KeyTrigger(KeyInput.KEY_RETURN),
            new KeyTrigger(KeyInput.KEY_NUMPADENTER)
        );

        // Listener asíncrono que detecta la interacción del usuario. Si se presiona 
        // la tecla mapeada y la simulación está en espera, dispara el arranque.
        inputManager.addListener((ActionListener) (nombreAccion, presionado, tpf) -> {
            if (nombreAccion.equals("IniciarSimulacion") && presionado) {
                if (!estadoJuego.isSimulacionIniciada()) {
                    estadoJuego.iniciarSimulacion();
                }
            }
        }, "IniciarSimulacion");
    }
}