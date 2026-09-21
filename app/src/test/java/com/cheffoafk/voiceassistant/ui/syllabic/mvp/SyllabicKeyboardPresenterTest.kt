package com.cheffoafk.voiceassistant.ui.syllabic.mvp

import com.cheffoafk.voiceassistant.data.room.UserWord
import com.cheffoafk.voiceassistant.data.room.UserWordDao
import com.cheffoafk.voiceassistant.domain.SyllableModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.Runnable
import kotlin.time.Duration.Companion.milliseconds

private object ImmediateDispatcherForPresenterTest : CoroutineDispatcher() {
    override fun dispatch(context: kotlin.coroutines.CoroutineContext, block: Runnable) {
        block.run()
    }
}

private class FakeUserWordDaoForPresenter : UserWordDao {
    private val freq = linkedMapOf<String, Int>()

    override suspend fun findTopPredictionsByPrefix(
        prefix: String,
        limit: Int,
        nowMs: Long,
        minFrequency: Int
    ): List<String> {
        return freq.entries
            .asSequence()
            .filter { it.key.startsWith(prefix) && it.value >= minFrequency }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
            .take(limit)
            .toList()
    }

    override suspend fun incrementFrequency(word: String, timestamp: Long): Int {
        val current = freq[word] ?: return 0
        freq[word] = current + 1
        return 1
    }

    override suspend fun insert(word: UserWord): Long {
        if (freq.containsKey(word.word)) return -1L
        freq[word.word] = word.frequency
        return 1L
    }
}

private class FakeKeyboardView : SyllabicKeyboardContract.View {
    var renderedText: String = ""
    var renderedSuggestions: List<String> = emptyList()
    var hapticCount: Int = 0
    var spokenFeedback: MutableList<String> = mutableListOf()

    override fun renderComposedText(text: String) {
        renderedText = text
    }

    override fun showSuggestionBar(suggestions: List<String>) {
        renderedSuggestions = suggestions
    }

    override fun triggerHapticFeedback() {
        hapticCount++
    }

    override fun speakFeedback(text: String) {
        spokenFeedback.add(text)
    }
}

class SyllabicKeyboardPresenterTest {

    @Test
    fun onPrefixAndSuffix_updatesTextAndSuggestions() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        model.loadDictionaryWords(listOf("acqua", "aiuto"))
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )
        val view = FakeKeyboardView()

        presenter.attachView(view)
        presenter.onPrefixClicked("a")
        presenter.onSuffixClicked("c")

        assertEquals("AC", view.renderedText)
        assertEquals(listOf("qua"), view.renderedSuggestions)
        assertTrue(view.hapticCount >= 2)

        presenter.destroy()
    }

    @Test
    fun suggestions_refresh_whenAppendingConsonantsOrSyllables() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        model.loadDictionaryWords(listOf("acqua", "aiuto", "andiamo"))
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )
        val view = FakeKeyboardView()

        presenter.attachView(view)
        presenter.onPrefixClicked("a")
        val firstSuggestions = view.renderedSuggestions

        presenter.onSuffixClicked("c")
        val secondSuggestions = view.renderedSuggestions

        assertTrue(firstSuggestions.isNotEmpty())
        assertTrue(secondSuggestions.isNotEmpty())
        assertTrue(firstSuggestions != secondSuggestions)

        presenter.destroy()
    }

    @Test
    fun onSpace_registersLastWordInUsageStore() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        model.loadDictionaryWords(listOf("acqua"))
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )
        val view = FakeKeyboardView()

        presenter.attachView(view)
        presenter.onPrefixClicked("a")
        presenter.onSuffixClicked("c")
        presenter.onSuffixClicked("qua")
        presenter.onSpaceClicked()
        delay(10.milliseconds)

        val userPredictions = dao.findTopPredictionsByPrefix("ac", limit = 5)
        assertEquals(listOf("acqua"), userPredictions)
        assertEquals("ACQUA ", view.renderedText)

        presenter.destroy()
    }

    @Test
    fun onSuggestionClicked_appendsSpace_andRegistersCompletedWord() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        model.loadDictionaryWords(listOf("acqua"))
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )
        val view = FakeKeyboardView()

        presenter.attachView(view)
        presenter.onPrefixClicked("ac")
        presenter.onSuggestionClicked("qua")
        delay(10.milliseconds)

        assertEquals("ACQUA ", view.renderedText)
        assertTrue(view.spokenFeedback.isEmpty())
        val userPredictions = dao.findTopPredictionsByPrefix("ac", limit = 5)
        assertEquals(listOf("acqua"), userPredictions)

        presenter.destroy()
    }

    @Test
    fun onSuggestionClicked_preservesInternalSpacesForMultiWordSuggestion() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )
        val view = FakeKeyboardView()

        presenter.attachView(view)
        presenter.onPrefixClicked("per")
        presenter.onSuggestionClicked(" favore")

        assertEquals("PER FAVORE ", view.renderedText)

        presenter.destroy()
    }

    @Test
    fun attachView_afterDestroy_doesNotCrash_andPresenterRemainsUsable() = runBlocking {
        val dao = FakeUserWordDaoForPresenter()
        val model = SyllableModel(dao, ioDispatcher = ImmediateDispatcherForPresenterTest)
        model.loadDictionaryWords(listOf("acqua", "aiuto"))
        val presenter = SyllabicKeyboardPresenter(
            model = model,
            mainDispatcher = ImmediateDispatcherForPresenterTest
        )

        val firstView = FakeKeyboardView()
        presenter.attachView(firstView)
        presenter.onPrefixClicked("a")
        presenter.destroy()

        val secondView = FakeKeyboardView()
        try {
            presenter.attachView(secondView)
            presenter.onSuffixClicked("c")
        } catch (t: Throwable) {
            fail("Il presenter non deve crashare dopo destroy+attach: ${t.message}")
        }

        assertEquals("AC", secondView.renderedText)
        presenter.destroy()
    }
}

