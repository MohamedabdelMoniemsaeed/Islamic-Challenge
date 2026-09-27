package com.example

import com.example.data.models.GameMode
import com.example.data.models.QuestionDifficulty
import com.example.data.models.QuizCategory
import com.example.data.repository.QuestionRepository
import com.example.data.validation.QuestionValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContentAuditAndValidationTest {

  private lateinit var questionRepository: QuestionRepository

  @Before
  fun setUp() {
    questionRepository = QuestionRepository()
  }

  @Test
  fun testTotalQuestionCountAndCategoryDistribution() {
    val allQuestions = questionRepository.getAllQuestions()
    println("Total questions in bank: ${allQuestions.size}")
    assertEquals("Question bank must have exactly 144 questions", 144, allQuestions.size)

    // All 8 categories must be present with exactly 18 questions each
    QuizCategory.values().forEach { category ->
      val count = questionRepository.getQuestionCountForCategory(category)
      println("Category ${category.name}: $count questions")
      assertEquals("Category $category must have exactly 18 questions", 18, count)
    }
  }

  @Test
  fun testQuestionIdsAreUnique() {
    val allQuestions = questionRepository.getAllQuestions()
    val idSet = mutableSetOf<String>()
    val duplicates = mutableListOf<String>()

    for (q in allQuestions) {
      if (!idSet.add(q.id)) {
        duplicates.add(q.id)
      }
    }

    assertTrue("Found duplicate question IDs: $duplicates", duplicates.isEmpty())
  }

  @Test
  fun testNoDuplicateQuestionsExactOrNormalized() {
    val allQuestions = questionRepository.getAllQuestions()
    val normalizedArabic = mutableSetOf<String>()
    val duplicateQuestions = mutableListOf<String>()

    for (q in allQuestions) {
      val norm = QuestionValidator.normalizeText(q.questionAr)
      if (!normalizedArabic.add(norm)) {
        duplicateQuestions.add("${q.id}: '${q.questionAr}' (normalized: '$norm')")
      }
    }

    assertTrue("Found duplicate or near-duplicate questions:\n${duplicateQuestions.joinToString("\n")}", duplicateQuestions.isEmpty())
  }

  @Test
  fun testEveryQuestionHasFourDistinctChoicesBothLanguages() {
    val allQuestions = questionRepository.getAllQuestions()

    for (q in allQuestions) {
      assertEquals("Question ${q.id} must have exactly 4 Arabic choices", 4, q.optionsAr.size)
      assertEquals("Question ${q.id} must have exactly 4 English choices", 4, q.optionsEn.size)

      val uniqueAr = q.optionsAr.map { it.trim() }.toSet()
      assertEquals("Question ${q.id} has duplicate Arabic choices: ${q.optionsAr}", 4, uniqueAr.size)

      val uniqueEn = q.optionsEn.map { it.trim().lowercase() }.toSet()
      assertEquals("Question ${q.id} has duplicate English choices: ${q.optionsEn}", 4, uniqueEn.size)

      q.optionsAr.forEach { opt ->
        assertFalse("Question ${q.id} has blank Arabic option", opt.isBlank())
      }
      q.optionsEn.forEach { opt ->
        assertFalse("Question ${q.id} has blank English option", opt.isBlank())
      }
    }
  }

  @Test
  fun testCorrectAnswerIndicesValidAndBalanced() {
    val allQuestions = questionRepository.getAllQuestions()
    val indexCounts = mutableMapOf<Int, Int>()

    for (q in allQuestions) {
      assertTrue("Question ${q.id} has invalid correct index: ${q.correctAnswerIndex}", q.correctAnswerIndex in 0..3)
      indexCounts[q.correctAnswerIndex] = (indexCounts[q.correctAnswerIndex] ?: 0) + 1
    }

    // Ensure all 4 positions (0, 1, 2, 3) are represented
    for (i in 0..3) {
      val count = indexCounts[i] ?: 0
      println("Option position $i count: $count")
      assertTrue("Answer position $i is never used", count > 0)
    }
  }

  @Test
  fun testNonEmptyExplanationsAndAuthenticSources() {
    val allQuestions = questionRepository.getAllQuestions()

    for (q in allQuestions) {
      assertTrue("Question ${q.id} missing Arabic explanation", q.explanationAr.isNotBlank() && q.explanationAr.length > 5)
      assertTrue("Question ${q.id} missing English explanation", q.explanationEn.isNotBlank() && q.explanationEn.length > 5)
      assertTrue("Question ${q.id} missing source/reference", q.source.isNotBlank())
    }
  }

  @Test
  fun testValidRewardsAndDifficultyDistribution() {
    val allQuestions = questionRepository.getAllQuestions()
    val difficultyCounts = mutableMapOf<QuestionDifficulty, Int>()

    for (q in allQuestions) {
      assertTrue("Question ${q.id} has invalid XP: ${q.xpReward}", q.xpReward > 0)
      assertTrue("Question ${q.id} has negative coins: ${q.coinReward}", q.coinReward >= 0)
      difficultyCounts[q.difficulty] = (difficultyCounts[q.difficulty] ?: 0) + 1
    }

    println("Difficulty distribution: $difficultyCounts")
    QuestionDifficulty.values().forEach { diff ->
      val count = difficultyCounts[diff] ?: 0
      assertTrue("Difficulty level $diff has no questions", count > 0)
    }
  }

  @Test
  fun testQuestionBankFullAuditReport() {
    val allQuestions = questionRepository.getAllQuestions()
    val report = QuestionValidator.auditQuestionBank(allQuestions)

    assertEquals("Expected 0 invalid questions", 0, report.invalidCount)
    assertEquals("Expected 0 duplicate IDs", 0, report.duplicateIdCount)
    assertEquals("Expected 0 duplicate questions", 0, report.duplicateQuestionCount)
    assertEquals("All questions should be valid", allQuestions.size, report.validCount)
  }

  @Test
  fun testRandomizationAndModeFiltering() {
    val quickQuestions = questionRepository.getQuestionsForMode(GameMode.QUICK_CHALLENGE)
    assertEquals(GameMode.QUICK_CHALLENGE.defaultQuestionCount, quickQuestions.size)

    val quranQuestions = questionRepository.getQuestionsForCategory(QuizCategory.QURAN, limit = 10)
    assertEquals(10, quranQuestions.size)
    quranQuestions.forEach { q ->
      assertEquals(QuizCategory.QURAN, q.category)
    }

    // Daily challenge deterministic seed test
    val daily1 = questionRepository.getQuestionsForMode(GameMode.DAILY_CHALLENGE)
    val daily2 = questionRepository.getQuestionsForMode(GameMode.DAILY_CHALLENGE)
    assertEquals("Daily challenge questions must be consistent for the same day", daily1.map { it.id }, daily2.map { it.id })
  }
}
