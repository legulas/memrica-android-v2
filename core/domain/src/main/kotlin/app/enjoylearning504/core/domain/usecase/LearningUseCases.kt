package app.enjoylearning504.core.domain.usecase

import app.enjoylearning504.core.domain.learning.LearningSessionEngine
import app.enjoylearning504.core.domain.learning.LearningState
import app.enjoylearning504.core.domain.learning.LevelCompleted
import app.enjoylearning504.core.domain.repository.ContentRepository
import app.enjoylearning504.core.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first

class StartLearningSessionUseCase(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
    private val engine: LearningSessionEngine,
) {
    /** Returns null when the lesson is already fully learned. */
    suspend operator fun invoke(lessonId: Int): LearningState? {
        val words = content.words().filter { it.lessonId == lessonId }
        return engine.start(words.withProgress(progress.observeWordProgress().first()))
    }
}

class SaveLevelUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(completed: LevelCompleted) = progress.setLevel(completed.wordId, completed.level)
}
