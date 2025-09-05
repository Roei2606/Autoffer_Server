from google.cloud import documentai_v1 as documentai
from .config import PROCESSOR_NAME
import anyio

def process_docai_page(page_bytes):
    client = documentai.DocumentProcessorServiceClient()
    raw_document = documentai.RawDocument(content=page_bytes, mime_type="application/pdf")
    request = documentai.ProcessRequest(name=PROCESSOR_NAME, raw_document=raw_document)
    result = client.process_document(request=request)
    document = result.document
    fields = {}
    for entity in document.entities:
        fields[entity.type_] = entity.mention_text
    return fields

# process_page_async runs process_docai_page in a thread
# this is the recommended FastAPI way to sync code (GCP SDK) in async endpoints
async def process_page_async(page_num, page_bytes): 
    try:
        fields = await anyio.to_thread.run_sync(process_docai_page, page_bytes)
        return {"page": page_num, "fields": fields}
    except Exception as e:
        return {"page": page_num, "fields": None, "error": str(e)}