package com.ncert7.aitutorandlab.ui.components

import com.ncert7.aitutorandlab.utils.isKannadaLanguage

data class LoadingQuote(
    val textEn: String,
    val textKn: String,
    val authorEn: String,
    val authorKn: String,
    val emoji: String,
)

object LoadingQuotes {
    private val quotes = listOf(
        LoadingQuote(
            textEn = "I have no special talent. I am only passionately curious.",
            textKn = "ನನಗೆ ವಿಶೇಷ ಪ್ರತಿಭೆ ಇಲ್ಲ. ನಾನು ಕೇವಲ ಆಸಕ್ತಿಯಿಂದ ಕುತೂಹಲಿಯಾಗಿದ್ದೇನೆ.",
            authorEn = "Albert Einstein",
            authorKn = "ಆಲ್ಬರ್ಟ್ ಐನ್‌ಸ್ಟೀನ್",
            emoji = "🔭",
        ),
        LoadingQuote(
            textEn = "The important thing is not to stop questioning.",
            textKn = "ಪ್ರಶ್ನೆ ಕೇಳುವುದನ್ನು ನಿಲ್ಲಿಸಬಾರದು — ಅದೇ ಮುಖ್ಯ.",
            authorEn = "Albert Einstein",
            authorKn = "ಆಲ್ಬರ್ಟ್ ಐನ್‌ಸ್ಟೀನ್",
            emoji = "❓",
        ),
        LoadingQuote(
            textEn = "Nothing in life is to be feared, it is only to be understood.",
            textKn = "ಜೀವನದಲ್ಲಿ ಏನನ್ನೂ ಭಯಪಡಬೇಕಾಗಿಲ್ಲ; ಅರ್ಥಮಾಡಿಕೊಳ್ಳಬೇಕು.",
            authorEn = "Marie Curie",
            authorKn = "ಮೇರಿ ಕ್ಯೂರಿ",
            emoji = "⚗️",
        ),
        LoadingQuote(
            textEn = "Dream is not that which you see while sleeping — it is something that does not let you sleep.",
            textKn = "ಕನಸು ನಿದ್ರೆಯಲ್ಲಿನ ದೃಶ್ಯವಲ್ಲ — ನಿದ್ರೆಯನ್ನೇ ಬಿಡದ ಕರ್ತವ್ಯವೇ ಕನಸು.",
            authorEn = "A.P.J. Abdul Kalam",
            authorKn = "ಎ.ಪಿ.ಜೆ. ಅಬ್ದುಲ್ ಕಲಾಂ",
            emoji = "🚀",
        ),
        LoadingQuote(
            textEn = "If you want to shine like a sun, first burn like a sun.",
            textKn = "ಸೂರ್ಯನಂತೆ ಹೊಳೆಯಬೇಕಾದರೆ, ಮೊದಲು ಸೂರ್ಯನಂತೆ ಉರಿಯಿರಿ.",
            authorEn = "A.P.J. Abdul Kalam",
            authorKn = "ಎ.ಪಿ.ಜೆ. ಅಬ್ದುಲ್ ಕಲಾಂ",
            emoji = "☀️",
        ),
        LoadingQuote(
            textEn = "The science of today is the technology of tomorrow.",
            textKn = "ಇಂದಿನ ವಿಜ್ಞಾನವೇ ನಾಳೆಯ ತಂತ್ರಜ್ಞಾನ.",
            authorEn = "Edward Teller",
            authorKn = "ಎಡ್ವರ್ಡ್ ಟೆಲ್ಲರ್",
            emoji = "🔬",
        ),
        LoadingQuote(
            textEn = "Somewhere, something incredible is waiting to be known.",
            textKn = "ಎಲ್ಲೋ ಒಂದು ಅದ್ಭುತವು ತಿಳಿದುಕೊಳ್ಳಲು ಕಾಯುತ್ತಿದೆ.",
            authorEn = "Carl Sagan",
            authorKn = "ಕಾರ್ಲ್ ಸೇಗನ್",
            emoji = "🌌",
        ),
        LoadingQuote(
            textEn = "An expert is a person who has made all the mistakes that can be made.",
            textKn = "ತಜ್ಞ = ಮಾಡಬಹುದಾದ ಎಲ್ಲ ತಪ್ಪುಗಳನ್ನೂ ಮಾಡಿದವರು.",
            authorEn = "Niels Bohr",
            authorKn = "ನೀಲ್ಸ್ ಬೋರ್",
            emoji = "😄",
        ),
        LoadingQuote(
            textEn = "I am among those who think that science has great beauty.",
            textKn = "ವಿಜ್ಞಾನದಲ್ಲಿ ಅಪಾರ ಸೌಂದರ್ಯವಿದೆ ಎಂದು ನಾನು ನಂಬುತ್ತೇನೆ.",
            authorEn = "Marie Curie",
            authorKn = "ಮೇರಿ ಕ್ಯೂರಿ",
            emoji = "✨",
        ),
        LoadingQuote(
            textEn = "The good thing about science is that it's true whether or not you believe in it.",
            textKn = "ವಿಜ್ಞಾನದ ಒಳ್ಳೆಯದು — ನೀವು ನಂಬಿದರೂ ಸತ್ಯ, ನಂಬದಿದ್ದರೂ ಸತ್ಯ.",
            authorEn = "Neil deGrasse Tyson",
            authorKn = "ನೀಲ್ ಡಿ ಗ್ರಾಸ್ ಟೈಸನ್",
            emoji = "🪐",
        ),
        LoadingQuote(
            textEn = "Education is the most powerful weapon which you can use to change the world.",
            textKn = "ವಿಶ್ವವನ್ನು ಬದಲಾಯಿಸಲು ಶಿಕ್ಷಣವೇ ಅತ್ಯಂತ ಶಕ್ತಿಶಾಲಿ ಆಯುಧ.",
            authorEn = "Nelson Mandela",
            authorKn = "ನೆಲ್ಸನ್ ಮಂಡೇಲಾ",
            emoji = "📚",
        ),
        LoadingQuote(
            textEn = "Live as if you were to die tomorrow. Learn as if you were to live forever.",
            textKn = "ನಾಳೆ ಸಾವು ಎಂದು ಬದುಕಿರಿ. ಶಾಶ್ವತವಾಗಿ ಬದುಕುವುದು ಎಂದು ಕಲಿಯಿರಿ.",
            authorEn = "Mahatma Gandhi",
            authorKn = "ಮಹಾತ್ಮ ಗಾಂಧಿ",
            emoji = "🌱",
        ),
        LoadingQuote(
            textEn = "Mathematics is the music of reason.",
            textKn = "ಗಣಿತವೆ ತಾರ್ಕಿಕತೆಯ ಸಂಗೀತ.",
            authorEn = "James Joseph Sylvester",
            authorKn = "ಜೇಮ್ಸ್ ಜೋಸೆಫ್ ಸಿಲ್ವೆಸ್ಟರ್",
            emoji = "🎵",
        ),
        LoadingQuote(
            textEn = "Pure mathematics is, in its way, the poetry of logical ideas.",
            textKn = "ಶುದ್ಧ ಗಣಿತವು ತಾರ್ಕಿಕ ಕಲ್ಪನೆಗಳ ಕವಿತೆಯಂತೆ.",
            authorEn = "Albert Einstein",
            authorKn = "ಆಲ್ಬರ್ಟ್ ಐನ್‌ಸ್ಟೀನ್",
            emoji = "📐",
        ),
        LoadingQuote(
            textEn = "If I have seen further, it is by standing on the shoulders of giants.",
            textKn = "ನಾನು ದೂರ ಕಂಡಿದ್ದೇನೆಂದರೆ, ದೈತ್ಯರ ಭುಜದ ಮೇಲೆ ನಿಂತಿದ್ದರಿಂದ.",
            authorEn = "Isaac Newton",
            authorKn = "ಐಸಾಕ್ ನ್ಯೂಟನ್",
            emoji = "🍎",
        ),
        LoadingQuote(
            textEn = "The only way to do great work is to love what you do.",
            textKn = "ಉತ್ತಮ ಕೆಲಸ ಮಾಡುವ ಏಕೈಕ ಮಾರ್ಗ — ನೀವು ಮಾಡುವುದನ್ನು ಪ್ರೀತಿಸುವುದು.",
            authorEn = "Steve Jobs",
            authorKn = "ಸ್ಟೀವ್ ಜಾಬ್ಸ್",
            emoji = "💡",
        ),
        LoadingQuote(
            textEn = "Your brain is like a muscle — the more you use it, the stronger it gets.",
            textKn = "ಮೆದುಳು ಸ್ನಾಯುವಿನಂತೆ — ಹೆಚ್ಚು ಉಪಯೋಗಿಸಿದಷ್ಟೂ ಬಲಿಷ್ಠವಾಗುತ್ತದೆ.",
            authorEn = "Learning tip",
            authorKn = "ಕಲಿಕೆಯ ಸಲಹೆ",
            emoji = "🧠",
        ),
        LoadingQuote(
            textEn = "Mistakes are proof that you are trying.",
            textKn = "ತಪ್ಪುಗಳು = ನೀವು ಪ್ರಯತ್ನಿಸುತ್ತಿದ್ದೀರಿ ಎಂಬ ಸಾಕ್ಷ್ಯ.",
            authorEn = "Classroom wisdom",
            authorKn = "ತರಗತಿ ಬುದ್ಧಿವಂತಿಕೆ",
            emoji = "😊",
        ),
        LoadingQuote(
            textEn = "Scientists are just kids who never stopped asking 'why?'",
            textKn = "ವಿಜ್ಞಾನಿಗಳು 'ಏಕೆ?' ಎಂದು ಕೇಳುವುದನ್ನು ನಿಲ್ಲಿಸದ ಮಕ್ಕಳು.",
            authorEn = "Science joke",
            authorKn = "ವಿಜ್ಞಾನ ನಗೆ",
            emoji = "🤔",
        ),
        LoadingQuote(
            textEn = "C.V. Raman saw light scatter — and India saw a Nobel Prize.",
            textKn = "ಸಿ.ವಿ. ರಾಮನ್ ಬೆಳಕಿನ ಚದುರಿಕೆಯನ್ನು ಕಂಡರು — ಭಾರತಕ್ಕೆ ನೋಬೆಲ್ ಬಂತು.",
            authorEn = "C.V. Raman",
            authorKn = "ಸಿ.ವಿ. ರಾಮನ್",
            emoji = "🌈",
        ),
    )

    fun random(languageCode: String): LoadingQuote =
        quotes.random()
}

fun LoadingQuote.localizedText(languageCode: String): String =
    if (isKannadaLanguage(languageCode)) textKn else textEn

fun LoadingQuote.localizedAuthor(languageCode: String): String =
    if (isKannadaLanguage(languageCode)) authorKn else authorEn
