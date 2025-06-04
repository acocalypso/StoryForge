package de.astronarren.storyforge.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object BookList : Screen()
    
    @Serializable
    data class BookDetail(val bookId: String) : Screen()
      @Serializable
    data class ChapterList(val bookId: String) : Screen()
    
    @Serializable
    data class ChapterDetail(val bookId: String, val chapterId: String) : Screen()
    
    @Serializable
    data class ChapterEditor(val chapterId: String) : Screen()
    
    @Serializable
    data class SceneEditor(val bookId: String, val sceneId: String? = null, val chapterId: String? = null) : Screen()
    
    @Serializable
    data class CharacterList(val bookId: String) : Screen()
    
    @Serializable
    data class CharacterDetail(val bookId: String, val characterId: String? = null) : Screen()
      @Serializable
    data class Timeline(val bookId: String) : Screen()
    
    @Serializable
    data class SceneList(val bookId: String) : Screen()
    
    @Serializable
    data object Settings : Screen()
}

