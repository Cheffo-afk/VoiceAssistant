package com.cheffoafk.voiceassistant.ui.syllabic.mvp

import android.content.Context
import com.cheffoafk.voiceassistant.data.room.AppDatabase
import com.cheffoafk.voiceassistant.domain.SyllableModel

object SyllabicKeyboardMvpFactory {

    fun createPresenter(context: Context): SyllabicKeyboardPresenter {
        val dao = AppDatabase.getInstance(context).userWordDao()
        val model = SyllableModel(userWordDao = dao)
        return SyllabicKeyboardPresenter(model)
    }
}
