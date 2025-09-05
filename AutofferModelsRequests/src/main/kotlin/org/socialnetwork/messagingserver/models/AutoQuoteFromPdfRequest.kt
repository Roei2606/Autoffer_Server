package org.socialnetwork.messagingserver.models


data class AutoQuoteFromPdfRequest(
    val projectId: String,
    val factoryId: String,
    val factor: Double? = null,              // אם לא יישלח: 1.0 או מהמפעל בבקשה
    val includeRaw: Boolean? = null,
    val skipUnparseable: Boolean? = null,
    val filename: String? = "boq.pdf",
    val pdfBytes: ByteArray,

    // --- חדשים עבור ה-PDF ---
    val projectAddress: String? = null,
    val client: PdfClientDTO? = null,
    val factory: PdfFactoryUserDTO? = null,
)
