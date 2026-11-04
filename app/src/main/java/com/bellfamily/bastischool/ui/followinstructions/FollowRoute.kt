package com.bellfamily.bastischool.ui.followinstructions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bellfamily.bastischool.ShellNavigation
import com.bellfamily.bastischool.ShellScreen
import com.bellfamily.bastischool.learning.models.ContentLanguage

@Composable
internal fun FollowRoute(
    screen: ShellScreen, navigation: ShellNavigation, viewModel: FollowViewModel,
    languageCode: String, modifier: Modifier, navigate: (ShellNavigation) -> Unit,
    cancelCelebration: () -> Unit, onPop: (String) -> Unit
) {
    val language = if (languageCode == "de") ContentLanguage.GERMAN else ContentLanguage.ENGLISH
    when (screen) {
        ShellScreen.FOLLOW_LEVELS -> FollowLevelsScreen(language,
            { navigate(navigation.openFollowOne()) }, { navigate(navigation.openFollowTwo()) },
            { navigate(navigation.openFollowSchoolSkills()) }, { navigate(navigation.openFollowSchoolActions()) },
            { navigate(navigation.openFollowSchoolActionSequence()) },
            { navigate(navigation.openFollowSchoolActionTriple()) },
            { navigate(navigation.openClassroomHelperBoard()) },
            { navigate(navigation.home()) }, modifier)
        ShellScreen.FOLLOW_INSTRUCTIONS -> FollowScreen(viewModel.state, language, viewModel.images,
            viewModel.busy, viewModel.saveFailed, viewModel.audioFailed, viewModel::action, viewModel::replay,
            viewModel::again, viewModel::retrySave, onHome = { navigate(navigation.home()) },
            modifier = modifier, onPop = onPop, onLevels = { navigate(navigation.openFollowInstructions()) })
        ShellScreen.FOLLOW_TWO -> FollowTwoScreen(viewModel.twoState, language, viewModel.images,
            viewModel.busy, viewModel.saveFailed, viewModel.audioFailed, viewModel::twoAction,
            viewModel::twoReplay, viewModel::twoExample, viewModel::twoAgain, viewModel::retrySave,
            { navigate(navigation.home()) }, modifier, onPop,
            { navigate(navigation.openFollowInstructions()) })
        ShellScreen.FOLLOW_SCHOOL -> SchoolSkillsScreen(viewModel.schoolState, language,
            viewModel.schoolScene, viewModel.schoolIllustrations, viewModel.busy, viewModel.saveFailed,
            viewModel.audioFailed, viewModel::schoolAction, viewModel::schoolReplay,
            { cancelCelebration(); viewModel.schoolAgain() }, viewModel::retrySave,
            { navigate(navigation.home()) }, { navigate(navigation.openFollowInstructions()) }, modifier, onPop)
        ShellScreen.FOLLOW_SCHOOL_ACTIONS -> SchoolActionsRoute(viewModel, languageCode, modifier,
            { cancelCelebration(); viewModel.schoolActionsAgain() },
            { navigate(navigation.home()) }, { navigate(navigation.openFollowInstructions()) }, onPop)
        ShellScreen.FOLLOW_SCHOOL_ACTION_SEQUENCE -> SchoolActionSequenceRoute(viewModel, languageCode, modifier,
            { cancelCelebration(); viewModel.actionSequenceAgain() },
            { navigate(navigation.home()) }, { navigate(navigation.openFollowInstructions()) }, onPop)
        ShellScreen.FOLLOW_SCHOOL_ACTION_TRIPLE -> SchoolActionTripleRoute(viewModel, languageCode, modifier,
            { cancelCelebration(); viewModel.actionTripleAgain() },
            { navigate(navigation.home()) }, { navigate(navigation.openFollowInstructions()) }, onPop)
        ShellScreen.FOLLOW_CLASSROOM_HELPER_BOARD -> ClassroomHelperRoute(viewModel, languageCode, modifier,
            { cancelCelebration(); viewModel.classroomHelperAgain() },
            { navigate(navigation.home()) }, { navigate(navigation.openFollowInstructions()) }, onPop)
        else -> Unit
    }
}
