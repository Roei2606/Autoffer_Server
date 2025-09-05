import asyncio
import time
from .docai_client import process_docai_page, process_page_async
from fastapi import FastAPI, UploadFile, File
from .pdf_utils import split_pdf_to_page_buffers

app = FastAPI()


@app.post("/process_pdf/")
async def process_pdf(file: UploadFile = File(...)):
    start_time = time.time()

    pdf_bytes = await file.read()
    page_buffers = split_pdf_to_page_buffers(pdf_bytes)

    # Schedule all page processing concurrently using asyncio.gather
    tasks = [
        process_page_async(page_num, page_bytes)
        for page_num, page_bytes in page_buffers
    ]

    processing_start = time.time()
    results = await asyncio.gather(*tasks)
    processing_end = time.time()

    results.sort(key=lambda x: x["page"])

    end_time = time.time()

    return {
        "results": results,
        "timing": {
            "total_duration_seconds": round(end_time - start_time, 3),
            "processing_duration_seconds": round(processing_end - processing_start, 3),
            "pages_processed": len(results),
            "average_per_page_seconds": (
                round((processing_end - processing_start) / len(results), 3)
                if results
                else 0
            ),
        },
    }
