package com.mygame;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * LevelController - Entidad de Gestión Ambiental y Grafo de Escena.
 * Encargado de la instanciación de mallas 3D, texturizado procedural,
 * manejo de Inteligencia Artificial de enemigos e interacción de colisiones kinemáticas.
 */
public class LevelController {

    private Main aplicacionPrincipal;
    private BulletAppState estadoFisicas;
    private Node nodoEscenario;
    
    // --- COMPONENTES GEOMÉTRICOS Y FÍSICOS ---
    private Geometry modeloCompuertaIzquierda, modeloCompuertaDerecha;
    private RigidBodyControl fisicaCompuertaIzquierda, fisicaCompuertaDerecha;
    private boolean compuertaAbriendose = false;
    
    private Spatial modeloDispositivo;
    private RigidBodyControl fisicaDispositivo;
    public boolean dispositivoActivo = false;
    
    private final Spatial[] modelosTarjetas = new Spatial[3];
    private final RigidBodyControl[] fisicasTarjetas = new RigidBodyControl[3];
    public final boolean[] tarjetasActivas = new boolean[3];
    
    private final int CANTIDAD_PLATAFORMAS = 8;
    private final Spatial[] modelosPlataformas = new Spatial[CANTIDAD_PLATAFORMAS];
    private final RigidBodyControl[] fisicasPlataformas = new RigidBodyControl[CANTIDAD_PLATAFORMAS];
    private final float[] direccionPlataformas = new float[CANTIDAD_PLATAFORMAS];
    private final float[] velocidadPlataformas = new float[CANTIDAD_PLATAFORMAS]; 
    
    // --- VARIABLES PARA PLATAFORMAS FALSAS Y DESGASTE TÉRMICO ---
    public ArrayList<Spatial> plataformasFalsas = new ArrayList<>();
    public ArrayList<RigidBodyControl> fisicasFalsas = new ArrayList<>();
    public ArrayList<Boolean> esFalsaEnGravedadNormal = new ArrayList<>();
    public ArrayList<Float> desgastePlataformas = new ArrayList<>();
    public ArrayList<Boolean> esDesgastable = new ArrayList<>();
    
    // --- INTELIGENCIA ARTIFICIAL: DRONES ---
    public ArrayList<Spatial> modelosDrones = new ArrayList<>();
    public ArrayList<Vector3f> origenDrones = new ArrayList<>(); 
    
    private final int CANTIDAD_LASERS = 5;
    private final Spatial[] modelosLasers = new Spatial[CANTIDAD_LASERS];
    private final RigidBodyControl[] fisicasLasers = new RigidBodyControl[CANTIDAD_LASERS];

    private final float[] rangoZLasers = new float[CANTIDAD_LASERS];
    private final float[] velocidadLasers = new float[CANTIDAD_LASERS];
    private final float[] centroZLasers = new float[CANTIDAD_LASERS];
    private final float[] progresoLasers = new float[CANTIDAD_LASERS];

    private float desplazamientoZActual = 0f;
    public float tiempoDesgaste = 2.2f;
    
    // Variable estática para "Ocultar" geometrías eficientemente mediante traslación lejana
    private final Vector3f ZONA_LIMBO = new Vector3f(0, -1000f, 0);
    
    private final INivel[] modulosNiveles = new INivel[10];
    private boolean[] habitacionConstruida = new boolean[15];

    private ArrayList<Spatial> murosTemporales = new ArrayList<>();
    private ArrayList<RigidBodyControl> fisicasMurosTemporales = new ArrayList<>();

    public LevelController(Main aplicacionPrincipal, BulletAppState estadoFisicas) {
        this.aplicacionPrincipal = aplicacionPrincipal;
        this.estadoFisicas = estadoFisicas;
        this.nodoEscenario = new Node("NodoEscenario");
        aplicacionPrincipal.getRootNode().attachChild(nodoEscenario);
        
        // Iluminación global del entorno utilizando AmbientLight y ColorRGBA
        AmbientLight luzAmbiental = new AmbientLight();
        luzAmbiental.setColor(new ColorRGBA(0.20f, 0.20f, 0.20f, 1.0f)); 
        aplicacionPrincipal.getRootNode().addLight(luzAmbiental);
        
        inicializarObstaculos();
        inicializarDispositivo();
        
        // Patrón de diseño Strategy para el polimorfismo de niveles
        modulosNiveles[1] = new Nivel1();
        modulosNiveles[2] = new Nivel2();
        modulosNiveles[3] = new Nivel3();
        modulosNiveles[4] = new Nivel4();
        modulosNiveles[5] = new Nivel5();
        modulosNiveles[6] = new Nivel6(); 
        modulosNiveles[7] = new Nivel7();
        modulosNiveles[8] = new Nivel8();
        modulosNiveles[9] = new Nivel9();
    }

    /**
     * Instanciación y cacheo de geometrías reutilizables.
     * Mejora el rendimiento evitando instanciar y destruir objetos constantemente (Object Pooling).
     */
    private void inicializarObstaculos() {
        ColorRGBA colorAzul = new ColorRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        ColorRGBA colorRojo = new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f);

        for (int i = 0; i < 3; i++) {
            Spatial modeloTarjeta = aplicacionPrincipal.getAssetManager().loadModel("Models/key_card.j3o"); 
            modeloTarjeta.setName("TarjetaDatos_" + i);
            modeloTarjeta.scale(8.5f);
            arreglarMaterialesPBR(modeloTarjeta, aplicacionPrincipal.getAssetManager());
            
            // Asignación de control físico cinemático (sin reacción a gravedad)
            RigidBodyControl fisicas = new RigidBodyControl(0f);
            modeloTarjeta.addControl(fisicas);
            nodoEscenario.attachChild(modeloTarjeta);
            estadoFisicas.getPhysicsSpace().add(fisicas);

            modeloTarjeta.setLocalTranslation(ZONA_LIMBO);
            fisicas.setPhysicsLocation(ZONA_LIMBO);

            modelosTarjetas[i] = modeloTarjeta;
            fisicasTarjetas[i] = fisicas;
            tarjetasActivas[i] = false;
        }
        
        for (int i = 0; i < CANTIDAD_LASERS; i++) {
            modelosLasers[i] = crearElementoHolografico("RayoLaser", new Cylinder(16, 16, 0.15f, 100f, true), colorRojo);
            fisicasLasers[i] = modelosLasers[i].getControl(RigidBodyControl.class);
            fisicasLasers[i].setKinematic(true); 
            rangoZLasers[i] = 0f;
            velocidadLasers[i] = 0f;
            centroZLasers[i] = 0f;
            progresoLasers[i] = 0f;
        }

        for (int i = 0; i < CANTIDAD_PLATAFORMAS; i++) {
            modelosPlataformas[i] = crearElementoHolografico("PlataformaLuz", new Box(4f, 0.2f, 4f), colorAzul);
            fisicasPlataformas[i] = modelosPlataformas[i].getControl(RigidBodyControl.class);
            fisicasPlataformas[i].setKinematic(true);
            direccionPlataformas[i] = (i % 2 == 0) ? 1f : -1f;
            velocidadPlataformas[i] = 1.0f + (i * 0.15f); 
        }

        modeloCompuertaIzquierda = construirPanelMetal("CompuertaIzq", new Box(1.5f, 5f, 0.1f), ZONA_LIMBO);
        fisicaCompuertaIzquierda = modeloCompuertaIzquierda.getControl(RigidBodyControl.class);
        modeloCompuertaDerecha = construirPanelMetal("CompuertaDer", new Box(1.5f, 5f, 0.1f), ZONA_LIMBO);
        fisicaCompuertaDerecha = modeloCompuertaDerecha.getControl(RigidBodyControl.class);
    }
    
    private void inicializarDispositivo() {
        modeloDispositivo = aplicacionPrincipal.getAssetManager().loadModel("Models/dispositivo.j3o");
        modeloDispositivo.setName("DispositivoGravedad");
        modeloDispositivo.scale(0.4f);
        arreglarMaterialesPBR(modeloDispositivo, aplicacionPrincipal.getAssetManager());
        
        // Uso de BoxCollisionShape personalizado para hitboxes más precisas
        BoxCollisionShape formaCaja = new BoxCollisionShape(new Vector3f(1.5f, 1.5f, 1.5f));
        fisicaDispositivo = new RigidBodyControl(formaCaja, 0f);
        fisicaDispositivo.setKinematic(true); 
        modeloDispositivo.addControl(fisicaDispositivo);
        
        nodoEscenario.attachChild(modeloDispositivo);
        estadoFisicas.getPhysicsSpace().add(fisicaDispositivo);
        
        modeloDispositivo.setLocalTranslation(ZONA_LIMBO);
        fisicaDispositivo.setPhysicsLocation(ZONA_LIMBO);
    }

    public void iniciarPruebaEscenario(int numeroPrueba) {
        desplazamientoZActual = (numeroPrueba - 1) * -50f; 
        compuertaAbriendose = false;
        
        // Traslación vectorial matricial de las compuertas a su origen dinámico
        Vector3f posicionIzquierda = new Vector3f(-1.5f, -20f, desplazamientoZActual - 25.7f);
        modeloCompuertaIzquierda.setLocalTranslation(posicionIzquierda); 
        fisicaCompuertaIzquierda.setPhysicsLocation(posicionIzquierda);
        
        Vector3f posicionDerecha = new Vector3f(1.5f, -20f, desplazamientoZActual - 25.7f);
        modeloCompuertaDerecha.setLocalTranslation(posicionDerecha); 
        fisicaCompuertaDerecha.setPhysicsLocation(posicionDerecha);

        if (numeroPrueba < modulosNiveles.length && modulosNiveles[numeroPrueba] != null) {
            modulosNiveles[numeroPrueba].construirNivel(this, desplazamientoZActual);
        } else {
            modulosNiveles[2].construirNivel(this, desplazamientoZActual);
        }
    }

    /**
     * Gestión de Memoria (Garbage Collection y Scene Graph cleanup).
     * Remueve meticulosamente los controles del PhysicsSpace antes de desvincular
     * los nodos espaciales para evitar memory leaks graves al reiniciar el nivel.
     */
    public void limpiarNivel() {
        for (int i = 0; i < 3; i++) {
            tarjetasActivas[i] = false;
            modelosTarjetas[i].setLocalTranslation(ZONA_LIMBO);
            fisicasTarjetas[i].setPhysicsLocation(ZONA_LIMBO);
        }
        
        for (int i = 0; i < CANTIDAD_LASERS; i++) {
            modelosLasers[i].setLocalTranslation(ZONA_LIMBO); 
            fisicasLasers[i].setPhysicsLocation(ZONA_LIMBO);
            rangoZLasers[i] = 0f;
            velocidadLasers[i] = 0f;
            centroZLasers[i] = 0f;
            progresoLasers[i] = 0f;
        }
        
        for (int i = 0; i < CANTIDAD_PLATAFORMAS; i++) {
            modelosPlataformas[i].setLocalTranslation(ZONA_LIMBO); 
            fisicasPlataformas[i].setPhysicsLocation(ZONA_LIMBO);
        }
        
        // LIMPIEZA DE PLATAFORMAS FALSAS
        for (int i = 0; i < plataformasFalsas.size(); i++) {
            RigidBodyControl fisica = fisicasFalsas.get(i);
            if (fisica.getPhysicsSpace() != null) {
                estadoFisicas.getPhysicsSpace().remove(fisica); 
            }
            plataformasFalsas.get(i).removeFromParent(); 
        }
        plataformasFalsas.clear();
        fisicasFalsas.clear();
        esFalsaEnGravedadNormal.clear();
        desgastePlataformas.clear();
        esDesgastable.clear();
        
        // LIMPIEZA DE MUROS TEMPORALES
        for (int i = 0; i < murosTemporales.size(); i++) {
            RigidBodyControl fisica = fisicasMurosTemporales.get(i);
            if (fisica.getPhysicsSpace() != null) {
                estadoFisicas.getPhysicsSpace().remove(fisica);
            }
            murosTemporales.get(i).removeFromParent();
        }
        murosTemporales.clear();
        fisicasMurosTemporales.clear();
        
        // LIMPIEZA DE DRONES
        for (Spatial dron : modelosDrones) {
            dron.removeFromParent();
        }
        modelosDrones.clear();
        origenDrones.clear();

        ocultarDispositivo();
    }

    // --- MÉTODOS DE CONFIGURACIÓN ESPACIAL MATEMÁTICA ---
    public void configurarTarjeta(int indice, Vector3f posicionMundo) {
        if (indice >= 3) return;
        modelosTarjetas[indice].setLocalTranslation(posicionMundo);
        fisicasTarjetas[indice].setPhysicsLocation(posicionMundo);
        tarjetasActivas[indice] = true;
    }

    public void configurarLaser(int indice, Vector3f posicionMundo, Quaternion rotacionMundo, float rangoZ, float velocidad) {
        if (indice >= CANTIDAD_LASERS) return;
        modelosLasers[indice].setLocalTranslation(posicionMundo);
        fisicasLasers[indice].setPhysicsLocation(posicionMundo);
        modelosLasers[indice].setLocalRotation(rotacionMundo); // Aplicación de Cuaterniones
        fisicasLasers[indice].setPhysicsRotation(rotacionMundo);
        
        this.centroZLasers[indice] = posicionMundo.z;
        this.rangoZLasers[indice] = rangoZ;
        this.velocidadLasers[indice] = velocidad;
        this.progresoLasers[indice] = 0f;
    }

    public void configurarLaser(int indice, Vector3f posicionMundo, Quaternion rotacionMundo) {
        configurarLaser(indice, posicionMundo, rotacionMundo, 0f, 0f);
    }
    
    public void configurarPlataforma(int indice, Vector3f posicionMundo) {
        if (indice >= CANTIDAD_PLATAFORMAS) return;
        modelosPlataformas[indice].setLocalTranslation(posicionMundo);
        fisicasPlataformas[indice].setPhysicsLocation(posicionMundo);
    }

    /**
     * Detección de colisiones manual (AABB - Axis-Aligned Bounding Box).
     * Calcula la intersección volumétrica entre el jugador y las plataformas holográficas
     * mediante la evaluación de extenciones (Extents) matemáticas sin sobrecargar Bullet Physics.
     */
    public void procesarDesgasteSuelos(float tpf, PlayerController jugador) {
        Vector3f posJugador = jugador.obtenerUbicacion();
        
        for (int i = 0; i < plataformasFalsas.size(); i++) {
            if (esDesgastable.get(i)) {
                boolean falsaEnNormal = esFalsaEnGravedadNormal.get(i);
                boolean esSolidaActualmente = (falsaEnNormal == jugador.gravedadInvertida);

                if (esSolidaActualmente) {
                    Spatial plataforma = plataformasFalsas.get(i);
                    Geometry geom = (Geometry) ((Node)plataforma).getChild(0);
                    com.jme3.scene.shape.Box forma = (com.jme3.scene.shape.Box) geom.getMesh();
                    Vector3f posPlataforma = plataforma.getLocalTranslation();

                    // Matemáticas de AABB para validar el contacto superficial
                    boolean pisando = (
                        FastMath.abs(posPlataforma.x - posJugador.x) < (forma.getXExtent() + 0.8f) &&
                        FastMath.abs(posPlataforma.y - posJugador.y) < (forma.getYExtent() + 3.0f) && 
                        FastMath.abs(posPlataforma.z - posJugador.z) < (forma.getZExtent() + 0.8f)
                    );

                    if (pisando) {
                        float desgasteActual = desgastePlataformas.get(i) + tpf;
                        desgastePlataformas.set(i, desgasteActual);

                        if (desgasteActual > tiempoDesgaste) { 
                            esFalsaEnGravedadNormal.set(i, !falsaEnNormal); 
                            esDesgastable.set(i, false); 
                            desgastePlataformas.set(i, 0f);
                        }
                    } else {
                        float enfriamiento = Math.max(0f, desgastePlataformas.get(i) - (tpf * 0.5f));
                        desgastePlataformas.set(i, enfriamiento);
                    }
                }
            }
        }
    }

    /**
     * Bucle de actualización ambiental cinemático.
     * Gestiona las transformaciones rotacionales y translacionales de los elementos dinámicos
     * utilizando trigonometría para los movimientos armónicos y cíclicos.
     */
    public void actualizar(float tiempoPorFotograma, int numeroPrueba, PlayerController jugador) {
        // Cinemática de Compuertas
        if (compuertaAbriendose) {
            if (modeloCompuertaIzquierda.getLocalTranslation().x > -5.0f) {
                float pasoApertura = 10f * tiempoPorFotograma; 
                Vector3f nuevaPosIzq = modeloCompuertaIzquierda.getLocalTranslation().add(-pasoApertura, 0, 0);
                Vector3f nuevaPosDer = modeloCompuertaDerecha.getLocalTranslation().add(pasoApertura, 0, 0);
                modeloCompuertaIzquierda.setLocalTranslation(nuevaPosIzq); 
                modeloCompuertaDerecha.setLocalTranslation(nuevaPosDer);
                fisicaCompuertaIzquierda.setPhysicsLocation(nuevaPosIzq); 
                fisicaCompuertaDerecha.setPhysicsLocation(nuevaPosDer);
            }
        }

        // Rotación continua (Euler Angles) sobre el eje Y
        for (int i = 0; i < 3; i++) {
            if (tarjetasActivas[i]) modelosTarjetas[i].rotate(0, tiempoPorFotograma * 2, 0);
        }

        boolean jugadorEnPlataforma = false; 

        // Cinemática de plataformas móviles (rebote horizontal)
        for (int i = 0; i < CANTIDAD_PLATAFORMAS; i++) {
            if (fisicasPlataformas[i].getPhysicsLocation().y > -100) { 
                Vector3f posicionActual = modelosPlataformas[i].getLocalTranslation();
                if (posicionActual.x > 18) direccionPlataformas[i] = -1f; 
                else if (posicionActual.x < -18) direccionPlataformas[i] = 1f;
                
                float velocidadActualPlataforma = direccionPlataformas[i] * (5f + numeroPrueba * 1.5f) * velocidadPlataformas[i];
                float distanciaMovimiento = velocidadActualPlataforma * tiempoPorFotograma;
                
                modelosPlataformas[i].setLocalTranslation(posicionActual.x + distanciaMovimiento, posicionActual.y, posicionActual.z);
                fisicasPlataformas[i].setPhysicsLocation(modelosPlataformas[i].getWorldTranslation());
                
                if (jugador.sobrePlataforma && jugador.obtenerUbicacion().distance(modelosPlataformas[i].getWorldTranslation()) <= 6.0f) {
                    jugador.recibirVelocidadPlataforma(velocidadActualPlataforma);
                    jugadorEnPlataforma = true; 
                }
            }
        }

        if (!jugadorEnPlataforma) jugador.sobrePlataforma = false;

        // Uso de Funciones Senoidales (Trigonometría) para oscilación de lásers
        if (numeroPrueba == 3) {
            for (int i = 0; i < CANTIDAD_LASERS; i++) {
                if (fisicasLasers[i].getPhysicsLocation().y > -100 && velocidadLasers[i] == 0f) { 
                    Vector3f posActual = modelosLasers[i].getLocalTranslation();
                    Quaternion rotActual = modelosLasers[i].getLocalRotation();
                    float direccionAlternada = (i % 2 == 0) ? 1f : -1f;
                    float velocidadRitmica = (1.5f + i * 0.2f);
                    float factorMovimiento = FastMath.sin(aplicacionPrincipal.getTimer().getTimeInSeconds() * velocidadRitmica) * 16f * direccionAlternada;
                    Vector3f vectorDireccionLocal = rotActual.mult(Vector3f.UNIT_X); 
                    Vector3f nuevaPosicion = new Vector3f(0f, posActual.y, posActual.z).addLocal(vectorDireccionLocal.mult(factorMovimiento));
                    modelosLasers[i].setLocalTranslation(nuevaPosicion);
                    fisicasLasers[i].setPhysicsLocation(modelosLasers[i].getWorldTranslation());
                }
            }
        }
        
        for (int i = 0; i < CANTIDAD_LASERS; i++) {
            if (velocidadLasers[i] > 0f && fisicasLasers[i].getPhysicsLocation().y > -100) {
                progresoLasers[i] += tiempoPorFotograma * velocidadLasers[i];
                float nuevoZ = centroZLasers[i] + (FastMath.sin(progresoLasers[i]) * rangoZLasers[i]);
                Vector3f nuevaPosicion = modelosLasers[i].getLocalTranslation().clone();
                nuevaPosicion.z = nuevoZ;
                modelosLasers[i].setLocalTranslation(nuevaPosicion);
                fisicasLasers[i].setPhysicsLocation(nuevaPosicion);
            }
        }
        
        if (dispositivoActivo) {
            modeloDispositivo.rotate(0, tiempoPorFotograma * 3, 0);
            fisicaDispositivo.setPhysicsRotation(modeloDispositivo.getLocalRotation());
        }
        
        procesarDesgasteSuelos(tiempoPorFotograma, jugador); 
        actualizarPlataformasFalsas(jugador.gravedadInvertida);
        verificarInterferenciaDispositivo(jugador);
    }

    public void desactivarTarjeta(int indice) {
        tarjetasActivas[indice] = false; 
        aplicacionPrincipal.enqueue(() -> { 
            modelosTarjetas[indice].setLocalTranslation(ZONA_LIMBO); 
            fisicasTarjetas[indice].setPhysicsLocation(ZONA_LIMBO); 
        });
    }

    /**
     * Construcción procedural del nivel (Level Generation).
     * Mapea Texturas UV, aplica Materiales, e inyecta texturas 2D dinámicas.
     */
    public void generarHabitacion(int numeroPrueba) {
        if (numeroPrueba < habitacionConstruida.length && habitacionConstruida[numeroPrueba]) {
            return; 
        }
        if (numeroPrueba < habitacionConstruida.length) {
            habitacionConstruida[numeroPrueba] = true; 
        }

        float desplazamientoZ = (numeroPrueba - 1) * -50f; 
        if (numeroPrueba == 9) {
            generarPasilloOscuro(desplazamientoZ);
            return; 
        }
        float alturaEstructura = 25f, anchoEstructura = 25f, profundidadEstructura = 25f; 
        
        // Uso de rutas relativas a la carpeta /assets/ (Texturizado de Videojuego)
        String texturaPared = "Textures/hex_dark.jpg";
        String texturaPiso = "Textures/floor_geometry.jpg"; 
        String texturaTecho = "Textures/ceil.jpg";
        
        construirPared(0, -alturaEstructura, desplazamientoZ, anchoEstructura, 0.5f, profundidadEstructura, texturaPiso, "Suelo_" + numeroPrueba);
        construirPared(0, alturaEstructura, desplazamientoZ, anchoEstructura, 0.5f, profundidadEstructura, texturaTecho, "Techo_" + numeroPrueba);
        
        construirPared(anchoEstructura, 0, desplazamientoZ, 0.5f, alturaEstructura, profundidadEstructura, texturaPared, "ParedDerecha_" + numeroPrueba);
        construirPared(-anchoEstructura, 0, desplazamientoZ, 0.5f, alturaEstructura, profundidadEstructura, texturaPared, "ParedIzquierda_" + numeroPrueba);
        if (numeroPrueba == 1) construirPared(0, 0, desplazamientoZ + profundidadEstructura, anchoEstructura, alturaEstructura, 0.5f, texturaPared, "ParedInicio");

        float zonaParedFondo = desplazamientoZ - profundidadEstructura; 
        construirPared(-14f, 0, zonaParedFondo, 11f, alturaEstructura, 0.5f, texturaPared, "ParedFondoIzq_" + numeroPrueba);
        construirPared(14f, 0, zonaParedFondo, 11f, alturaEstructura, 0.5f, texturaPared, "ParedFondoDer_" + numeroPrueba);
        construirPared(0, 5f, zonaParedFondo, 3f, 20f, 0.5f, texturaPared, "ParedFondoArriba_" + numeroPrueba); 

        construirPanelMetal("MarcoIzq_" + numeroPrueba, new Box(0.1f, 5f, 0.55f), new Vector3f(-3.1f, -20f, zonaParedFondo));
        construirPanelMetal("MarcoDer_" + numeroPrueba, new Box(0.1f, 5f, 0.55f), new Vector3f(3.1f, -20f, zonaParedFondo));
        construirPanelMetal("MarcoArriba_" + numeroPrueba, new Box(3.2f, 0.1f, 0.55f), new Vector3f(0f, -14.9f, zonaParedFondo));

        Geometry pantallaHolografica = new Geometry("Pantalla_" + numeroPrueba, new Box(14f, 6f, 0.15f));
        Material materialPantalla = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        materialPantalla.setColor("Color", ColorRGBA.Black); pantallaHolografica.setMaterial(materialPantalla);
        pantallaHolografica.setLocalTranslation(0f, -2f, zonaParedFondo + 0.55f); nodoEscenario.attachChild(pantallaHolografica);

        // =====================================================================
        // GENERACIÓN PROCEDURAL DE TEXTURA 2D EN MEMORIA Y PROYECCIÓN 3D
        // =====================================================================
        
        int anchoTextoPx = 1024;
        int altoTextoPx = 256;
        BufferedImage imagenTexto = new BufferedImage(anchoTextoPx, altoTextoPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagenTexto.createGraphics();

        // RenderingHints para Anti-Aliasing (Rasterización HD de textos)
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new java.awt.Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, anchoTextoPx, altoTextoPx);

        g2d.setFont(new Font("Consolas", Font.BOLD, 250));
        g2d.setColor(Color.WHITE); 

        String textoAMostrar = "FASE " + String.format("%02d", numeroPrueba);
        
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int posXTexto = (anchoTextoPx - fm.stringWidth(textoAMostrar)) / 2;
        int posYTexto = ((altoTextoPx - fm.getHeight()) / 2) + fm.getAscent();
        
        g2d.drawString(textoAMostrar, posXTexto, posYTexto);
        g2d.dispose();

        // Conversión y puente hacia jMonkeyEngine
        AWTLoader cargadorTextura = new com.jme3.texture.plugins.AWTLoader();
        Image imagenJME = cargadorTextura.load(imagenTexto, true);
        Texture2D texturaFinalTexto = new Texture2D(imagenJME);
        
        // MagFilter.Bilinear asegura que al estirar la textura no se vea pixelada
        texturaFinalTexto.setMagFilter(Texture.MagFilter.Bilinear);
        texturaFinalTexto.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

        float anchoMallaQuad = 16f;
        float altoMallaQuad = 4f;
        Quad mallaTexto = new Quad(anchoMallaQuad, altoMallaQuad);
        Geometry geomTextoHolograma = new Geometry("TextoHolograma_" + numeroPrueba, mallaTexto);
        
        Material matTextoHolograma = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matTextoHolograma.setTexture("ColorMap", texturaFinalTexto);
        matTextoHolograma.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geomTextoHolograma.setMaterial(matTextoHolograma);

        float posicionXQuad = -(anchoMallaQuad / 2f);
        float posicionYQuad = -2f - (altoMallaQuad / 2f); 
        float posicionZQuad = zonaParedFondo + 0.75f; 
        
        geomTextoHolograma.setLocalTranslation(posicionXQuad, posicionYQuad, posicionZQuad);
        nodoEscenario.attachChild(geomTextoHolograma);

        ColorRGBA colorNeon = (numeroPrueba % 3 == 1) ? new ColorRGBA(0,0,1,1) : (numeroPrueba % 3 == 2) ? new ColorRGBA(0,1,0.2f,1) : new ColorRGBA(0.8f,0,1,1);
        SpotLight focoIzquierdo = new SpotLight(); focoIzquierdo.setSpotRange(120f); focoIzquierdo.setSpotOuterAngle(55f * FastMath.DEG_TO_RAD); focoIzquierdo.setColor(colorNeon.mult(2f)); focoIzquierdo.setPosition(new Vector3f(-15f, alturaEstructura-0.5f, desplazamientoZ)); focoIzquierdo.setDirection(new Vector3f(0,-1,0)); aplicacionPrincipal.getRootNode().addLight(focoIzquierdo);
        SpotLight focoDerecho = new SpotLight(); focoDerecho.setSpotRange(120f); focoDerecho.setSpotOuterAngle(55f * FastMath.DEG_TO_RAD); focoDerecho.setColor(colorNeon.mult(2f)); focoDerecho.setPosition(new Vector3f(15f, alturaEstructura-0.5f, desplazamientoZ)); focoDerecho.setDirection(new Vector3f(0,-1,0)); aplicacionPrincipal.getRootNode().addLight(focoDerecho);
    }

    public void sellarHabitacion(int numeroPrueba, float posicionZ) { 
        construirPanelMetal("Sello_" + numeroPrueba, new Box(3f, 5f, 0.5f), new Vector3f(0, -20f, posicionZ)); 
    }
    
    public void abrirCompuerta() { compuertaAbriendose = true; }
    public boolean isCompuertaAbriendose() { return compuertaAbriendose; }
    public float obtenerDesplazamientoZ() { return desplazamientoZActual; }

    private Geometry construirPanelMetal(String nombre, Box forma, Vector3f posicionMundo) {
        Geometry geometria = new Geometry(nombre, forma); Material materialMetal = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        materialMetal.setBoolean("UseMaterialColors", true); materialMetal.setColor("Diffuse", new ColorRGBA(0f, 0f, 0f, 1.0f));
        materialMetal.setColor("Ambient", new ColorRGBA(0f, 0f, 0f, 1.0f)); materialMetal.setColor("Specular", ColorRGBA.White); materialMetal.setFloat("Shininess", 128f); 
        geometria.setMaterial(materialMetal); RigidBodyControl fisicas = new RigidBodyControl(0.0f); geometria.addControl(fisicas); fisicas.setKinematic(true); 
        geometria.setLocalTranslation(posicionMundo); fisicas.setPhysicsLocation(posicionMundo); nodoEscenario.attachChild(geometria); estadoFisicas.getPhysicsSpace().add(fisicas);
        return geometria;
    }

    /**
     * Mapeo de Coordenadas UV.
     * Escala matemáticamente las texturas (Wrapping) en base al área de la geometría (Box).
     */
    private void construirPared(float posX, float posY, float posZ, float escalaX, float escalaY, float escalaZ, String rutaTextura, String nombre) {
        Box formaCaja = new Box(escalaX, escalaY, escalaZ); 
        formaCaja.scaleTextureCoordinates(new Vector2f(((escalaX > 0.6f) ? escalaX : escalaZ) / 25f, ((escalaY > 0.6f) ? escalaY : escalaZ) / 25f)); 
        Geometry geometria = new Geometry(nombre, formaCaja); 
        Material materialPared = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        Texture textura = aplicacionPrincipal.getAssetManager().loadTexture(rutaTextura); 
        textura.setWrap(Texture.WrapMode.Repeat);
        materialPared.setTexture("DiffuseMap", textura); 
        materialPared.setColor("Diffuse", ColorRGBA.Gray); 
        materialPared.setColor("Specular", ColorRGBA.White); 
        materialPared.setFloat("Shininess", 64f); 
        geometria.setMaterial(materialPared); 
        geometria.setLocalTranslation(posX, posY, posZ);
        RigidBodyControl fisicas = new RigidBodyControl(0.0f); 
        geometria.addControl(fisicas); 
        fisicas.setFriction(0.0f); 
        nodoEscenario.attachChild(geometria); 
        estadoFisicas.getPhysicsSpace().add(fisicas);
    }
    
    private Spatial crearElementoHolografico(String nombre, com.jme3.scene.Mesh mallaEstructura, ColorRGBA colorResplandor) {
        Node nodoEstructura = new Node(nombre);
        Geometry carasInteriores = new Geometry(nombre + "_Caras", mallaEstructura); 
        Material materialTransparente = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"); 
        ColorRGBA colorTransparencia = colorResplandor.clone(); 
        colorTransparencia.a = 0.50f; 
        materialTransparente.setColor("Color", colorTransparencia); 
        materialTransparente.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); 
        carasInteriores.setMaterial(materialTransparente); 
        nodoEstructura.attachChild(carasInteriores);
        RigidBodyControl fisicas = new RigidBodyControl(0f); 
        nodoEstructura.addControl(fisicas); 
        nodoEscenario.attachChild(nodoEstructura); 
        estadoFisicas.getPhysicsSpace().add(fisicas);
        nodoEstructura.setLocalTranslation(ZONA_LIMBO); 
        fisicas.setPhysicsLocation(ZONA_LIMBO); 
        return nodoEstructura;
    }
    
    public void construirMuroHolografico(Vector3f posicion, Vector3f escala, ColorRGBA colorResplandor) {
        Box forma = new Box(escala.x, escala.y, escala.z);
        Node nodoMuro = new Node("MuroHolografico");

        Geometry caras = new Geometry("Muro_Caras", forma);
        Material matTransparente = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA colorTransparente = colorResplandor.clone();
        colorTransparente.a = 0.40f; 
        matTransparente.setColor("Color", colorTransparente);
        matTransparente.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        caras.setMaterial(matTransparente);
        nodoMuro.attachChild(caras);

        RigidBodyControl fisicas = new RigidBodyControl(0f); 
        nodoMuro.addControl(fisicas);

        nodoMuro.setLocalTranslation(posicion);
        fisicas.setPhysicsLocation(posicion);

        nodoEscenario.attachChild(nodoMuro);
        estadoFisicas.getPhysicsSpace().add(fisicas);

        murosTemporales.add(nodoMuro);
        fisicasMurosTemporales.add(fisicas);
    }

    public void construirSueloHolografico(Vector3f posicion, Vector3f extension, boolean falsaEnGravedadNormal) {
        construirSueloHolografico(posicion, extension, falsaEnGravedadNormal, false);
    }

    public void construirSueloHolografico(Vector3f posicion, Vector3f extension, boolean falsaEnGravedadNormal, boolean desgastable) {
        Box forma = new Box(extension.x, extension.y, extension.z); 
        Geometry geometria = new Geometry("SueloHolografico", forma);
        Material mat = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        
        ColorRGBA colorBase = falsaEnGravedadNormal ? new ColorRGBA(1f, 0.4f, 0f, 1f) : new ColorRGBA(0f, 0.6f, 1f, 1f);
        mat.setColor("Color", colorBase);
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        geometria.setMaterial(mat);
        
        Node nodoSuelo = new Node("NodoSueloHolografico");
        nodoSuelo.attachChild(geometria);
        
        RigidBodyControl fisicas = new RigidBodyControl(0f); 
        nodoSuelo.addControl(fisicas);
        
        nodoSuelo.setLocalTranslation(posicion);
        fisicas.setPhysicsLocation(posicion);
        
        nodoEscenario.attachChild(nodoSuelo);
        
        plataformasFalsas.add(nodoSuelo);
        fisicasFalsas.add(fisicas);
        esFalsaEnGravedadNormal.add(falsaEnGravedadNormal);
        desgastePlataformas.add(0f);
        esDesgastable.add(desgastable);
    }
    
    public void actualizarPlataformasFalsas(boolean jugadorGravedadInvertida) {
        for (int i = 0; i < plataformasFalsas.size(); i++) {
            Spatial plataforma = plataformasFalsas.get(i);
            RigidBodyControl fisica = fisicasFalsas.get(i);
            boolean falsaEnNormal = esFalsaEnGravedadNormal.get(i);
            float desgaste = desgastePlataformas.get(i); 
            
            Geometry geom = (Geometry) ((Node)plataforma).getChild(0);
            Material mat = geom.getMaterial();
            
            boolean debeSerSolida = (falsaEnNormal == jugadorGravedadInvertida);
            
            if (debeSerSolida) {
                if (desgaste > 0f) {
                    float temperatura = Math.min(desgaste / tiempoDesgaste, 1.0f);
                    mat.setColor("Color", new ColorRGBA(1f, 0.6f - (temperatura * 0.4f), 0f, 0.35f + (temperatura * 0.5f)));
                } else {
                    mat.setColor("Color", new ColorRGBA(0f, 0.6f, 1f, 0.35f));
                }
                if (fisica.getPhysicsSpace() == null) estadoFisicas.getPhysicsSpace().add(fisica);
            } else {
                mat.setColor("Color", new ColorRGBA(1f, 0.05f, 0.05f, 1.0f));
                if (fisica.getPhysicsSpace() != null) estadoFisicas.getPhysicsSpace().remove(fisica);
            }
        }
    }

    public void verificarInterferenciaDispositivo(PlayerController jugador) {
        boolean estaAtravesando = false;
        Vector3f posJugador = jugador.obtenerUbicacion();
        for (int i = 0; i < plataformasFalsas.size(); i++) {
            boolean falsaEnNormal = esFalsaEnGravedadNormal.get(i);
            boolean esSolidaActualmente = (falsaEnNormal == jugador.gravedadInvertida);
            if (!esSolidaActualmente) {
                Spatial plataforma = plataformasFalsas.get(i);
                Geometry geom = (Geometry) ((Node)plataforma).getChild(0);
                com.jme3.scene.shape.Box forma = (com.jme3.scene.shape.Box) geom.getMesh();
                Vector3f posPlataforma = plataforma.getLocalTranslation();
                boolean tocandoHolograma = (
                    FastMath.abs(posPlataforma.x - posJugador.x) < (forma.getXExtent() + 0.5f) &&
                    FastMath.abs(posPlataforma.y - posJugador.y) < (forma.getYExtent() + 6.0f) && 
                    FastMath.abs(posPlataforma.z - posJugador.z) < (forma.getZExtent() + 0.5f)
                );
                if (tocandoHolograma) {
                    estaAtravesando = true;
                    break; 
                }
            }
        }
        jugador.interferenciaEstructural = estaAtravesando;
    }
    
    public void configurarDispositivo(Vector3f posicionMundo) {
        modeloDispositivo.setLocalTranslation(posicionMundo);
        fisicaDispositivo.setPhysicsLocation(posicionMundo);
        dispositivoActivo = true;
    }

    public void ocultarDispositivo() {
        dispositivoActivo = false;
        modeloDispositivo.setLocalTranslation(ZONA_LIMBO); 
        fisicaDispositivo.setPhysicsLocation(ZONA_LIMBO);
    }
    
    public void configurarDron(Vector3f posicionInicial) {
        Spatial modeloDron;
        try {
            modeloDron = aplicacionPrincipal.getAssetManager().loadModel("Models/dron.j3o");
        } catch (Exception e) {
            modeloDron = new Geometry("DronTemp", new Box(0.8f, 0.8f, 0.8f));
            Material matRojo = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            matRojo.setColor("Color", ColorRGBA.Red);
            modeloDron.setMaterial(matRojo);
        }
        
        modeloDron.scale(1.0f); 
        
        nodoEscenario.attachChild(modeloDron);
        modeloDron.setLocalTranslation(posicionInicial);

        modelosDrones.add(modeloDron);
        origenDrones.add(posicionInicial.clone());
    }

    /**
     * Máquina de Estados Finita (FSM) de Enemigos (Inteligencia Artificial).
     * Transiciona entre "Patrullaje" y "Ataque" en base a la medición de distancias euclidianas.
     * Calcula vectores de dirección normalizados para interceptar al jugador.
     */
    public boolean procesarIAEnemigos(float tpf, PlayerController jugador) {
        Vector3f posJugador = jugador.obtenerUbicacion();
        boolean jugadorAtrapado = false;
        
        float velocidadDron = 6.0f;     
        float distanciaDeteccion = 25f; 
        float distanciaLetal = 1.5f;    
        
        float tiempoJuego = aplicacionPrincipal.getTimer().getTimeInSeconds();

        for (int i = 0; i < modelosDrones.size(); i++) {
            Spatial dron = modelosDrones.get(i);
            Vector3f posDron = dron.getLocalTranslation();
            Vector3f origen = origenDrones.get(i); 
            
            float distancia = posDron.distance(posJugador);

            if (distancia < distanciaDeteccion) {
                // --- MODO ATAQUE ---
                // Se obtiene el vector direccional puro (normalizado)
                Vector3f direccion = posJugador.subtract(posDron).normalizeLocal();
                dron.lookAt(posJugador, Vector3f.UNIT_Y);
                Vector3f nuevaPos = posDron.add(direccion.mult(velocidadDron * tpf));
                dron.setLocalTranslation(nuevaPos);

                if (distancia < distanciaLetal) {
                    jugadorAtrapado = true;
                }
            } else {
                // --- MODO PATRULLA ---
                // Uso del círculo trigonométrico (Seno y Coseno) paramétrico
                float patrullaX = origen.x + FastMath.sin(tiempoJuego * 1.5f + i) * 3f; 
                float patrullaZ = origen.z + FastMath.cos(tiempoJuego * 1.5f + i) * 3f;
                float patrullaY = origen.y + FastMath.sin(tiempoJuego * 2f + i) * 0.5f; 
                
                Vector3f objetivoPatrulla = new Vector3f(patrullaX, patrullaY, patrullaZ);
                dron.lookAt(objetivoPatrulla, Vector3f.UNIT_Y);
                
                // Interpolación lineal (LERP) para una traslación suave entre frames
                dron.setLocalTranslation(FastMath.interpolateLinear(tpf * 2f, posDron, objetivoPatrulla));
            }
        }
        return jugadorAtrapado;
    }
    
    private void generarPasilloOscuro(float desplazamientoZ) {
        com.jme3.material.Material matNegro = new com.jme3.material.Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matNegro.setColor("Color", com.jme3.math.ColorRGBA.Black);

        Geometry paredIzq = new Geometry("ParedIzqOscura", new Box(0.5f, 15f, 25f));
        paredIzq.setLocalTranslation(-3.5f, -10f, desplazamientoZ);
        paredIzq.setMaterial(matNegro);
        RigidBodyControl fisIzq = new RigidBodyControl(0f);
        paredIzq.addControl(fisIzq);
        nodoEscenario.attachChild(paredIzq);
        estadoFisicas.getPhysicsSpace().add(fisIzq);

        Geometry paredDer = new Geometry("ParedDerOscura", new Box(0.5f, 15f, 25f));
        paredDer.setLocalTranslation(3.5f, -10f, desplazamientoZ);
        paredDer.setMaterial(matNegro);
        RigidBodyControl fisDer = new RigidBodyControl(0f);
        paredDer.addControl(fisDer);
        nodoEscenario.attachChild(paredDer);
        estadoFisicas.getPhysicsSpace().add(fisDer);

        Geometry techo = new Geometry("TechoOscuro", new Box(3.5f, 0.5f, 25f));
        techo.setLocalTranslation(0f, -2f, desplazamientoZ);
        techo.setMaterial(matNegro);
        RigidBodyControl fisTecho = new RigidBodyControl(0f);
        techo.addControl(fisTecho);
        nodoEscenario.attachChild(techo);
        estadoFisicas.getPhysicsSpace().add(fisTecho);
        
        Geometry suelo = new Geometry("SueloOscuro", new Box(4.0f, 0.5f, 25f));
        suelo.setLocalTranslation(0f, -25f, desplazamientoZ);
        suelo.setMaterial(matNegro);
        RigidBodyControl fisSuelo = new RigidBodyControl(0f);
        suelo.addControl(fisSuelo);
        nodoEscenario.attachChild(suelo);
        estadoFisicas.getPhysicsSpace().add(fisSuelo);

        Geometry luzSalida = new Geometry("LuzSalida", new Box(2.5f, 5f, 0.5f));
        com.jme3.material.Material matLuz = new com.jme3.material.Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matLuz.setColor("Color", com.jme3.math.ColorRGBA.White);
        luzSalida.setMaterial(matLuz);
        luzSalida.setLocalTranslation(0f, -20f, desplazamientoZ - 24.5f);
        nodoEscenario.attachChild(luzSalida);
    }
    
    private void arreglarMaterialesPBR(com.jme3.scene.Spatial modelo, com.jme3.asset.AssetManager assetManager) {
        modelo.depthFirstTraversal(espacial -> {
            if (espacial instanceof com.jme3.scene.Geometry) {
                com.jme3.scene.Geometry geometria = (com.jme3.scene.Geometry) espacial;
                com.jme3.material.Material matActual = geometria.getMaterial();
                if (matActual != null && matActual.getMaterialDef().getName().contains("PBR")) {
                    com.jme3.material.Material matNuevo = new com.jme3.material.Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                    com.jme3.material.MatParam texturaPBR = matActual.getParam("BaseColorMap");
                    if (texturaPBR != null) matNuevo.setTexture("DiffuseMap", (com.jme3.texture.Texture) texturaPBR.getValue());
                    matNuevo.setBoolean("UseMaterialColors", true);
                    matNuevo.setColor("Ambient", new com.jme3.math.ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f)); 
                    matNuevo.setColor("Diffuse", com.jme3.math.ColorRGBA.White); 
                    matNuevo.setFloat("Shininess", 16f);
                    geometria.setMaterial(matNuevo);
                }
            }
        });
    }
}