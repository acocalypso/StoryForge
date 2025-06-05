package de.astronarren.storyforge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import de.astronarren.storyforge.ui.screens.booklist.BookListScreen
import de.astronarren.storyforge.ui.screens.bookdetail.BookDetailScreen
import de.astronarren.storyforge.ui.screens.chapterlist.ChapterListScreen
import de.astronarren.storyforge.ui.screens.chaptereditor.ChapterEditorScreen
import de.astronarren.storyforge.ui.screens.characterlist.CharacterListScreen
import de.astronarren.storyforge.ui.screens.characterdetail.CharacterDetailScreen
import de.astronarren.storyforge.ui.screens.timeline.TimelineScreen
import de.astronarren.storyforge.ui.screens.scenelist.SceneListScreen
import de.astronarren.storyforge.ui.screens.settings.SettingsScreen

@Composable
fun StoryForgeNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BookList
    ) {        composable<Screen.BookList> {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail(bookId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }
          composable<Screen.BookDetail> { backStackEntry ->
            val bookDetail = backStackEntry.toRoute<Screen.BookDetail>()
            
            BookDetailScreen(
                bookId = bookDetail.bookId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChapters = { 
                    navController.navigate(Screen.ChapterList(bookDetail.bookId))
                },
                onNavigateToCharacters = {
                    navController.navigate(Screen.CharacterList(bookDetail.bookId))
                },
                onNavigateToTimeline = {
                    navController.navigate(Screen.Timeline(bookDetail.bookId))
                },
                onNavigateToScenes = {
                    navController.navigate(Screen.SceneList(bookDetail.bookId))
                }
            )
        }
          composable<Screen.ChapterList> { backStackEntry ->
            val chapterList = backStackEntry.toRoute<Screen.ChapterList>()
            
            ChapterListScreen(
                bookId = chapterList.bookId,
                onNavigateBack = { navController.popBackStack() },
                onChapterEdit = { chapterId ->
                    navController.navigate(Screen.ChapterEditor(chapterId))
                }
            )
        }
        
        composable<Screen.ChapterEditor> { backStackEntry ->
            val chapterEditor = backStackEntry.toRoute<Screen.ChapterEditor>()
            
            ChapterEditorScreen(
                chapterId = chapterEditor.chapterId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.CharacterList> { backStackEntry ->
            val args = backStackEntry.arguments
            val bookId = args?.getString("bookId") ?: return@composable
            
            CharacterListScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onCharacterClick = { characterId ->
                    navController.navigate(Screen.CharacterDetail(bookId, characterId))
                }
            )        }
        
        composable<Screen.CharacterDetail> { backStackEntry ->
            val args = backStackEntry.arguments
            val bookId = args?.getString("bookId") ?: return@composable
            val characterId = args?.getString("characterId")
            
            CharacterDetailScreen(
                bookId = bookId,
                characterId = characterId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
          composable<Screen.Timeline> { backStackEntry ->
            val args = backStackEntry.arguments
            val bookId = args?.getString("bookId") ?: return@composable
            
            TimelineScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
          composable<Screen.SceneList> { backStackEntry ->
            val sceneList = backStackEntry.toRoute<Screen.SceneList>()
            
            SceneListScreen(
                bookId = sceneList.bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

