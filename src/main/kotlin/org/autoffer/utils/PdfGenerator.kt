package org.autoffer.utils

import FactoryUserModel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.autoffer.models.ItemModelDTO
import org.autoffer.models.ProjectModel
import org.autoffer.models.UserModel
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import javax.imageio.IIOException
import org.slf4j.LoggerFactory

data class PdfProject(
    val id: String,
    val projectAddress: String = "-",
    val items: List<ItemModelDTO>
)

data class PdfClient(
    val firstName: String,
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

data class PdfFactoryUser(
    val factoryName: String = "Factory",
    val factor: Double = 1.0
)

@Component
class PdfGenerator {
    private val log = LoggerFactory.getLogger(PdfGenerator::class.java)


    fun generateBoqPdf(project: ProjectModel, client: UserModel, autofferLogoBytes: ByteArray): ByteArray {
        val document = PDDocument()
        val font = PDType1Font.HELVETICA_BOLD
        val fontSize = 13f
        val margin = 50f
        val rowHeight = 25f
        // ⬇️ הורדנו יותר נמוך כדי לא לדרוס את ההדר
        val tableStartY = PDRectangle.A4.height - margin - 250f
        val colWidths = listOf(30f, 70f, 90f, 70f, 70f, 40f, 120f)

        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        val contentStream = PDPageContentStream(document, page)

        // Draw Autoffer logo
        val logoImage = JPEGFactory.createFromByteArray(document, autofferLogoBytes)
        contentStream.drawImage(
            logoImage,
            margin,
            PDRectangle.A4.height - margin - 50f,
            120f,
            50f
        )

        // Header
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, PDRectangle.A4.height - margin - 60f)
        contentStream.showText("Bill of Quantities")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Project ID: ${project.id}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Client: ${client.firstName} ${client.lastName}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Phone: ${client.phoneNumber} | Email: ${client.email}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Project Address: ${project.projectAddress}")
        contentStream.endText()

        // Table header
        var currentX = margin
        var currentY = tableStartY
        val headers = listOf("#", "Profile", "Glass", "Width", "Height", "Qty", "Location")
        for ((i, header) in headers.withIndex()) {
            drawTableCell(contentStream, header, currentX, currentY, colWidths[i], rowHeight, font, fontSize)
            currentX += colWidths[i]
        }
        currentY -= rowHeight

        // Table rows
        for ((index, item) in project.items.withIndex()) {
            currentX = margin
            val rowData = listOf(
                (index + 1).toString(),
                item.profile.profileNumber,
                item.glass.type,
                item.width.toInt().toString(),
                item.height.toInt().toString(),
                item.quantity.toString(),
                item.location
            )
            for ((i, cell) in rowData.withIndex()) {
                drawTableCell(contentStream, cell, currentX, currentY, colWidths[i], rowHeight, font, fontSize)
                currentX += colWidths[i]
            }
            currentY -= rowHeight
        }

        contentStream.close()
        val output = ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }

    fun generateQuotePdf(
        project: ProjectModel,
        client: UserModel,
        factoryUser: FactoryUserModel,
        factoryLogoBytes: ByteArray
    ): ByteArray {
        val document = PDDocument()
        val font = PDType1Font.HELVETICA_BOLD
        val fontSize = 13f
        val margin = 50f
        val rowHeight = 25f
        val tableStartY = PDRectangle.A4.height - margin - 250f
        val colWidths = listOf(30f, 70f, 90f, 70f, 70f, 40f, 80f, 70f)

        var page = PDPage(PDRectangle.A4)
        document.addPage(page)
        var contentStream = PDPageContentStream(document, page)

        // Logo
        val logoImage = try {
            JPEGFactory.createFromByteArray(document, factoryLogoBytes)
        } catch (e: IIOException) {
            log.warn("Invalid JPEG logo: ${e.message}")
            null
        }
        logoImage?.let {
            contentStream.drawImage(it, page.mediaBox.upperRightX - margin - 120f, page.mediaBox.upperRightY - margin - 50f, 120f, 50f)
        }

        // Header
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, PDRectangle.A4.height - margin - 60f)
        contentStream.showText("Quotation from: ${factoryUser.factoryName}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Project ID: ${project.id}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Client: ${client.firstName} ${client.lastName}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Phone: ${client.phoneNumber} | Email: ${client.email}")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Project Address: ${project.projectAddress}")
        contentStream.endText()

        // פונקציה פנימית לציור headers
        fun drawHeaders(y: Float): Float {
            var x = margin
            val headers = listOf("#", "Profile", "Glass", "Width", "Height", "Qty", "Unit $", "Total $")
            for ((i, header) in headers.withIndex()) {
                drawTableCell(contentStream, header, x, y, colWidths[i], rowHeight, font, fontSize)
                x += colWidths[i]
            }
            return y - rowHeight
        }

        var currentY = drawHeaders(tableStartY)

        var subtotal = 0.0
        for ((index, item) in project.items.withIndex()) {
            // אם נגמר מקום – פותחים עמוד חדש
            if (currentY < margin + rowHeight * 6) {
                contentStream.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)
                currentY = drawHeaders(tableStartY)
            }

            var currentX = margin
            val widthM = item.width / 1000.0
            val heightM = item.height / 1000.0
            val areaSqM = widthM * heightM

            val unitPrice = ((item.profile.pricePerSquareMeter * areaSqM) + (item.glass.pricePerSquareMeter * areaSqM)) * factoryUser.factor
            val itemTotal = unitPrice * item.quantity
            subtotal += itemTotal

            val rowData = listOf(
                (index + 1).toString(),
                item.profile.profileNumber,
                item.glass.type,
                item.width.toInt().toString(),
                item.height.toInt().toString(),
                item.quantity.toString(),
                String.format("%.2f", unitPrice),
                String.format("%.2f", itemTotal)
            )

            for ((i, cell) in rowData.withIndex()) {
                drawTableCell(contentStream, cell, currentX, currentY, colWidths[i], rowHeight, font, fontSize)
                currentX += colWidths[i]
            }
            currentY -= rowHeight
        }

        val vat = subtotal * 0.18
        val total = subtotal + vat

        // אם אין מקום totals – פותחים עמוד חדש
        if (currentY < margin + rowHeight * 10) {
            contentStream.close()
            page = PDPage(PDRectangle.A4)
            document.addPage(page)
            contentStream = PDPageContentStream(document, page)
            currentY = PDRectangle.A4.height - margin - 100f
        }

        // Totals
        currentY -= 30f
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, currentY)
        contentStream.showText(String.format("Subtotal: %.2f $", subtotal))
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText(String.format("VAT (18%%): %.2f $", vat))
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText(String.format("Total (incl. VAT): %.2f $", total))
        contentStream.endText()

        // Terms & Notes
        currentY -= 60f
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, currentY)
        contentStream.showText("Terms & Notes:")
        val notes = listOf(
            "1. Prices include measurement, production, delivery, installation, and glazing",
            "2. Installation after painting, plaster (interior & exterior)",
            "3. Electricity, lifting tools, and storage area to be provided by the client",
            "4. Prices are linked to aluminum price index",
            "5. Price includes E.P.D.M rubber",
            "6. Two-year warranty from job completion",
            "7. External sealant is neutral silicone",
            "8. Payment: 20% on signature, 50% with material order, 20% end of production, 10% after installation",
            "9. Final price is determined after site measurements"
        )
        for (note in notes) {
            currentY -= 20f
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(note)
        }
        contentStream.endText()

        // חתימות
        currentY -= 60f
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.newLineAtOffset(margin, currentY)
        contentStream.showText("_____________________             _____________________")
        contentStream.newLineAtOffset(0f, -20f)
        contentStream.showText("Customer Signature                    Factory Signature")
        contentStream.endText()

        contentStream.close()
        val output = ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }

    private fun drawTableCell(
        cs: PDPageContentStream,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        font: PDType1Font,
        fontSize: Float
    ) {
        cs.addRect(x, y, width, height)
        cs.stroke()
        cs.beginText()
        cs.setFont(font, fontSize)
        cs.newLineAtOffset(x + 5f, y + height / 2 - fontSize / 2)
        cs.showText(text)
        cs.endText()
    }
}
