package com.mygame;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

/**
 * UIController - Gestor de Interfaz Gráfica de Usuario (GUI).
 * Maneja el renderizado en el plano ortogonal (2D) del motor gráfico.
 * Encargado del HUD (Heads-Up Display), menús modales y retroalimentación visual.
 */
public class UIController {
    private Main aplicacionPrincipal;
    private Node guiNode;
    private BitmapFont fuentePrincipal;
    
    private Node nodoInstrucciones;
    private BitmapText textoInstrucciones;

    // Elementos de la Pantalla de Inicio
    private Picture logoInicio;
    private BitmapText mensajeInicio;
    
    // Elementos del HUD del Juego (Texto)
    private BitmapText textoFase;
    private BitmapText textoTiempo;
    private BitmapText textoLlaves; 
    private BitmapText mensajeCentroPantalla;

    // --- ICONOS EN PANEL 2D ---
    private Picture iconoLlave;
    private Picture iconoGravedad;

    private float tiempoParpadeo = 0f;
    private boolean mostrarInicio = true;
    private int anchoPantalla;
    private int altoPantalla;
    private final float DIMENSION_ICONO = 52f;

    public UIController(Main aplicacionPrincipal) {
        this.aplicacionPrincipal = aplicacionPrincipal;
        // Se obtiene el guiNode, un nodo especial de JME que no es afectado por la cámara 3D
        this.guiNode = aplicacionPrincipal.getGuiNode();
        this.fuentePrincipal = aplicacionPrincipal.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        
        AppSettings settings = aplicacionPrincipal.getContext().getSettings();
        this.anchoPantalla = settings.getWidth();
        this.altoPantalla = settings.getHeight();

        inicializarHUD();
    }

    /**
     * Construcción de la Interfaz mediante posicionamiento dinámico.
     * Se utilizan cálculos matemáticos relativos a la resolución de la pantalla 
     * (anchoPantalla / altoPantalla) para asegurar que la UI sea responsiva.
     */
    private void inicializarHUD() {
        float tamanioBase = fuentePrincipal.getCharSet().getRenderedSize();

        // ==========================================
        // 1. PANTALLA DE INICIO (Logo)
        // ==========================================
        logoInicio = new Picture("LogoJuego");
        logoInicio.setImage(aplicacionPrincipal.getAssetManager(), "Textures/logo.png", true);
        float anchoLogo = 700f; 
        float altoLogo = 500f;  
        logoInicio.setWidth(anchoLogo);
        logoInicio.setHeight(altoLogo);

        // Cálculo algebraico para centrado dinámico en pantalla
        float posXLogo = (anchoPantalla - anchoLogo) / 2f;
        float posYLogo = (altoPantalla - altoLogo) / 2f + 80f; 
        
        logoInicio.setPosition(posXLogo, posYLogo);
        guiNode.attachChild(logoInicio);

        mensajeInicio = new BitmapText(fuentePrincipal, false);
        mensajeInicio.setSize(tamanioBase * 1.5f);
        mensajeInicio.setText("- PRESIONE [ENTER] PARA INICIAR SIMULACIÓN -");
        mensajeInicio.setColor(ColorRGBA.White);
        mensajeInicio.setLocalTranslation((anchoPantalla - mensajeInicio.getLineWidth()) / 2f, (altoPantalla - 200f) / 2f + 40f, 0);
        guiNode.attachChild(mensajeInicio);

        // ==========================================
        // 2. CONFIGURACIÓN DE TEXTOS DEL HUD
        // ==========================================
        textoFase = new BitmapText(fuentePrincipal, false);
        textoFase.setSize(tamanioBase * 1.5f);
        textoFase.setColor(ColorRGBA.White);
        textoFase.setLocalTranslation(20f, altoPantalla - 20f, 0); 

        textoTiempo = new BitmapText(fuentePrincipal, false);
        textoTiempo.setSize(tamanioBase * 1.5f);
        textoTiempo.setColor(ColorRGBA.White);

        textoLlaves = new BitmapText(fuentePrincipal, false);
        textoLlaves.setSize(tamanioBase * 1.5f);
        textoLlaves.setColor(ColorRGBA.White);

        mensajeCentroPantalla = new BitmapText(fuentePrincipal, false);
        mensajeCentroPantalla.setSize(tamanioBase * 1.5f);

        // ==========================================
        // 3. INSTANCIACIÓN DE ICONOS (PICTURES)
        // ==========================================
        iconoLlave = new Picture("IconoLlave");
        iconoLlave.setImage(aplicacionPrincipal.getAssetManager(), "Textures/icon_key.png", true);
        iconoLlave.setWidth(DIMENSION_ICONO + 20f);
        iconoLlave.setHeight(DIMENSION_ICONO);

        iconoGravedad = new Picture("IconoGravedad");
        iconoGravedad.setWidth(DIMENSION_ICONO + 40f);
        iconoGravedad.setHeight(DIMENSION_ICONO);
        iconoGravedad.setPosition(15f, altoPantalla - 105f);
        
        // ==========================================
        // 4. PANTALLA DE INSTRUCCIONES (PAUSA / MODAL)
        // ==========================================
        nodoInstrucciones = new Node("NodoInstrucciones");

        // --- FILTRADO DE TEXTURAS (Calidad HD) ---
        // Aplicación de filtro bilineal (Bilinear Interpolation) a la textura de la fuente
        // para evitar el aliasing (efecto sierra) al escalar el BitmapText.
        com.jme3.texture.Texture texturaFuente = fuentePrincipal.getPage(0).getTextureParam("ColorMap").getTextureValue();
        texturaFuente.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
        texturaFuente.setMinFilter(com.jme3.texture.Texture.MinFilter.BilinearNoMipMaps);

        // --- MEDIDAS DEL PANEL ---
        float anchoPanel = anchoPantalla * 0.6f;
        float altoPanel = altoPantalla * 0.45f;
        float posXPanel = (anchoPantalla - anchoPanel) / 2f;
        float posYPanel = (altoPantalla - altoPanel) / 2f;

        // --- MANEJO DE TRANSPARENCIAS (Canal Alpha) ---
        Material matFondo = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.0f, 0.35f, 0.5f, 0.35f)); 
        // Se activa el BlendMode.Alpha para renderizar el color con opacidad parcial
        matFondo.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Geometry fondoOscuro = new Geometry("FondoOscuro", new Quad(anchoPanel, altoPanel));
        fondoOscuro.setMaterial(matFondo);
        fondoOscuro.setLocalTranslation(posXPanel, posYPanel, -1); 
        nodoInstrucciones.attachChild(fondoOscuro);

        // --- MARCO DE NEÓN LIMPIO (Geometría Básica) ---
        Material matBorde = new Material(aplicacionPrincipal.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBorde.setColor("Color", ColorRGBA.Cyan);
        float grosor = 2.5f; 
        
        Geometry lineaSup = new Geometry("BordeSup", new Quad(anchoPanel, grosor));
        lineaSup.setMaterial(matBorde);
        lineaSup.setLocalTranslation(posXPanel, posYPanel + altoPanel, 0);
        nodoInstrucciones.attachChild(lineaSup);

        Geometry lineaInf = new Geometry("BordeInf", new Quad(anchoPanel, grosor));
        lineaInf.setMaterial(matBorde);
        lineaInf.setLocalTranslation(posXPanel, posYPanel, 0);
        nodoInstrucciones.attachChild(lineaInf);

        Geometry lineaIzq = new Geometry("BordeIzq", new Quad(grosor, altoPanel));
        lineaIzq.setMaterial(matBorde);
        lineaIzq.setLocalTranslation(posXPanel, posYPanel, 0);
        nodoInstrucciones.attachChild(lineaIzq);

        Geometry lineaDer = new Geometry("BordeDer", new Quad(grosor, altoPanel));
        lineaDer.setMaterial(matBorde);
        lineaDer.setLocalTranslation(posXPanel + anchoPanel, posYPanel, 0);
        nodoInstrucciones.attachChild(lineaDer);

        // --- CAJA DELIMITADORA DE TEXTO (Bounding Box) ---
        textoInstrucciones = new BitmapText(fuentePrincipal, false);
        textoInstrucciones.setSize(tamanioBase * 1.8f); 
        textoInstrucciones.setColor(ColorRGBA.Cyan);
        
        float anchoCaja = anchoPanel * 0.9f;
        float altoCaja = altoPanel * 0.8f;
        // Asignación de un Rectangle matemático para forzar el salto de línea automático (Word Wrap)
        textoInstrucciones.setBox(new com.jme3.font.Rectangle(0, 0, anchoCaja, altoCaja));
        textoInstrucciones.setAlignment(BitmapFont.Align.Center);
        textoInstrucciones.setVerticalAlignment(BitmapFont.VAlign.Center);
        
        float posXTexto = posXPanel + (anchoPanel * 0.05f);
        float posYTexto = posYPanel + altoPanel - (altoPanel * 0.1f);
        textoInstrucciones.setLocalTranslation(posXTexto, posYTexto, 0);
        
        nodoInstrucciones.attachChild(textoInstrucciones);

        BitmapText textoContinuar = new BitmapText(fuentePrincipal, false);
        textoContinuar.setSize(tamanioBase * 1.5f);
        textoContinuar.setText("PRESIONA [ESPACIO] PARA COMENZAR");
        textoContinuar.setColor(ColorRGBA.White);
        textoContinuar.setLocalTranslation(anchoPantalla / 2f - textoContinuar.getLineWidth() / 2f, posYPanel + 40f, 0);
        nodoInstrucciones.attachChild(textoContinuar);
    }
    
    public void mostrarMensajeInicio() {
        mostrarInicio = true;
        tiempoParpadeo = 0f;
        
        if (logoInicio.getParent() == null) guiNode.attachChild(logoInicio);
        if (mensajeInicio.getParent() == null) guiNode.attachChild(mensajeInicio);
        
        if (textoFase.getParent() != null) guiNode.detachChild(textoFase);
        if (textoTiempo.getParent() != null) guiNode.detachChild(textoTiempo);
        if (textoLlaves.getParent() != null) guiNode.detachChild(textoLlaves);
        if (iconoLlave.getParent() != null) guiNode.detachChild(iconoLlave);
        if (iconoGravedad.getParent() != null) guiNode.detachChild(iconoGravedad);
        if (mensajeCentroPantalla.getParent() != null) guiNode.detachChild(mensajeCentroPantalla);
    }

    /**
     * Animación generada proceduralmente mediante Funciones Senoidales.
     * Calcula la curva del canal Alpha para lograr un efecto de pulso (Fade In/Out).
     */
    public void actualizarEfectos(float tpf) {
        if (mostrarInicio && mensajeInicio != null) {
            tiempoParpadeo += tpf * 3f; 
            float alpha = (FastMath.sin(tiempoParpadeo) + 1f) / 2f; 
            mensajeInicio.setColor(new ColorRGBA(1f, 1f, 1f, alpha));
        }
    }

    public void ocultarMensajeInicio() {
        mostrarInicio = false;
        guiNode.detachChild(logoInicio);
        guiNode.detachChild(mensajeInicio);

        guiNode.attachChild(textoFase);
        guiNode.attachChild(textoTiempo);
        guiNode.attachChild(textoLlaves);
        guiNode.attachChild(iconoLlave);
    }

    public void actualizarFase(int fase) {
        textoFase.setText("FASE ACTUAL: " + String.format("%02d", fase));
    }

    public void actualizarTiempo(float tiempoRestante) {
        textoTiempo.setText(String.format("%.1f", Math.max(0, tiempoRestante)) + "s");
        
        float posXTexto = anchoPantalla - textoTiempo.getLineWidth() - 20f;
        textoTiempo.setLocalTranslation(posXTexto, altoPantalla - 20f, 0);
        
        if (tiempoRestante <= 15f) {
            textoTiempo.setColor(ColorRGBA.Red);
        } else {
            textoTiempo.setColor(ColorRGBA.White);
        }
    }

    public void actualizarDatos(int recolectados, int necesarios) {
        textoLlaves.setText("LLAVES: [" + recolectados + "/" + necesarios + "]");
        
        float posXTexto = (anchoPantalla - textoLlaves.getLineWidth()) / 2f + 20f;
        textoLlaves.setLocalTranslation(posXTexto, altoPantalla - 20f, 0);
        
        iconoLlave.setPosition(posXTexto - 80f, altoPantalla - 60f);
        
        if (recolectados >= necesarios) {
            textoLlaves.setColor(new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f)); 
        } else {
            textoLlaves.setColor(ColorRGBA.White);
        }
    }

    public void actualizarGravedadHUD(boolean tieneDispositivo, boolean estaInvertida) {
        if (!tieneDispositivo) {
            if (iconoGravedad.getParent() != null) {
                guiNode.detachChild(iconoGravedad);
            }
            return;
        }

        if (iconoGravedad.getParent() == null) {
            guiNode.attachChild(iconoGravedad);
        }

        if (estaInvertida) {
            iconoGravedad.setImage(aplicacionPrincipal.getAssetManager(), "Textures/icon_gravity_on.png", true);
        } else {
            iconoGravedad.setImage(aplicacionPrincipal.getAssetManager(), "Textures/icon_gravity_off.png", true);
        }
    }

    public void mostrarMensajeAcercarse() {
        mensajeCentroPantalla.setText("LLAVES RECOLECTADAS. DIRÍJASE A LA SALIDA.");
        mensajeCentroPantalla.setColor(ColorRGBA.White);
        centrarMensajePantalla();
        guiNode.attachChild(mensajeCentroPantalla);
    }

    /**
     * Gestión Concurrente (Multithreading).
     * Utiliza un Thread secundario para contar el tiempo y la función enqueue()
     * para reingresar al hilo principal (Render Thread) de JME y actualizar el Scene Graph de forma segura.
     */
    public void mostrarMensajeAcceso(String mensaje) {
        mensajeCentroPantalla.setText(mensaje);
        mensajeCentroPantalla.setColor(new ColorRGBA(0f, 1f, 0f, 1f)); 
        centrarMensajePantalla();
        guiNode.attachChild(mensajeCentroPantalla);
        
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Pausa el hilo secundario
                // Retorna la ejecución al hilo principal de la aplicación
                aplicacionPrincipal.enqueue(() -> guiNode.detachChild(mensajeCentroPantalla));
            } catch (Exception e) {}
        }).start();
    }

    private void centrarMensajePantalla() {
        float posX = (anchoPantalla - mensajeCentroPantalla.getLineWidth()) / 2f;
        float posY = (altoPantalla + mensajeCentroPantalla.getLineHeight()) / 2f;
        mensajeCentroPantalla.setLocalTranslation(posX, posY, 0);
    }
    
    public void mostrarInstrucciones(String texto) {
        textoInstrucciones.setText(texto);
        guiNode.attachChild(nodoInstrucciones);
    }

    public void ocultarInstrucciones() {
        guiNode.detachChild(nodoInstrucciones);
    }
}