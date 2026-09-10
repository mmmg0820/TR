package com.softcat.mystictarot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingFlowSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeToQuestionFlowRemainsReachable() {
        openThreeCardQuestion()

        composeRule.onNodeWithText("질문 정리").assertIsDisplayed()
        composeRule.onNodeWithText("현재 의미 · 직접 입력").fetchSemanticsNode()
        composeRule.onNodeWithText("카드 선택으로 이동", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun blankQuestionCanContinueToCardSelection() {
        openThreeCardQuestion()

        composeRule.onNodeWithText("카드 선택으로 이동", useUnmergedTree = true).performClick()

        composeRule.onNodeWithText("0/3장").assertIsDisplayed()
        composeRule.onNodeWithText("결과 보기", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun generatedResultRequiresConfirmationBeforeReselecting() {
        generateThreeCardResult("관계를 정리하고 싶어요")
        composeRule.onNodeWithText("리딩 결과").assertIsDisplayed()
        composeRule.onNodeWithText("질문: 관계를 정리하고 싶어요").assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.onNodeWithText("카드를 다시 선택할까요?").assertIsDisplayed()
        composeRule.onNodeWithText("결과 유지").performClick()
        composeRule.onNodeWithText("리딩 결과").assertIsDisplayed()
    }

    @Test
    fun savedResultSurvivesRecreationWithoutDuplicateRecord() {
        val question = "선택지를 비교하고 싶어요"
        generateThreeCardResult(question)
        composeRule.onNodeWithText("저장", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("저장됨", useUnmergedTree = true).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("리딩 결과").assertIsDisplayed()
        composeRule.onNodeWithText("질문: $question").assertIsDisplayed()
        composeRule.onNodeWithText("저장됨", useUnmergedTree = true).assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("기록 탭").performClick()
        composeRule.onAllNodesWithText(question).assertCountEquals(1)
    }

    @Test
    fun infoScreenShowsInstalledVersion() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("먕타로").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("메뉴 열기").performClick()
        composeRule.onNodeWithText("앱 설정").performClick()
        composeRule.onNodeWithText("정보").performClick()

        composeRule.onNodeWithText("버전 1.0 (1)").assertIsDisplayed()
        composeRule.onNodeWithText("커피 코드").assertIsDisplayed()
    }

    @Test
    fun invalidCoffeeCodeShowsErrorWithoutCrash() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("먕타로").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("메뉴 열기").performClick()
        composeRule.onNodeWithText("앱 설정").performClick()
        composeRule.onNodeWithText("정보").performClick()
        composeRule.onNodeWithText("커피 코드").performClick()

        composeRule.onNodeWithText("커피를 선물해준 당신에게 드리는 서비스입니다. 등록된 코드는 광고 숨김 등 앱 혜택을 적용합니다.")
            .assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("NOT-A-COFFEE-CODE")
        composeRule.onNodeWithText("적용").performClick()
        composeRule.onNodeWithText("등록되지 않은 코드입니다").assertIsDisplayed()
    }

    private fun openThreeCardQuestion() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("새 리딩 시작").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("새 리딩 시작", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("스프레드 선택").assertIsDisplayed()
        composeRule.onNodeWithText("1~10장").performClick()
        composeRule.onNodeWithContentDescription("3장 스프레드").performClick()
        composeRule.onNodeWithText("이 스프레드로 리딩", useUnmergedTree = true).performClick()
    }

    private fun generateThreeCardResult(questionExample: String) {
        openThreeCardQuestion()
        composeRule.onNodeWithContentDescription("질문 예시: $questionExample").performClick()
        composeRule.onNodeWithText("카드 선택으로 이동", useUnmergedTree = true).performClick()
        (1..3).forEach { order ->
            composeRule.onNodeWithContentDescription("${order}번째 카드, 선택 안 됨").performClick()
        }
        composeRule.onNodeWithText("결과 보기", useUnmergedTree = true).performClick()
    }
}
