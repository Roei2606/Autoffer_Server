package org.socialnetwork.messagingserver.services.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.socialnetwork.messagingserver.models.ItemModelDTO
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class QuotePdfService {

    fun render(
        projectId: String,
        factoryId: String,
        items: List<ItemModelDTO>,
        finalPrice: Double
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val doc = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(doc, out)
        doc.open()

        val title = Paragraph("הצעת מחיר", Font(Font.HELVETICA, 18f, Font.BOLD))
        title.alignment = Element.ALIGN_RIGHT
        doc.add(title)

        val sub = Paragraph(
            "פרויקט: $projectId   |   מפעל: $factoryId\nתאריך: ${
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now())
            }",
            Font(Font.HELVETICA, 10f)
        )
        sub.alignment = Element.ALIGN_RIGHT
        sub.spacingAfter = 12f
        doc.add(sub)

        val table = PdfPTable(7)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(10f, 12f, 12f, 12f, 14f, 20f, 20f))

        fun headerCell(txt: String) = PdfPCell(Phrase(txt, Font(Font.HELVETICA, 10f, Font.BOLD))).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            backgroundColor = Color(230, 230, 230)
        }
        fun cell(txt: String) = PdfPCell(Phrase(txt, Font(Font.HELVETICA, 9f))).apply {
            horizontalAlignment = Element.ALIGN_CENTER
        }

        table.addCell(headerCell("מספר"))
        table.addCell(headerCell("רוחב (ס\"מ)"))
        table.addCell(headerCell("גובה (ס\"מ)"))
        table.addCell(headerCell("כמות"))
        table.addCell(headerCell("פרופיל"))
        table.addCell(headerCell("שימוש"))
        table.addCell(headerCell("זכוכית"))

        items.forEach { it ->
            table.addCell(cell(it.itemNumber.toString()))
            table.addCell(cell("%.0f".format(it.width)))
            table.addCell(cell("%.0f".format(it.height)))
            table.addCell(cell(it.quantity.toString()))
            table.addCell(cell(it.profile.profileNumber))
            table.addCell(cell(it.profile.usageType.name))
            table.addCell(cell(it.glass.type))
        }

        table.setSpacingAfter(12f)
        table.setSpacingBefore(8f)

        doc.add(table)

        val total = Paragraph("מחיר סופי: %.2f ₪".format(finalPrice), Font(Font.HELVETICA, 14f, Font.BOLD))
        total.alignment = Element.ALIGN_RIGHT
        doc.add(total)

        doc.close()
        return out.toByteArray()
    }
}
