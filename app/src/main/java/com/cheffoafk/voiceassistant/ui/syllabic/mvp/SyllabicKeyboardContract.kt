package com.cheffoafk.voiceassistant.ui.syllabic.mvp

import android.content.Context

interface SyllabicKeyboardContract {

    interface View {
        fun renderComposedText(text: String)
        fun showSuggestionBar(suggestions: List<String>)
        fun triggerHapticFeedback()
        fun speakFeedback(text: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()

        fun preloadDictionary(context: Context, assetFileName: String = "dictionary_it")

        fun onPrefixClicked(prefix: String)
        fun onSuffixClicked(suffix: String)
        fun onSuggestionClicked(suggestionChunk: String)
        fun onBackspaceClicked()
        fun onSpaceClicked()
    }
}


