package app.enjoylearning504.core.ads

/** Every ad slot in the app, with the Tapsell zone the original app used for it. */
enum class AdPlacement(val zoneId: String) {
    NativeLesson("604b08729730480001d660cd"),
    NativeWord("604b08b2fca50c00011d2e86"),
    NativeLearningComplete("6ad725af5b6cc35a9080059a"),
    NativeDifficultWords("6ad7260d9252922579630ee7"),
    NativeClassQuizResult("6ad725819252922579630ee6"),
    NativeLessonQuizResult("6ad725d3fc418616211cb6b9"),
    NativeDifficultQuizResult("6ad7259c5b6cc35a90800599"),

    InterstitialLearning("604b064efca50c00011d2e84"),
    InterstitialClassQuiz("604b05b3fca50c00011d2e83"),
    InterstitialLessonQuiz("604b07b2fca50c00011d2e85"),
    InterstitialDifficultQuiz("604b060e9730480001d660cb"),
}
