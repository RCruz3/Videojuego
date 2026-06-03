package com.mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.MatParam;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

/**
 * PlayerController - Entidad de Control Cinemático y Físico.
 * Maneja la interacción del usuario, la cámara seguidora en tercera persona, 
 * y las transformaciones matemáticas complejas para la inversión de gravedad.
 */
public class PlayerController implements ActionListener {

    private SimpleApplication aplicacionPrincipal;
    private Node nodoRaiz;
    private BulletAppState estadoFisicas;

    private Node nodoJugador;
    private Node nodoAjusteVisual;
    private BetterCharacterControl fisicaJugador;
    private Spatial modeloVisual;
    private AnimComposer controladorAnimacion;
    private ChaseCamera camaraSeguidora;

    public boolean tieneDispositivoGravedad = false;
    public boolean gravedadInvertida = false; 
    
    // Candado lógico de colisión
    public boolean interferenciaEstructural = false; 
    
    // Vectores booleanos de dirección
    private boolean moviendoAdelante = false;
    private boolean moviendoAtras = false;
    private boolean moviendoIzquierda = false;
    private boolean moviendoDerecha = false;
    
    public boolean sobrePlataforma = false;
    private float empujeExternoX = 0f; 

    private String animacionActual = "Parado";
    private float velocidadCaminar = 16f; 

    private final float TAMANIO_CAPSULA = 3.6f; 
    private final float RADIO_CAPSULA = 0.6f;

    private boolean haciendoTransicion = false;
    private float tiempoTransicion = 0f;
    private final float DURACION_TRANSICION = 0.7f; 

    public PlayerController(SimpleApplication app, BulletAppState estadoFisicas) {
        this.aplicacionPrincipal = app;
        this.nodoRaiz = app.getRootNode();
        this.estadoFisicas = estadoFisicas;
        
        inicializarJugador();
        configurarControles();
    }

    /**
     * Inicialización del Grafo de Escena (Scene Graph) y Físicas.
     * Carga el modelo 3D y le asigna una cápsula de colisión dinámica (BetterCharacterControl)
     * para calcular respuestas a colisiones e inercia mediante Bullet Physics.
     */
    private void inicializarJugador() {
        nodoJugador = new Node("NodoJugador");
        nodoAjusteVisual = new Node("NodoAjusteVisual");
        modeloVisual = aplicacionPrincipal.getAssetManager().loadModel("Models/personaje.j3o");
        TangentBinormalGenerator.generate(modeloVisual);
        arreglarMaterialesPBR(modeloVisual);
        nodoAjusteVisual.setLocalScale(2.0f); 
        nodoAjusteVisual.attachChild(modeloVisual);
        
        controladorAnimacion = encontrarControladorAnimacion(modeloVisual);
        
        if (controladorAnimacion != null) {
            try {
                controladorAnimacion.action("Saltar").setSpeed(1.7f);
                controladorAnimacion.setCurrentAction("Parado");
            } catch (Exception e) {}
        }

        nodoAjusteVisual.setCullHint(Spatial.CullHint.Always); 
        nodoJugador.setCullHint(Spatial.CullHint.Always);       

        // Creación del controlador de carácter físico (Cápsula matemática)
        fisicaJugador = new BetterCharacterControl(RADIO_CAPSULA, TAMANIO_CAPSULA, 80f);
        nodoJugador.addControl(fisicaJugador);
        nodoJugador.attachChild(nodoAjusteVisual);
        nodoRaiz.attachChild(nodoJugador);
        estadoFisicas.getPhysicsSpace().add(fisicaJugador);
        
        // Aplicación del vector de gravedad inicial
        fisicaJugador.setGravity(new Vector3f(0, -9.81f * 3, 0));
        fisicaJugador.setJumpForce(new Vector3f(0, 1200f, 0)); 

        // Configuración de la cámara con rotación orbital
        camaraSeguidora = new ChaseCamera(aplicacionPrincipal.getCamera(), nodoJugador, aplicacionPrincipal.getInputManager());
        camaraSeguidora.setSmoothMotion(true);
        camaraSeguidora.setLookAtOffset(new Vector3f(0, 2.8f, 0)); 
        camaraSeguidora.setDefaultDistance(2.5f); 
        camaraSeguidora.setDragToRotate(false); 
        camaraSeguidora.setRotationSpeed(5f); 
        camaraSeguidora.setChasingSensitivity(50f); 
        camaraSeguidora.setMinVerticalRotation(-FastMath.PI / 2.2f);
        camaraSeguidora.setMaxVerticalRotation(FastMath.PI / 2.2f);

        camaraSeguidora.setInvertVerticalAxis(true); 
        camaraSeguidora.setInvertHorizontalAxis(false);
        camaraSeguidora.setEnabled(false);
    }

    /**
     * Manipulación algorítmica de materiales en tiempo de ejecución.
     * Recorre los nodos descendientes (Depth First Traversal) para adaptar texturas 
     * PBR a sistemas de iluminación estándar.
     */
    private void arreglarMaterialesPBR(Spatial modelo) {
        modelo.depthFirstTraversal(espacial -> {
            if (espacial instanceof Geometry) {
                Geometry geometria = (Geometry) espacial;
                Material matActual = geometria.getMaterial();
                if (matActual != null && matActual.getMaterialDef().getName().contains("PBR")) {
                    Material matNuevo = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
                    MatParam texturaPBR = matActual.getParam("BaseColorMap");
                    if (texturaPBR != null) matNuevo.setTexture("DiffuseMap", (Texture) texturaPBR.getValue());
                    matNuevo.setBoolean("UseMaterialColors", true);
                    matNuevo.setColor("Ambient", new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f)); 
                    matNuevo.setColor("Diffuse", ColorRGBA.White); 
                    matNuevo.setFloat("Shininess", 16f);
                    geometria.setMaterial(matNuevo);
                }
            }
        });
    }

    private void configurarControles() {
        InputManager manejadorEntrada = aplicacionPrincipal.getInputManager();
        manejadorEntrada.addMapping("Adelante", new KeyTrigger(KeyInput.KEY_W));
        manejadorEntrada.addMapping("Atras", new KeyTrigger(KeyInput.KEY_S));
        manejadorEntrada.addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        manejadorEntrada.addMapping("Derecha", new KeyTrigger(KeyInput.KEY_D));
        manejadorEntrada.addMapping("Saltar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        manejadorEntrada.addMapping("Gravedad", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        manejadorEntrada.addListener(this, "Adelante", "Atras", "Izquierda", "Derecha", "Saltar", "Gravedad");
    }

    @Override
    public void onAction(String accion, boolean presionado, float tpf) {
        if (accion.equals("Adelante")) moviendoAdelante = presionado;
        if (accion.equals("Atras")) moviendoAtras = presionado;
        if (accion.equals("Izquierda")) moviendoIzquierda = presionado;
        if (accion.equals("Derecha")) moviendoDerecha = presionado;
        if (haciendoTransicion) return;
        
        if (accion.equals("Saltar") && presionado) {
            fisicaJugador.jump();
            if (controladorAnimacion != null) try { controladorAnimacion.setCurrentAction("Saltar"); animacionActual = "Saltar"; } catch (Exception e) {}
        }
        
        if (accion.equals("Gravedad") && presionado) {
            if (!tieneDispositivoGravedad) {
                System.out.println("[SISTEMA] Módulo de inversión gravitatoria no detectado.");
                return; 
            }
            if (interferenciaEstructural) {
                System.out.println("[SISTEMA ERROR] Interferencia estructural detectada. Gravedad bloqueada.");
                return; 
            }
            invertirGravedad();
            aplicacionPrincipal.getStateManager().getState(GameState.class).getControladorAudio().reproducirCambioGravedad();        
        }
    }

    /**
     * Bucle de actualización cinemática. 
     * Aplica transformaciones de traslación mediante Álgebra Vectorial y rotación 
     * utilizando Quaterniones y LERP (Interpolación Lineal) para transiciones fluidas.
     */
    public void actualizar(float tiempoPorFotograma) {
        if (!camaraSeguidora.isEnabled()) return;
        
        // Obtención de los vectores unitarios direccionales de la cámara
        Vector3f direccionCamara = aplicacionPrincipal.getCamera().getDirection().clone().setY(0).normalizeLocal();
        Vector3f izquierdaCamara = aplicacionPrincipal.getCamera().getLeft().clone().setY(0).normalizeLocal();

        if (haciendoTransicion) {
            // Aplicación de FastMath.interpolateLinear (LERP) para suavizado matemático
            tiempoTransicion += tiempoPorFotograma; 
            float progreso = FastMath.clamp(tiempoTransicion / DURACION_TRANSICION, 0.0f, 1.0f);
            float rotacionInicio = gravedadInvertida ? -FastMath.PI : FastMath.PI;
            float rotacionActual = FastMath.interpolateLinear(progreso, rotacionInicio, 0f);
            float alturaActual = FastMath.interpolateLinear(progreso, TAMANIO_CAPSULA, 0f);

            // Uso de Quaternion para evitar Gimbal Lock al alterar ejes locales
            Quaternion rotacionDinamica = new Quaternion().fromAngleAxis(rotacionActual, Vector3f.UNIT_Z);
            nodoAjusteVisual.setLocalRotation(rotacionDinamica);
            nodoAjusteVisual.setLocalTranslation(0, alturaActual, 0);
            
            Vector3f vectorArribaDestino = gravedadInvertida ? new Vector3f(0, -0.999f, 0.001f).normalizeLocal() : new Vector3f(0, 1f, 0);
            camaraSeguidora.setUpVector(rotacionDinamica.mult(vectorArribaDestino));
            
            if (progreso >= 1.0f) {
                haciendoTransicion = false;
                camaraSeguidora.setUpVector(vectorArribaDestino);
            }
        } else {
            camaraSeguidora.setUpVector(gravedadInvertida ? new Vector3f(0, -1, 0) : new Vector3f(0, 1, 0));

            Vector3f arribaMundo = gravedadInvertida ? new Vector3f(0, -1, 0) : new Vector3f(0, 1, 0);
            Quaternion rotacionDeseadaMundo = new Quaternion().lookAt(direccionCamara, arribaMundo);
            Quaternion compensacionLocal = nodoJugador.getLocalRotation().inverse().mult(rotacionDeseadaMundo);
            nodoAjusteVisual.setLocalRotation(compensacionLocal);
        }

        fisicaJugador.setViewDirection(direccionCamara);

        // Suma de vectores de dirección multiplicados por el escalar escalar de velocidad
        Vector3f direccionCaminata = new Vector3f(0, 0, 0);
        if (moviendoAdelante) direccionCaminata.addLocal(direccionCamara);
        if (moviendoAtras) direccionCaminata.addLocal(direccionCamara.negate());
        if (moviendoIzquierda) direccionCaminata.addLocal(izquierdaCamara);
        if (moviendoDerecha) direccionCaminata.addLocal(izquierdaCamara.negate());

        direccionCaminata.normalizeLocal().multLocal(velocidadCaminar);
        if (sobrePlataforma) direccionCaminata.x += empujeExternoX;
        fisicaJugador.setWalkDirection(direccionCaminata);
        empujeExternoX = 0f;

        gestionarAnimaciones();
    }

    /**
     * Mecánica Core: Alteración vectorial gravitacional.
     * Invierte la matriz de la cámara y aplica la fuerza de gravedad en el eje Y opuesto.
     */
    private void invertirGravedad() {
        gravedadInvertida = !gravedadInvertida;
        haciendoTransicion = true;
        tiempoTransicion = 0f;
        Vector3f pAM = nodoJugador.getLocalTranslation().clone();
        
        camaraSeguidora.setLookAtOffset(new Vector3f(0, gravedadInvertida ? -2.8f : 2.8f, 0));

        camaraSeguidora.setInvertVerticalAxis(!gravedadInvertida); 
        camaraSeguidora.setInvertHorizontalAxis(gravedadInvertida);

        if (gravedadInvertida) {
            fisicaJugador.setGravity(new Vector3f(0, 9.81f * 3, 0)); 
            fisicaJugador.setJumpForce(new Vector3f(0, 1200f, 0)); 
            fisicaJugador.warp(pAM.addLocal(0, TAMANIO_CAPSULA, 0));
        } else {
            fisicaJugador.setGravity(new Vector3f(0, -9.81f * 3, 0));
            fisicaJugador.setJumpForce(new Vector3f(0, 1200f, 0)); 
            fisicaJugador.warp(pAM.addLocal(0, -TAMANIO_CAPSULA, 0));
        }
    }
    
    public void activarCamara() {
        camaraSeguidora.setEnabled(true);
        nodoAjusteVisual.setCullHint(Spatial.CullHint.Inherit);
        nodoJugador.setCullHint(Spatial.CullHint.Inherit);
        if (controladorAnimacion != null) {
            controladorAnimacion.setCurrentAction("Parado");
            animacionActual = "Parado";
        }
        fisicaJugador.setWalkDirection(Vector3f.ZERO);
    }
    
    public void desactivarCamara() {
        camaraSeguidora.setEnabled(false);
    }
    
    public void forzarAnimacion(String animacion) {
        if (controladorAnimacion != null && !animacionActual.equals(animacion)) {
            try {
                controladorAnimacion.setCurrentAction(animacion);
                animacionActual = animacion;
            } catch (Exception e) {}
        }
    }

    public void recogerDispositivo() {
        this.tieneDispositivoGravedad = true;
    }

    public void resetearGravedadNormal() {
        gravedadInvertida = false;
        haciendoTransicion = false;
        tiempoTransicion = 0f;

        fisicaJugador.setGravity(new Vector3f(0, -9.81f * 3, 0));
        fisicaJugador.setJumpForce(new Vector3f(0, 1200f, 0));

        camaraSeguidora.setLookAtOffset(new Vector3f(0, 2.8f, 0));
        camaraSeguidora.setInvertVerticalAxis(true);
        camaraSeguidora.setInvertHorizontalAxis(false);
        camaraSeguidora.setUpVector(new Vector3f(0, 1, 0));

        nodoAjusteVisual.setLocalRotation(new Quaternion());
        nodoAjusteVisual.setLocalTranslation(Vector3f.ZERO);
        
        forzarAnimacion("Parado");
    }

    /**
     * Motor lógico de animaciones.
     * Evalúa la inercia (velocidad en Y) y el contacto superficial para determinar 
     * las acciones cinemáticas.
     */
    private void gestionarAnimaciones() { 
        if (controladorAnimacion == null) return; 
        
        float vV = fisicaJugador.getVelocity().y; 
        boolean tS = fisicaJugador.isOnGround(); 
        String nA = animacionActual; 
        float vVC = gravedadInvertida ? -vV : vV; 
        
        if (!tS) { 
            if (vVC > 0.5f) nA = "Saltar"; 
            else if (vVC < -0.5f) nA = "Caer"; 
        } else if (FastMath.abs(vVC) < 0.6f) { 
            if (moviendoAdelante) nA = "Correr"; 
            else if (moviendoAtras) nA = "CaminarAtras"; 
            else if (moviendoIzquierda) nA = "CorrerIzquierda"; 
            else if (moviendoDerecha) nA = "CorrerDerecha"; 
            else nA = "Parado"; 
        } 
        
        if (!nA.equals(animacionActual)) { 
            try { 
                controladorAnimacion.setCurrentAction(nA); 
                animacionActual = nA; 
            } catch (Exception e) {} 
        } 
    }
    
    public void recibirVelocidadPlataforma(float vX) { this.empujeExternoX = vX; }
    public Vector3f obtenerUbicacion() { return nodoJugador.getLocalTranslation(); }
    
    /**
     * Teletransportación a coordenadas relativas (Warp).
     * Modifica directamente la matriz del control dinámico saltando cálculos de inercia.
     */
    public void reaparecer(Vector3f pM) { 
        fisicaJugador.warp(pM); 
        fisicaJugador.setWalkDirection(Vector3f.ZERO); 

        camaraSeguidora.setSmoothMotion(false);
        aplicacionPrincipal.enqueue(() -> {
            camaraSeguidora.setSmoothMotion(true);
        });
    }
    
    public void setVisible(boolean visible) {
        if (visible) {
            nodoAjusteVisual.setCullHint(Spatial.CullHint.Inherit);
            nodoJugador.setCullHint(Spatial.CullHint.Inherit);
        } else {
            nodoAjusteVisual.setCullHint(Spatial.CullHint.Always);
            nodoJugador.setCullHint(Spatial.CullHint.Always);
        }
    }
    
    private AnimComposer encontrarControladorAnimacion(Spatial s) { 
        AnimComposer c = s.getControl(AnimComposer.class); 
        if (c != null) return c; 
        
        if (s instanceof Node) { 
            for (Spatial h : ((Node) s).getChildren()) { 
                AnimComposer e = encontrarControladorAnimacion(h); 
                if (e != null) return e; 
            } 
        } 
        return null; 
    }
}

