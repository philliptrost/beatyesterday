package com.beatyesterday.web.controller

import com.beatyesterday.application.import.RunImportUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Single endpoint to trigger a full Strava data import. The import runs synchronously —
 * the frontend shows a loading state while waiting.
 * TODO: For large accounts, consider making this async with a progress endpoint.
 */
@RestController
@RequestMapping("/api/import")
class ImportController(
    private val runImportUseCase: RunImportUseCase,
) {

    @PostMapping
    fun triggerImport(): ResponseEntity<Map<String, Any>> {
        return try {
            runImportUseCase.execute()
            ResponseEntity.ok(mapOf("success" to true, "message" to "Import completed successfully"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf("success" to false, "message" to (e.message ?: "Import failed"))
            )
        }
    }
}
