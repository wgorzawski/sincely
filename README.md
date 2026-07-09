# Sincely

Sincely to tracker "kiedy ostatnio coś zrobiłem" — podlewanie kwiatów, wymiana
filtra, backup, cokolwiek co robisz cyklicznie i łatwo zapomnieć kiedy
ostatnio było zrobione. Rdzeń aplikacji to jedno kliknięcie ("check-in"),
z którego wyliczany jest status trackera (`OK` / `WARNING` / `OVERDUE`)
względem oczekiwanej częstotliwości. Offline-first, bez backendu — wszystko
żyje lokalnie w SQLite na urządzeniu.

Ten commit to szkielet: kompletna struktura projektu, konfiguracja buildów i
jeden działający ekran-placeholder na Android i iOS. Zobacz [TODO](#todo---co-świadomie-pominięto)
na dole tego pliku po listę rzeczy celowo zostawionych na później.

## Stack

- **Kotlin Multiplatform** (Kotlin 2.3.21, K2) — cała logika domenowa w `commonMain`
- **SQLDelight 2** — typowane zapytania SQL, `AndroidSqliteDriver` / `NativeSqliteDriver`
- **kotlinx-datetime** + `kotlin.time.Instant` — obliczanie statusu i formatowanie czasu
- **Koin** — DI spinające bazę danych i repozytorium
- **kotlinx.serialization** — modele gotowe pod przyszły eksport/import JSON
- **Android**: Jetpack Compose + Material 3
- **iOS**: SwiftUI, integracja z `shared` bez CocoaPods (`embedAndSignAppleFrameworkForXcode`)

## Struktura projektu

```
sincely/
├─ shared/               # Kotlin Multiplatform
│  └─ src/
│     ├─ commonMain/     # modele domenowe, statusy, SQLDelight, repozytorium, Koin
│     ├─ commonTest/     # testy jednostkowe logiki domenowej
│     ├─ androidMain/    # AndroidSqliteDriver, Koin androidContext
│     └─ iosMain/        # NativeSqliteDriver, fasada dla Swift (IosTrackerGateway)
├─ androidApp/           # Jetpack Compose + Material 3, single-activity
├─ iosApp/               # SwiftUI, projekt Xcode (bez CocoaPods)
├─ gradle/libs.versions.toml
├─ build.gradle.kts, settings.gradle.kts
└─ .github/workflows/ci.yml
```

### Zasada architektoniczna

Cała logika domenowa (statusy, liczenie dni, formatowanie czasu) żyje
wyłącznie w `shared/commonMain`. Warstwy UI (Compose, SwiftUI) niczego nie
liczą — tylko renderują stan, który dostają z repozytorium. Przepływ danych
jest jednokierunkowy: repository (SQLDelight, `Flow`) → ViewModel/gateway →
UI.

## Jak zbudować

Wymagania: JDK 17+. Do budowy `androidApp` potrzebny jest Android SDK
(zmienna `ANDROID_HOME`/`ANDROID_SDK_ROOT` lub plik `local.properties` ze
ścieżką `sdk.dir`). Do budowy `iosApp` potrzebny jest macOS + Xcode.

```bash
# testy jednostkowe logiki domenowej (shared/commonTest)
./gradlew :shared:allTests

# build Androida (APK debug)
./gradlew :androidApp:assembleDebug

# oba na raz
./gradlew :shared:allTests :androidApp:assembleDebug
```

### Android

Zainstaluj wygenerowany APK (`androidApp/build/outputs/apk/debug/`) lub
otwórz projekt w Android Studio i uruchom konfigurację `androidApp`.

### iOS

`shared.framework` buduje się automatycznie przy każdym buildzie Xcode przez
skrypt w fazie "Compile Kotlin Framework" (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`),
więc nie trzeba nic budować ręcznie z linii poleceń wcześniej:

1. Otwórz `iosApp/iosApp.xcodeproj` w Xcode.
2. Wybierz schemat `iosApp` i uruchom na symulatorze.

Projekt celowo nie używa CocoaPods — integracja z `shared` jest "direct"
(Framework Search Paths + `-framework shared` w `OTHER_LDFLAGS`).

## Testy

Testy jednostkowe logiki domenowej (`computeStatus`, `daysSince`,
`RelativeTimeFormatter`) są w `shared/src/commonTest` i uruchamiają się na
JVM (przez `:shared:allTests`, bez potrzeby emulatora/symulatora), bo cała
logika jest czystym Kotlinem bez zależności platformowych.

## TODO — co świadomie pominięto

To jest szkielet (init commit), nie pełna aplikacja. Świadomie pominięte /
uproszczone rzeczy do kolejnych commitów:

- **Ekran check-inu i historii** — repozytorium ma już `checkIn`,
  `observeCheckIns`, `lastCheckIn`, ale żaden ekran jeszcze z nich nie
  korzysta. Ekran-placeholder tylko dodaje hardcodowany tracker (dowód
  przepływu shared → UI), nie robi check-inów.
- **Wyświetlanie statusu (`TrackerStatus`) w UI** — `computeStatus` i
  `RelativeTimeFormatter` mają testy w `commonMain`, ale żaden ekran jeszcze
  ich nie wywołuje do renderowania koloru/badge'a statusu.
- **Formularz dodawania/edycji trackera** — na razie tylko hardcodowany
  tracker testowy z FAB/przycisku "+".
- **Archiwizacja trackera** (`archivedAt`) — pole i zapytanie `selectAll`
  (filtruje po `archivedAt IS NULL`) już istnieją, brak akcji UI do
  archiwizowania.
- **i18n** — `RelativeTimeStrings` i stringi UI (`AndroidStrings`,
  `IosStrings`) są hardcodowane po polsku, ale wydzielone do stałych celowo,
  żeby podpięcie resources/`Localizable.strings` było mechaniczne.
- **Reaktywny ekran iOS** — `IosTrackerGateway` udostępnia jednorazowe
  (`suspend`) wywołania zamiast `Flow`, bo `Flow` nie mostkuje się do Swift
  bez dodatkowego tooling (SKIE / KMP-NativeCoroutines). Ekran odświeża listę
  ręcznie po dodaniu trackera, nie nasłuchuje zmian w bazie na żywo.
- **Eksport/import JSON** — modele mają już `@Serializable`, ale nie istnieje
  jeszcze żaden kod, który faktycznie serializuje/zapisuje/wczytuje plik.
- **Migracje SQLDelight** — schemat jest w wersji 1, brak jeszcze
  mechanizmu/testu migracji (na razie nie ma czego migrować).
- **Ikona aplikacji** — `AppIcon.appiconset` (iOS) i adaptive icon (Android)
  są na razie prostym wektorowym symbolem zastępczym, nie finalnym brandingiem.
- **CI dla iOS** — workflow buduje i testuje tylko `shared` + `androidApp`
  (zgodnie z wymaganiem); build Xcode wymaga runnera macOS i nie jest jeszcze
  spięty.
- **Weryfikacja builda w tym środowisku** — ten init commit został
  wygenerowany w środowisku bez zainstalowanej Javy/Android SDK, więc
  `./gradlew :shared:allTests :androidApp:assembleDebug` nie zostało
  uruchomione lokalnie przy tworzeniu commitu — warto to zrobić jako pierwszy
  krok po sklonowaniu.
