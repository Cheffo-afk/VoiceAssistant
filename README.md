# VoiceAssistant2

MVP Android per assistente vocale per persone con disabilita fisico-cognitive, presenta:
- frasi preimpostate riproducibili via TTS;
- scrittura facilitata con suggerimenti locali;
- interfaccia touch con scansione e selezione a singolo/doppio tap;
- tastiera sillabica predittiva con dizionario locale;
- uso del motore vocale nativo del dispositivo;
- onboarding iniziale riproducibile via TTS;
- persistenza locale delle frasi personalizzate;

## Requisiti
- Android Studio recente
- SDK Android configurato
- dispositivo/emulatore Android API 24+

## Avvio rapido
1. Apri il progetto in Android Studio.
2. Esegui la configurazione Gradle.
3. Avvia l'app su dispositivo/emulatore.

## Voce del dispositivo
L'app usa il motore TTS già presente sul telefono e non installa automaticamente motori vocali esterni.

Passi consigliati su dispositivo:
1. Apri l'app.
2. Tocca `Apri impostazioni voce`.
3. Seleziona il motore TTS preferito e verifica la lingua italiana.
4. Se vuoi riascoltare la guida, usa `Ripeti onboarding iniziale` nell'area caregiver.

## Linee guida accessibilita UX

Questo progetto e pensato per la fruibilita da parte di persone con disabilita.
Ogni modifica UI deve rispettare queste regole:

1. Layout stabile: evitare spostamenti improvvisi dei bottoni (niente "salti" quando compaiono/scompaiono contenuti).
2. Niente effetti intermittenti: evitare lampeggi, variazioni rapide di colore, animazioni invasive.
3. Azioni distruttive con conferma esplicita (es. eliminazione frasi).
4. Target touch grandi e consistenti tra schermate.
5. Feedback prevedibile: audio, vibrazione e cambi stato devono essere intenzionali e non sorprendenti.
6. Priorita alla leggibilita: font chiari, contrasto adeguato, gerarchia visiva semplice.

Nota operativa: quando possibile, riservare spazio fisso ai riquadri dinamici (es. completamento) per prevenire frustrazione dovuta ai cambi di posizione.

## Test unitari
Esegui i test da terminale nella root del progetto:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Suite mirata ViewModel (debug rapido):

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.cheffoafk.voiceassistant.ui.HomeScreenViewModelTest.moveSelectionDown_andUp_updatesSelectionWithWrap"
```

## Struttura principale
-- `app/src/main/java/com/cheffoafk/voiceassistant/MainActivity.kt`: bootstrap activity (splash, dependency wiring, theme).
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/VoiceAssistantApp.kt`: root composable e navigazione schermate.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/screens/HomeScreen.kt`: home principale con sidebar scansione.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/screens/SettingsScreen.kt`: impostazioni avanzate.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/screens/OnboardingScreen.kt`: onboarding con narrazione TTS.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/components/PhraseSidebar.kt`: scrollbar + frecce single/double tap.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/components/FacilitatedWritingSection.kt`: scrittura facilitata.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/common/AppLogger.kt`: logger centralizzato (con fallback test JVM).
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/common/UiText.kt`: costanti testuali UI.
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/HomeScreenViewModel.kt`: stato/logica scansione e input.
-- `app/src/main/java/com/cheffoafk/voiceassistant/data/AndroidTtsSynthesizer.kt`: adapter TTS nativo.
-- `app/src/main/java/com/cheffoafk/voiceassistant/data/PhraseRepository.kt`: persistenza frasi custom (`PhraseDataSource`).
-- `app/src/main/java/com/cheffoafk/voiceassistant/data/UserProfileRepository.kt`: persistenza profilo (`UserProfileDataSource`).
-- `app/src/main/java/com/cheffoafk/voiceassistant/data/FrequencyAutocompleteEngine.kt`: suggerimenti locali.
-- `app/src/test/java/com/cheffoafk/voiceassistant/FrequencyAutocompleteEngineTest.kt`: test autocomplete.
-- `app/src/test/java/com/cheffoafk/voiceassistant/ui/HomeScreenViewModelTest.kt`: test logica ViewModel.

## Architettura e LLM futuro

L'app è già predisposta per integrare un LLM in futuro:
- Lo stato è centralizzato nel `HomeScreenViewModel` via `StateFlow`.
- La logica di autocompletamento è nel `FrequencyAutocompleteEngine` (sostituibile).
- Le preferenze dell'utente (frasi usate, parole frequenti) restano locali in SharedPreferences.

### Prossimi passi (WIP)
1. Aggiungere una classe `UserProfile` per tracciare preferenze (parole preferite, stile, frequenza frasi).
2. Integrare un motore di suggerimenti "intelligente" (semplice algoritmo statale o LLM on-device).
3. Mantenere sempre privacy-first: dati sensibili restano locali sul dispositivo.

## Tastiera sillabica predittiva (MVP)

Infrastruttura separata in stile MVP:
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/syllabic/mvp/SyllableKeyboardContract.kt`
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/syllabic/mvp/SyllableKeyboardPresenter.kt`
-- `app/src/main/java/com/cheffoafk/voiceassistant/ui/syllabic/mvp/SyllabicKeyboardMvpFactory.kt`
- `app/src/main/java/com/example/voiceassistant/domain/SyllableModel.kt`
- `app/src/main/java/com/example/voiceassistant/domain/WordsTrie.kt`
- `app/src/main/java/com/example/voiceassistant/data/room/UserWord.kt`
- `app/src/main/java/com/example/voiceassistant/data/room/UserWordDao.kt`
- `app/src/main/java/com/example/voiceassistant/data/room/AppDatabase.kt`

Dizionario locale standard (assets):
- `app/src/main/assets/standard_dictionary_it.txt`

Algoritmo predizione (Strategia Completamento Parola):
1. Estrae l'ultima parola in composizione.
2. Cerca candidati in Room (parole utente ordinate per frequenza) e Trie (dizionario standard).
3. Unisce i candidati dando priorita a Room e rimuovendo duplicati.
4. Espone il prossimo blocco da 3-4 caratteri come suggerimento rapido.

Quando viene premuto spazio, l'ultima parola viene registrata nel DB locale per adattare il ranking alle abitudini dell'utente.




