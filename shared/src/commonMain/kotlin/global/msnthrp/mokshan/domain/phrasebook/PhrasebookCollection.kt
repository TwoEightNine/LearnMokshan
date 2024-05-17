package global.msnthrp.mokshan.domain.phrasebook

object PhrasebookCollection {

    val phrases: Phrasebook = listOf(
        Phrase(
            mokshanPhrase = "Шумбрат",
            translations = listOf(
                Translation(
                    value = "Hello (to one)",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Шумбратада",
            translations = listOf(
                Translation(
                    value = "Hello (to many)",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Няемозонк",
            translations = listOf(
                Translation(
                    value = "Goodbye",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Вандыс",
            translations = listOf(
                Translation(
                    value = "See you tomorrow",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Да",
            translations = listOf(
                Translation(
                    value = "Yes",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Аш",
            translations = listOf(
                Translation(
                    value = "No",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Кода эрят?",
            translations = listOf(
                Translation(
                    value = "How do you do?",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Кода тефне?",
            translations = listOf(
                Translation(
                    value = "How is it going?",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Пара",
            translations = listOf(
                Translation(
                    value = "Good",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Стане",
            translations = listOf(
                Translation(
                    value = "Of course",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Цебярьста",
            translations = listOf(
                Translation(
                    value = "Good",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Кальдяв",
            translations = listOf(
                Translation(
                    value = "Bad",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.COMMON),
        ),
        Phrase(
            mokshanPhrase = "Васедемозонк",
            translations = listOf(
                Translation(
                    value = "See you",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Оду васедемозонк",
            translations = listOf(
                Translation(
                    value = "Until we meet again",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Илядьс",
            translations = listOf(
                Translation(
                    value = "See you tonight",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Сатфкст",
            translations = listOf(
                Translation(
                    value = "Good luck",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Сюкпря",
            translations = listOf(
                Translation(
                    value = "Thanks",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.THANKSGIVING),
        ),
        Phrase(
            mokshanPhrase = "Оцю сюкпря",
            translations = listOf(
                Translation(
                    value = "Thank you very much",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.THANKSGIVING),
        ),
        Phrase(
            mokshanPhrase = "Сувак пара мяльса",
            translations = listOf(
                Translation(
                    value = "Welcome (to one)",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
        Phrase(
            mokshanPhrase = "Сувада пара мяльса",
            translations = listOf(
                Translation(
                    value = "Welcome (to many)",
                    foreignLanguage = ForeignLanguage.ENGLISH,
                ),
            ),
            tags = listOf(Category.GREETINGS),
        ),
    )

    enum class Category {
        GREETINGS,
        COMMON,
        THANKSGIVING
    }
}