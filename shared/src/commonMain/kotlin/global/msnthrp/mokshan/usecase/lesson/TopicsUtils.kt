package global.msnthrp.mokshan.usecase.lesson

object TopicsUtils {

    fun getTopicLength(lessonsCount: Int): Int {
        return lessonsCount * 2 + 3
    }
}