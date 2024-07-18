package global.msnthrp.mokshan.domain.lessons

data class Topic(
    val id: Int,
    val title: String,
    val lessons: List<Lesson>,
    val translations: List<Translation>,
) {

    val topicLength: Int by lazy { lessons.size * 3 + 4 }
}

data class Lesson(
    val order: Int,
    val toNative: List<LessonPair>,
    val dictionary: List<String>,
)

data class LessonPair(
    val mokshan: String,
    val native: List<String>,
)

data class Translation(
    val mokshan: String,
    val native: String,
)