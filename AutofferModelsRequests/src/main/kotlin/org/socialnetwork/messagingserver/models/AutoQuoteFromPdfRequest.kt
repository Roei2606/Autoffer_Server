package org.socialnetwork.messagingserver.models

// ⚠️ הערה: בבקשת RSocket, ByteArray יישלח בבסיס-64 (Jackson) – זה תקין ל-MVP.
// filename לא חובה; משמש רק להעברה ל-DocAI (נוח ללוגים).
data class AutoQuoteFromPdfRequest(
    val projectId: String,
    val factoryId: String,
    val factor: Double? = null,
    val includeRaw: Boolean? = null,     // נשמור לשלב הבא
    val skipUnparseable: Boolean? = null, // נשמור לשלב הבא
    val filename: String? = "boq.pdf",
    val pdfBytes: ByteArray
)
