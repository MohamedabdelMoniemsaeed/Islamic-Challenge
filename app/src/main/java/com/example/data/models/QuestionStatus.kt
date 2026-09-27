package com.example.data.models

/**
 * Represents the deterministic lifecycle and resolution status of a question within a game session.
 */
enum class QuestionStatus {
  UNANSWERED,
  ANSWERING,
  ANSWERED_CORRECT,
  ANSWERED_WRONG,
  SKIPPED,
  EXPIRED
}
