package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

/**
 * AudioController - Gestor Centralizado de Recursos Multimedia.
 * Encapsula la lógica de carga, decodificación y emisión de retroalimentación sonora.
 * Optimiza el uso de memoria RAM al aplicar estrategias de lectura diferenciadas.
 */
public class AudioController {
    
    private AudioNode musicaFondo;
    private AudioNode sonidoLlave;
    private AudioNode sonidoGravedad;
    private AudioNode sonidoMuerte;
    private AudioNode sonidoPuerta;

    // Inyección del AssetManager para acceso directo al sistema de archivos virtuales (Assets)
    public AudioController(AssetManager assetManager, Node nodoRaiz) {
        inicializarAudio(assetManager, nodoRaiz);
    }

    /**
     * Inicialización y cacheo de nodos de audio en el Grafo de Escena.
     * Configura volúmenes relativos (Audio Mixing) y define la propagación acústica.
     */
    private void inicializarAudio(AssetManager assetManager, Node nodoRaiz) {
        // ==========================================
        // MÚSICA DE FONDO (BGM) - OPTIMIZACIÓN DE MEMORIA
        // ==========================================
        // DataType.Stream decodifica el archivo por fragmentos directamente desde el disco rígido.
        // Es imperativo para pistas largas, previniendo el desbordamiento de memoria (OutOfMemoryException).
        musicaFondo = new AudioNode(assetManager, "Sounds/musica_fondo.ogg", DataType.Stream);
        musicaFondo.setLooping(true); // Reproducción cíclica infinita
        musicaFondo.setPositional(false); // Audio 2D (Ambiental), anula la atenuación por distancia espacial
        musicaFondo.setVolume(0.25f);
        nodoRaiz.attachChild(musicaFondo);

        // ==========================================
        // EFECTOS DE SONIDO (SFX) - BAJA LATENCIA
        // ==========================================
        // DataType.Buffer carga el archivo binario completo en la RAM durante la inicialización
        sonidoLlave = new AudioNode(assetManager, "Sounds/llave.ogg", DataType.Buffer);
        sonidoLlave.setPositional(false);
        sonidoLlave.setVolume(0.3f);
        nodoRaiz.attachChild(sonidoLlave);

        sonidoGravedad = new AudioNode(assetManager, "Sounds/gravedad.ogg", DataType.Buffer);
        sonidoGravedad.setPositional(false);
        sonidoGravedad.setVolume(1.0f);
        nodoRaiz.attachChild(sonidoGravedad);

        sonidoMuerte = new AudioNode(assetManager, "Sounds/muerte.ogg", DataType.Buffer);
        sonidoMuerte.setPositional(false);
        sonidoMuerte.setVolume(1.2f);
        nodoRaiz.attachChild(sonidoMuerte);
        
        sonidoPuerta = new AudioNode(assetManager, "Sounds/puerta.ogg", DataType.Buffer);
        sonidoPuerta.setPositional(false);
        sonidoPuerta.setVolume(2.9f);
        nodoRaiz.attachChild(sonidoPuerta);
    }

    // ==========================================
    // MÉTODOS DE CONTROL Y EVENTOS DE ESTADO
    // ==========================================
    
    public void reproducirMusica() {
        // Validación de estado lógico para evitar superposición de hilos (Ghosting) de la misma pista
        if (musicaFondo.getStatus() != com.jme3.audio.AudioSource.Status.Playing) {
            musicaFondo.play();
        }
    }

    public void detenerMusica() {
        musicaFondo.stop();
    }

    // El uso del método playInstance() en lugar de play() permite Polifonía Concurrente.
    // Permite reproducir el mismo SFX múltiples veces simultáneamente sin interrumpir 
    // la instancia de audio anterior (vital al recoger objetos rápidamente).
    public void reproducirLlave() {
        sonidoLlave.playInstance();
    }

    public void reproducirCambioGravedad() {
        sonidoGravedad.playInstance();
    }

    public void reproducirMuerte() {
        sonidoMuerte.playInstance();
    }
    
    public void reproducirPuerta() {
        sonidoPuerta.playInstance();
    }
}
