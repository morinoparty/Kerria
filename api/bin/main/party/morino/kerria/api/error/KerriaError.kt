package party.morino.kerria.api.error

/**
 * Kerriaプラグインのエラー型
 */
sealed class KerriaError(message: String) : Exception(message) {
    
}