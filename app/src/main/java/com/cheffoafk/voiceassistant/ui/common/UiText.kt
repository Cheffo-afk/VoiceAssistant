package com.cheffoafk.voiceassistant.ui.common

object UiText {
    const val APP_TITLE = "Assistente Vocale"
    const val TAP_TO_SPEAK_HINT = "Tocca una frase per riprodurla"
    const val SCANNING_BANNER = "Scansione attiva\nTocca ovunque per riprodurre"
    const val SETTINGS_TITLE = "Impostazioni avanzate"
    const val KEYBOARD_SHORTCUT = "⌨"

    const val FACILITATED_WRITING_TITLE = "Dai voce alle parole"
    const val FACILITATED_WRITING_DESCRIPTION = "Componi parole o frasi, potrai poi riprodurle e/o salvarle per ogni necessità"
    const val WRITE_HERE = "Scrivi qui"
    const val SAVE_PHRASE = "Salva frase"
    const val PLAY_TEXT = "Leggi testo"
    const val CLEAR_TEXT = "Svuota testo"

    const val BACK = "Torna indietro"
    const val SYLLABIC_KEYBOARD_TITLE = "Tastiera sillabica"
    const val VOWELS = "Vocali"
    const val CONSONANTS = "Consonanti"
    const val COMPOSED_TEXT_PLACEHOLDER = "Tocca le sillabe per comporre..."
    const val SELECT_CONSONANT_GROUP = "Seleziona un gruppo consonantico"
    const val SPACE = "SPAZIO"
    const val SPEAK = "PARLA"
    const val CLEAR = "SVUOTA"
    const val SAVE_TO_HOME = "SALVA"
    const val DELETE_PHRASE = "Elimina frase"
    const val SHOUT = "Volume massimo"
    const val SHOUT_ICON = "📢"


    fun completionLabel(count: Int): String = "Completa ($count)"
    fun syllablesLabel(groupLabel: String): String = "Sillabe: $groupLabel"
}

