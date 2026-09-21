package com.cheffoafk.voiceassistant.ui

import androidx.lifecycle.ViewModel
import com.cheffoafk.voiceassistant.data.PhraseDataSource
import com.cheffoafk.voiceassistant.data.SuggestionUsageDataSource
import com.cheffoafk.voiceassistant.data.UserProfileDataSource
import com.cheffoafk.voiceassistant.domain.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun moveSelectionDown_andUp_updatesSelectionWithWrap() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource()
        val viewModel = createViewModel(phraseDataSource = phraseDataSource)
        try {
            phraseDataSource.savePhraseOrder(
                listOf("frase1", "frase2", "frase3", "frase4", "frase5", "frase6")
            )
            advanceUntilIdle()

            viewModel.moveSelectionDown()
            runCurrent()
            assertEquals(1, viewModel.uiState.value.scanningIndex)

            viewModel.moveSelectionUp()
            runCurrent()
            assertEquals(0, viewModel.uiState.value.scanningIndex)

            viewModel.moveSelectionUp()
            runCurrent()
            assertEquals(5, viewModel.uiState.value.scanningIndex)
        } finally {
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun startDownScan_withoutAvailablePhrases_stopsGracefully() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialIntervalSeconds = 1)
        try {
            advanceUntilIdle()

            viewModel.startDownScanFromCurrentSelection()
            runCurrent()

            assertTrue(viewModel.uiState.value.isScanningMode)
            viewModel.stopScanning()
            runCurrent()
            assertFalse(viewModel.uiState.value.isScanningMode)
        } finally {
            viewModel.stopScanning()
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun updateScanningInterval_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialIntervalSeconds = 4)
        try {
            advanceUntilIdle()

            viewModel.updateScanningInterval(2)
            runCurrent()

            assertFalse(viewModel.uiState.value.isScanningMode)
            assertEquals(2, viewModel.uiState.value.scanningIntervalSeconds)
        } finally {
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun startDownScan_withPhrases_advancesIndexOnEachInterval() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource(initialSavedOrder = listOf("a", "b", "c"))
        val viewModel = createViewModel(initialIntervalSeconds = 1, phraseDataSource = phraseDataSource)
        try {
            advanceUntilIdle()

            viewModel.startDownScanFromCurrentSelection()
            runCurrent()
            assertTrue(viewModel.uiState.value.isScanningMode)
            assertEquals(0, viewModel.uiState.value.scanningIndex)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(1, viewModel.uiState.value.scanningIndex)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(2, viewModel.uiState.value.scanningIndex)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(0, viewModel.uiState.value.scanningIndex)
        } finally {
            viewModel.stopScanning()
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun onScreenTap_duringScan_emitsPhraseAndStopsScanning() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource(initialSavedOrder = listOf("uno", "due", "tre"))
        val userProfileDataSource = FakeUserProfileDataSource(UserProfile(scanningIntervalSeconds = 1))
        val viewModel = createViewModel(
            phraseDataSource = phraseDataSource,
            userProfileDataSource = userProfileDataSource
        )
        val spoken = mutableListOf<String>()
        val collectJob = launch {
            viewModel.speakEffect.take(1).collect { spoken.add(it) }
        }
        try {
            advanceUntilIdle()
            viewModel.moveSelectionDown()
            runCurrent()
            assertEquals(1, viewModel.uiState.value.scanningIndex)

            viewModel.startDownScanFromCurrentSelection()
            runCurrent()
            assertTrue(viewModel.uiState.value.isScanningMode)

            viewModel.onScreenTap()
            runCurrent()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isScanningMode)
            assertEquals(listOf("due"), spoken)
            assertEquals(1, userProfileDataSource.readFrequency("due"))
        } finally {
            collectJob.cancel()
            viewModel.stopScanning()
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun registerSuggestionUse_updatesRankingFromPersistedUsage() = runTest(testDispatcher) {
        val usageDataSource = FakeSuggestionUsageDataSource()
        val viewModel = createViewModel(
            dictionary = listOf("acqua", "aiuto", "amico"),
            suggestionUsageDataSource = usageDataSource
        )
        try {
            advanceUntilIdle()

            viewModel.updateInputText("a")
            runCurrent()
            assertEquals(listOf("acqua", "aiuto", "amico"), viewModel.uiState.value.suggestions)

            viewModel.registerSuggestionUse("aiuto")
            runCurrent()

            assertEquals("aiuto", viewModel.uiState.value.suggestions.first())
        } finally {
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun startUpScan_withPhrases_wrapsFromFirstToLast() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource(initialSavedOrder = listOf("a", "b", "c"))
        val viewModel = createViewModel(initialIntervalSeconds = 1, phraseDataSource = phraseDataSource)
        try {
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.scanningIndex)

            viewModel.startUpScanFromCurrentSelection()
            runCurrent()
            assertTrue(viewModel.uiState.value.isScanningMode)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(2, viewModel.uiState.value.scanningIndex)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(1, viewModel.uiState.value.scanningIndex)
        } finally {
            viewModel.stopScanning()
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun saveExternalPhrase_addsPhraseToHomeList() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource()
        val viewModel = createViewModel(phraseDataSource = phraseDataSource)
        try {
            advanceUntilIdle()

            viewModel.saveExternalPhrase("frase da tastiera")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.allPhrases.contains("frase da tastiera"))
        } finally {
            viewModel.disposeForTest()
            runCurrent()
        }
    }

    @Test
    fun deletePhrase_removesPhraseFromHomeList() = runTest(testDispatcher) {
        val phraseDataSource = FakePhraseDataSource(initialSavedOrder = listOf("uno", "due", "tre"))
        val viewModel = createViewModel(phraseDataSource = phraseDataSource)
        try {
            advanceUntilIdle()

            viewModel.deletePhrase("due")
            advanceUntilIdle()

            assertEquals(listOf("uno", "tre"), viewModel.uiState.value.allPhrases)
        } finally {
            viewModel.disposeForTest()
            runCurrent()
        }
    }


    private fun createViewModel(
        initialIntervalSeconds: Int = 4,
        dictionary: List<String> = listOf("aiuto", "acqua", "ciao"),
        phraseDataSource: FakePhraseDataSource = FakePhraseDataSource(),
        userProfileDataSource: FakeUserProfileDataSource = FakeUserProfileDataSource(
            UserProfile(scanningIntervalSeconds = initialIntervalSeconds)
        ),
        suggestionUsageDataSource: SuggestionUsageDataSource = FakeSuggestionUsageDataSource()
    ): HomeScreenViewModel {
        return HomeScreenViewModel(
            phraseRepository = phraseDataSource,
            userProfileRepository = userProfileDataSource,
            dictionary = dictionary,
            suggestionUsageDataSource = suggestionUsageDataSource
        )
    }
}

private class FakePhraseDataSource(
    initial: List<String> = emptyList(),
    initialSavedOrder: List<String>? = null,
    private val shouldThrowOnAdd: Boolean = false
) : PhraseDataSource {
    private val state = MutableStateFlow(initial)
    private val savedOrderState = MutableStateFlow(initialSavedOrder)

    override val customPhrases: StateFlow<List<String>> = state
    override val savedOrder: StateFlow<List<String>?> = savedOrderState.asStateFlow()

    override fun addPhrase(phrase: String) {
        if (shouldThrowOnAdd) {
            throw IllegalStateException("Simulated addPhrase failure")
        }
        state.value = (state.value + phrase.trim()).filter { it.isNotBlank() }.distinct()
    }

    override fun removePhrase(phrase: String) {
        val normalized = phrase.trim()
        state.value = state.value.filterNot { it == normalized }
        savedOrderState.value = savedOrderState.value?.filterNot { it == normalized }
    }

    override fun savePhraseOrder(orderedPhrases: List<String>) {
        savedOrderState.value = orderedPhrases
    }
}

private class FakeUserProfileDataSource(
    private var profile: UserProfile
) : UserProfileDataSource {
    private val frequency = mutableMapOf<String, Int>()

    fun readFrequency(phrase: String): Int = frequency[phrase] ?: 0

    override fun getUserProfile(): UserProfile {
        return profile.copy(phraseFrequency = frequency.toMap())
    }

    override fun updateScanningInterval(seconds: Int) {
        profile = profile.copy(scanningIntervalSeconds = seconds)
    }

    override fun recordPhraseUse(phrase: String) {
        frequency[phrase] = (frequency[phrase] ?: 0) + 1
    }
}

private class FakeSuggestionUsageDataSource(
    initialCounts: Map<String, Int> = emptyMap()
) : SuggestionUsageDataSource {
    private val state = MutableStateFlow(initialCounts)

    override val usageCounts: StateFlow<Map<String, Int>> = state.asStateFlow()

    override fun recordUsage(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.isBlank()) return

        state.value = state.value.toMutableMap().apply {
            put(normalized, (get(normalized) ?: 0) + 1)
        }.toMap()
    }
}

private fun HomeScreenViewModel.disposeForTest() {
    // Nelle varie versioni lifecycle il metodo può essere clear() o clear$..._release.
    val methods = ViewModel::class.java.declaredMethods + this::class.java.declaredMethods
    val clearLike = methods.firstOrNull { it.name.startsWith("clear") && it.parameterCount == 0 }
    if (clearLike != null) {
        runCatching {
            clearLike.isAccessible = true
            clearLike.invoke(this)
        }
    }
}
