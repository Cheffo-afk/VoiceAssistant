package com.cheffoafk.voiceassistant.domain

interface SpeechSynthesizer {
    fun speak(text: String)
    /**
     * Parla il testo suddiviso dai marker di pausa e notifica tramite callback ogni volta che
     * inizia a leggere un segmento.  [onSegmentStart] riceve l'indice (0-based) del segmento.
     */
    fun speakWithProgress(text: String, onSegmentStart: (segmentIndex: Int) -> Unit)
    fun speakLoud(text: String)
    fun speakLoudPreview(text: String)
    fun setLoudGainMb(gainMb: Int)
    fun getLoudGainMb(): Int
    fun stop()
    fun shutdown()
}

