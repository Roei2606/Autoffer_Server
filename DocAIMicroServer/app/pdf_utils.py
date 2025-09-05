from PyPDF2 import PdfReader, PdfWriter
import io

def split_pdf_to_page_buffers(pdf_bytes):
    reader = PdfReader(io.BytesIO(pdf_bytes))
    page_buffers = []
    for i, page in enumerate(reader.pages):
        writer = PdfWriter()
        writer.add_page(page)
        buf = io.BytesIO()
        writer.write(buf)
        buf.seek(0)
        page_buffers.append((i + 1, buf.getvalue()))
    return page_buffers

