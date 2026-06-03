package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * GameState - Controlador Principal del Ciclo de Vida del Juego (State Management).
 * Hereda de BaseAppState para integrarse modularmente al motor jMonkeyEngine.
 * Coordina la comunicación entre la Interfaz de Usuario (UI), el Entorno (Nivel),
 * el Jugador, las Físicas y el Audio.
 */
public class GameState extends BaseAppState {
    private Main aplicacionPrincipal;
    private BulletAppState estadoFisicas;
    private LevelController controladorNivel;
    private PlayerController controladorJugador;
    private UIController controladorUI;
    private CollisionController controladorColisiones;
    private AudioController controladorAudio;

    private int pruebaActual = 1;
    
    // --- VARIABLES DE LÓGICA DE JUEGO ---
    private int llavesRecolectadas = 0;
    private final int LLAVES_NECESARIAS = 3;
    
    private float tiempoRestante = 100f;
    private final float TIEMPO_BASE = 90f;
    private boolean simulacionIniciada = false;
    private boolean sistemasListos = false;
    
    // Candados lógicos para control de concurrencia y flujo
    private boolean procesandoMuerte = false;
    private boolean pausadoPorInstrucciones = false;

    private float tiempoCinematica = 0f;
    private Camera camara;
    
    // --- VARIABLES DE RENDERIZADO Y TRANSICIONES ---
    private boolean mostrandoFinal = false;
    private float tiempoFinal = 0f;
    private com.jme3.scene.Geometry flashBlanco;
    private com.jme3.material.Material matBlanco; 
    private com.jme3.font.BitmapText textoFinal;

    public GameState(Main aplicacionPrincipal, BulletAppState estadoFisicas) {
        this.aplicacionPrincipal = aplicacionPrincipal;
        this.estadoFisicas = estadoFisicas;
    }

    @Override
    protected void initialize(Application app) {
        // ==========================================
        // 1. INYECCIÓN DE DEPENDENCIAS E INICIALIZACIÓN
        // ==========================================
        this.camara = app.getCamera(); 
        controladorUI = new UIController(aplicacionPrincipal);
        controladorNivel = new LevelController(aplicacionPrincipal, estadoFisicas);
        controladorJugador = new PlayerController(aplicacionPrincipal, estadoFisicas);
        controladorColisiones = new CollisionController(this, controladorJugador, controladorNivel);
        controladorAudio = new AudioController(aplicacionPrincipal.getAssetManager(), aplicacionPrincipal.getRootNode());
        
        // Registro del Listener de Colisiones en el motor físico (Bullet Physics)
        estadoFisicas.getPhysicsSpace().addCollisionListener(controladorColisiones);
        
        controladorNivel.generarHabitacion(1);
        iniciarPrueba(1);
        
        // Transformación inicial: Posicionamiento vectorial (Vector3f) del jugador
        Vector3f posicionAparicion = new Vector3f(0, -20f, controladorNivel.obtenerDesplazamientoZ() + 15f);
        controladorJugador.reaparecer(posicionAparicion);
        
        // ==========================================
        // 2. INTERACCIÓN DE USUARIO (INPUT MAPPING)
        // ==========================================
        aplicacionPrincipal.getInputManager().addMapping("ContinuarPrueba", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_SPACE));
        aplicacionPrincipal.getInputManager().addListener((com.jme3.input.controls.ActionListener) (nombreAccion, presionado, tpf) -> {
            if (nombreAccion.equals("ContinuarPrueba") && presionado) {
                if (pausadoPorInstrucciones) {
                    pausadoPorInstrucciones = false;
                    controladorUI.ocultarInstrucciones();
                    estadoFisicas.setEnabled(true); // Se reanuda el cálculo de físicas
                }
            }
        }, "ContinuarPrueba");
    }

    /**
     * Resetea las variables lógicas para el inicio de un nuevo nivel.
     * No destruye los objetos, solo reinicia sus estados matemáticos.
     */
    public void iniciarPrueba(int numeroPrueba) {
        this.pruebaActual = numeroPrueba;
        this.llavesRecolectadas = 0;
        this.tiempoRestante = TIEMPO_BASE + (numeroPrueba * 10f);

        controladorUI.actualizarFase(numeroPrueba);
        controladorUI.actualizarDatos(0, LLAVES_NECESARIAS); 
        controladorNivel.iniciarPruebaEscenario(numeroPrueba);

        controladorJugador.tieneDispositivoGravedad = (numeroPrueba != 1);  

        sistemasListos = true;
        
        if (simulacionIniciada) {
            pausadoPorInstrucciones = true;
            estadoFisicas.setEnabled(false); // Detención temporal del motor de colisiones
            controladorUI.mostrarInstrucciones(obtenerInstruccionesNivel(numeroPrueba));
        }
    }

    /**
     * Bucle Principal (Game Loop). 
     * Ejecutado en cada frame. Utiliza Delta Time (tpf) para independizar 
     * el movimiento y las animaciones de la tasa de refresco (FPS) del hardware.
     */
    @Override
    public void update(float tiempoPorFotograma) {
        if (!sistemasListos) return;
        if (pausadoPorInstrucciones) return;

        // ==========================================
        // LÓGICA DE CINEMÁTICA USANDO TRIGONOMETRÍA
        // ==========================================
        if (!simulacionIniciada) {
            controladorUI.actualizarEfectos(tiempoPorFotograma);
            tiempoCinematica += tiempoPorFotograma * 0.2f;

            // Uso de funciones Seno y Coseno (FastMath) para crear un recorrido
            // de cámara paramétrico (orbital y suave) alrededor del escenario.
            float xOffset = FastMath.sin(tiempoCinematica) * 12f;
            float yOffset = 10f + FastMath.sin(tiempoCinematica * 1.5f) * 5f; 
            float zOffset = controladorNivel.obtenerDesplazamientoZ() + FastMath.cos(tiempoCinematica) * 15f;
            
            Vector3f posicionCinematica = new Vector3f(xOffset, yOffset, zOffset);
            camara.setLocation(posicionCinematica);
            camara.lookAt(new Vector3f(0f, 0f, controladorNivel.obtenerDesplazamientoZ()), Vector3f.UNIT_Y);
            
            return;
        }
        
        if (mostrandoFinal) {
            tiempoFinal += tiempoPorFotograma;
            if (tiempoFinal > 5.0f) { 
                reiniciarAlMenuPrincipal();
            }
            return; 
        }
        
        // ==========================================
        // ÁLGEBRA Y RENDERIZADO: INTERPOLACIÓN LINEAL (LERP)
        // ==========================================
        if (pruebaActual == 9) {
            float zJugador = controladorJugador.obtenerUbicacion().z;
            float zNivel = controladorNivel.obtenerDesplazamientoZ();
            
            float zInicioFade = zNivel - 5f;  
            float zFinFade = zNivel - 22f;    

            if (zJugador < zInicioFade) {
                // Normalización de la distancia a un valor flotante entre 0.0 y 1.0
                // para manipular el canal Alpha (Transparencia) del Material gráfico.
                float alpha = 1.0f - ((zJugador - zFinFade) / (zInicioFade - zFinFade));
                alpha = FastMath.clamp(alpha, 0f, 1f); 
                
                actualizarFadeBlanco(alpha); 
                
                if (alpha >= 1.0f) {
                    iniciarSecuenciaFinal();
                    return;
                }
            } else {
                actualizarFadeBlanco(0f); 
            }
        }

        // ==========================================
        // LÓGICA DE CONDICIONES Y ACTUALIZACIÓN
        // ==========================================
        tiempoRestante -= tiempoPorFotograma;
        controladorUI.actualizarTiempo(tiempoRestante);

        controladorUI.actualizarGravedadHUD(
            controladorJugador.tieneDispositivoGravedad, 
            controladorJugador.gravedadInvertida
        );

        if (tiempoRestante <= 0) {
            manejarReinicioTotal("TIEMPO AGOTADO");
            return;
        }

        // Medición de proximidad espacial usando Álgebra Vectorial (distancia Euclidiana)
        if (pruebaActual < 9) {
            if (llavesRecolectadas >= LLAVES_NECESARIAS && !controladorNivel.isCompuertaAbriendose()) {
                Vector3f centroCompuerta = new Vector3f(0f, -20f, controladorNivel.obtenerDesplazamientoZ() - 25.7f);
                if (controladorJugador.obtenerUbicacion().distance(centroCompuerta) < 7.0f) {
                    controladorNivel.abrirCompuerta();
                    controladorAudio.reproducirPuerta();
                    controladorNivel.generarHabitacion(pruebaActual + 1);
                    controladorUI.mostrarMensajeAcceso("COMPUERTA ABIERTA - AVANZANDO...");
                }
            }
        } 
        else if (pruebaActual == 8 && !controladorNivel.isCompuertaAbriendose()) {
             controladorNivel.abrirCompuerta();
             controladorNivel.generarHabitacion(9);
        }
        
        // Delegación de actualización de Inteligencia Artificial al LevelController
        if (controladorNivel.procesarIAEnemigos(tiempoPorFotograma, controladorJugador)) {
            manejarReinicioTotal("ATRAPADO POR UNIDAD CENTINELA");
            return;
        }

        controladorNivel.actualizar(tiempoPorFotograma, pruebaActual, controladorJugador);
        controladorJugador.actualizar(tiempoPorFotograma);

        // Control de transición modular por zona (coordenada Z)
        float limiteZona = controladorNivel.obtenerDesplazamientoZ() - 25f;
        if (llavesRecolectadas >= LLAVES_NECESARIAS && controladorJugador.obtenerUbicacion().z < limiteZona - 3f) {
            pruebaActual++;
            controladorNivel.sellarHabitacion(pruebaActual, limiteZona);
            iniciarPrueba(pruebaActual);
        }
    }

    public void iniciarSimulacion() {
        simulacionIniciada = true;
        aplicacionPrincipal.getInputManager().setCursorVisible(false);
        controladorUI.ocultarMensajeInicio();
        controladorJugador.activarCamara();
        if (controladorAudio != null) controladorAudio.reproducirMusica();
        
        pausadoPorInstrucciones = true;
        estadoFisicas.setEnabled(false); 
        controladorUI.mostrarInstrucciones(obtenerInstruccionesNivel(1));
    }

    /**
     * Gestión de Memoria y Ciclo de Vida:
     * Tras una condición de derrota, se asegura el uso de bloqueos (candados lógicos)
     * para evitar ejecuciones concurrentes y se limpia el Grafo de Escena (Scene Graph)
     * mitigando fugas de memoria (Memory Leaks).
     */
    private void manejarReinicioTotal(String motivo) {
        if (procesandoMuerte) return; 
        procesandoMuerte = true; 
        controladorAudio.reproducirMuerte();

        // Encolado seguro en el hilo principal de renderizado (Render Thread)
        aplicacionPrincipal.enqueue(() -> {
            controladorJugador.resetearGravedadNormal();
            controladorNivel.limpiarNivel();
            iniciarPrueba(pruebaActual);
            
            float offsetAparicion = (pruebaActual == 1) ? 15f : 22f;
            Vector3f posicionSegura = new Vector3f(0, -20f, controladorNivel.obtenerDesplazamientoZ() + offsetAparicion);
            controladorJugador.reaparecer(posicionSegura);
            
            controladorUI.mostrarMensajeAcceso(motivo + " - SIMULACIÓN REINICIADA");
            procesandoMuerte = false; 
        });
    }

    public void registrarLlaveRecolectada(int indiceTarjeta) {
        controladorNivel.desactivarTarjeta(indiceTarjeta);
        llavesRecolectadas++;
        controladorAudio.reproducirLlave();
        controladorUI.actualizarDatos(llavesRecolectadas, LLAVES_NECESARIAS);
        
        if (llavesRecolectadas >= LLAVES_NECESARIAS) {
            controladorUI.mostrarMensajeAcercarse();
        }
    }
    
    public void obtenerModuloGravedad() {
        if (controladorNivel.dispositivoActivo) {
            aplicacionPrincipal.enqueue(() -> {
                controladorNivel.ocultarDispositivo();
                controladorJugador.recogerDispositivo();
                controladorUI.actualizarGravedadHUD(true, false);
                controladorUI.mostrarMensajeAcceso("MÓDULO DE GRAVEDAD DESBLOQUEADO - CLICK DERECHO PARA ACTIVAR/DESACTIVAR");
            });
        }
    }

    public void manejarImpactoLaser() {
        manejarReinicioTotal("IMPACTO LÁSER");
    }
    
    /**
     * Aplicación de un Filtro Ortogonal 2D para manipulación visual.
     * Mapea un Quad con transparencia dinámica al Nodo UI (Interfaz).
     */
    private void actualizarFadeBlanco(float alpha) {
        if (alpha <= 0f) {
            if (flashBlanco != null && flashBlanco.getParent() != null) {
                flashBlanco.removeFromParent(); 
            }
            return;
        }

        if (flashBlanco == null) {
            flashBlanco = new com.jme3.scene.Geometry("Flash", new com.jme3.scene.shape.Quad(aplicacionPrincipal.getCamera().getWidth(), aplicacionPrincipal.getCamera().getHeight()));
            matBlanco = new com.jme3.material.Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            matBlanco.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            flashBlanco.setMaterial(matBlanco);
        }

        matBlanco.setColor("Color", new com.jme3.math.ColorRGBA(1f, 1f, 1f, alpha));
        
        if (flashBlanco.getParent() == null) {
            aplicacionPrincipal.getGuiNode().attachChild(flashBlanco);
        }
    }
    
    private String obtenerInstruccionesNivel(int nivel) {
        switch (nivel) {
            case 1: 
                return "PRUEBA 01 - INICIACIÓN\n\nOBJETIVO: Recolecta las 3 llaves para pasar de fase.\nSISTEMA: Salto básico activado.\nCONSEJO: Encuentra primero el dispositivo gravitatorio";
            case 2: 
                return "PRUEBA 02 - PELIGRO\n\nALERTA: Red de láseres estáticos detectada.\nSISTEMA: Usa el clic derecho para invertir tu gravedad.";
            case 3: 
                return "PRUEBA 03 - EL LABERINTO\n\nOBJETIVO: Navega la geometría holográfica.\nCONSEJO: Piensa en 3D. El techo es tu nuevo suelo.";
            case 4: 
                return "PRUEBA 04 - LABERINTO LASER\n\nALERTA: Laseres estaticos en varias direcciones.\nCONSEJO: Calcula el momento exacto para alterar la gravedad.";
            case 5: 
                return "PRUEBA 05 - PLATAFORMAS CAMBIANTES\n\nALERTA: Hologramas de suelo inestables.\nSISTEMA: Cambia gravedad, cambia estabilidad";
            case 6: 
                return "PRUEBA 06 - PLATAFORMAS DESGASTE\n\nALERTA: Hologramas de suelo inestables.\nSISTEMA: No te detengas por más de 2 segundos.";
            case 7: 
                return "PRUEBA 07 - CENTINELAS\n\nALERTA: Drones de seguridad activos.\nSISTEMA: El contacto físico resultará en reinicio.";
            case 8: 
                return "PRUEBA 08 - EVASIÓN\n\nALERTA: Drones de seguridad y laseres.\nCONSEJO: Divide y venceras";
            case 9: 
                return "PRUEBA 09 - FINAL\n\nSISTEMA: Evaluación concluida con éxito.\nDirígete a la puerta para finalizar simulación.";
            default: 
                return "PRUEBA " + String.format("%02d", nivel) + "\n\nOBJETIVO: Sobrevive.\nSISTEMA: Sin asistencia.";
        }
    }

    private void iniciarSecuenciaFinal() {
        mostrandoFinal = true;
        tiempoFinal = 0f;
        
        textoFinal = new com.jme3.font.BitmapText(aplicacionPrincipal.getAssetManager().loadFont("Interface/Fonts/Default.fnt"), false);
        textoFinal.setSize(aplicacionPrincipal.getCamera().getHeight() * 0.08f);
        textoFinal.setColor(com.jme3.math.ColorRGBA.Black);
        textoFinal.setText("PROYECTO APEX COMPLETADO");
        
        textoFinal.setLocalTranslation(
            (aplicacionPrincipal.getCamera().getWidth() - textoFinal.getLineWidth()) / 2f,
            (aplicacionPrincipal.getCamera().getHeight() + textoFinal.getLineHeight()) / 2f,
            0
        );
        aplicacionPrincipal.getGuiNode().attachChild(textoFinal);
    }
    
    private void reiniciarAlMenuPrincipal() {
        if (flashBlanco != null) flashBlanco.removeFromParent();
        if (textoFinal != null) textoFinal.removeFromParent();
        
        controladorAudio.detenerMusica();
        
        controladorUI.mostrarMensajeInicio(); 
        mostrandoFinal = false;
        simulacionIniciada = false; 
        tiempoCinematica = 0f; 
        
        aplicacionPrincipal.getInputManager().setCursorVisible(true);
        controladorJugador.resetearGravedadNormal();
        
        controladorJugador.desactivarCamara(); 
        controladorJugador.setVisible(false);
        
        // Limpieza profunda del grafo de escena (Garbage Collection optimization)
        controladorNivel.limpiarNivel();
        
        controladorNivel.generarHabitacion(1);
        iniciarPrueba(1);
        
        Vector3f posicionAparicion = new Vector3f(0, -20f, controladorNivel.obtenerDesplazamientoZ() + 15f);
        controladorJugador.reaparecer(posicionAparicion);
    }
    
    public AudioController getControladorAudio() { return controladorAudio; }
    public boolean isSimulacionIniciada() { return simulacionIniciada; }

    @Override protected void cleanup(Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}