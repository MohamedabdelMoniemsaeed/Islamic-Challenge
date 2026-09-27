package com.example.data.validation

import com.example.data.models.Question
import com.example.data.models.QuestionDifficulty
import com.example.data.models.QuizCategory

object QuestionValidator {

  /**
   * Light, safe normalization of Arabic and English text for duplicate detection.
   * Collapses multiple whitespaces, strips punctuation like '؟', '?', '!', '.', '،', and trims.
   * Avoids aggressive normalization to ensure distinct questions are never merged.
   */
  fun normalizeText(text: String): String {
    return text
      .trim()
      .replace(Regex("[\\s\u00A0]+"), " ") // collapse whitespaces
      .replace(Regex("[؟?!.,،:;\\-_—\"'«»()\\[\\]{}~]"), "") // remove punctuation
      .trim()
      .lowercase()
  }

  sealed class QuestionValidationError(val message: String) {
    class EmptyId : QuestionValidationError("Question ID must not be blank")
    class EmptyText(val field: String) : QuestionValidationError("Question text ($field) must not be empty")
    class InvalidOptionsCount(val expected: Int, val actual: Int, val lang: String) :
      QuestionValidationError("Options count for $lang must be $expected, but was $actual")
    class DuplicateOption(val option: String, val lang: String) :
      QuestionValidationError("Duplicate option '$option' found in $lang options")
    class InvalidCorrectIndex(val index: Int) :
      QuestionValidationError("Correct answer index $index must be in range 0..3")
    class EmptyExplanation(val lang: String) :
      QuestionValidationError("Explanation ($lang) must not be empty")
    class EmptySource : QuestionValidationError("Source / Reference must not be empty")
    class InvalidReward(val reason: String) : QuestionValidationError("Invalid rewards: $reason")
  }

  data class ValidationResult(
    val isValid: Boolean,
    val errors: List<QuestionValidationError>
  )

  fun validateQuestion(question: Question): ValidationResult {
    val errors = mutableListOf<QuestionValidationError>()

    if (question.id.isBlank()) {
      errors.add(QuestionValidationError.EmptyId())
    }
    if (question.questionAr.isBlank()) {
      errors.add(QuestionValidationError.EmptyText("Arabic"))
    }
    if (question.questionEn.isBlank()) {
      errors.add(QuestionValidationError.EmptyText("English"))
    }

    // Arabic options validation
    if (question.optionsAr.size != 4) {
      errors.add(QuestionValidationError.InvalidOptionsCount(4, question.optionsAr.size, "Arabic"))
    } else {
      val uniqueAr = question.optionsAr.map { it.trim() }.toSet()
      if (uniqueAr.size != 4) {
        errors.add(QuestionValidationError.DuplicateOption("Detected duplicate Arabic options", "Arabic"))
      }
      if (question.optionsAr.any { it.isBlank() }) {
        errors.add(QuestionValidationError.EmptyText("Arabic Option"))
      }
    }

    // English options validation
    if (question.optionsEn.size != 4) {
      errors.add(QuestionValidationError.InvalidOptionsCount(4, question.optionsEn.size, "English"))
    } else {
      val uniqueEn = question.optionsEn.map { it.trim().lowercase() }.toSet()
      if (uniqueEn.size != 4) {
        errors.add(QuestionValidationError.DuplicateOption("Detected duplicate English options", "English"))
      }
      if (question.optionsEn.any { it.isBlank() }) {
        errors.add(QuestionValidationError.EmptyText("English Option"))
      }
    }

    // Correct answer index check
    if (question.correctAnswerIndex !in 0..3) {
      errors.add(QuestionValidationError.InvalidCorrectIndex(question.correctAnswerIndex))
    }

    // Explanations check
    if (question.explanationAr.isBlank()) {
      errors.add(QuestionValidationError.EmptyExplanation("Arabic"))
    }
    if (question.explanationEn.isBlank()) {
      errors.add(QuestionValidationError.EmptyExplanation("English"))
    }

    // Source check
    if (question.source.isBlank()) {
      errors.add(QuestionValidationError.EmptySource())
    }

    // Rewards check
    if (question.xpReward <= 0) {
      errors.add(QuestionValidationError.InvalidReward("xpReward must be > 0 (was ${question.xpReward})"))
    }
    if (question.coinReward < 0) {
      errors.add(QuestionValidationError.InvalidReward("coinReward must be >= 0 (was ${question.coinReward})"))
    }

    return ValidationResult(isValid = errors.isEmpty(), errors = errors)
  }

  data class BankAuditReport(
    val totalCount: Int,
    val validCount: Int,
    val invalidCount: Int,
    val duplicateIdCount: Int,
    val duplicateQuestionCount: Int,
    val countsByCategory: Map<QuizCategory, Int>,
    val countsByDifficulty: Map<QuestionDifficulty, Int>,
    val duplicateIds: List<String>,
    val duplicateQuestions: List<String>,
    val errorsByQuestionId: Map<String, List<String>>
  )

  fun auditQuestionBank(bank: List<Question>): BankAuditReport {
    val idSet = mutableSetOf<String>()
    val duplicateIds = mutableListOf<String>()

    val normalizedArabicSet = mutableSetOf<String>()
    val duplicateQuestions = mutableListOf<String>()

    val errorsMap = mutableMapOf<String, List<String>>()
    var validCount = 0
    var invalidCount = 0

    for (q in bank) {
      // ID check
      if (!idSet.add(q.id)) {
        duplicateIds.add(q.id)
      }

      // Duplicate question text check
      val normAr = normalizeText(q.questionAr)
      if (!normalizedArabicSet.add(normAr)) {
        duplicateQuestions.add(q.questionAr)
      }

      // Content validation
      val res = validateQuestion(q)
      if (res.isValid) {
        validCount++
      } else {
        invalidCount++
        errorsMap[q.id] = res.errors.map { it.message }
      }
    }

    val byCat = QuizCategory.values().associateWith { cat ->
      bank.count { it.category == cat }
    }

    val byDiff = QuestionDifficulty.values().associateWith { diff ->
      bank.count { it.difficulty == diff }
    }

    return BankAuditReport(
      totalCount = bank.size,
      validCount = validCount,
      invalidCount = invalidCount,
      duplicateIdCount = duplicateIds.size,
      duplicateQuestionCount = duplicateQuestions.size,
      countsByCategory = byCat,
      countsByDifficulty = byDiff,
      duplicateIds = duplicateIds,
      duplicateQuestions = duplicateQuestions,
      errorsByQuestionId = errorsMap
    )
  }
}
