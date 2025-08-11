import os
import json
from PyPDF2 import PdfReader
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

# --- Configuration ---
INPUT_ROOT = 'test_cases'
OUTPUT_FILENAME = 'challenge1b_output.json'
INPUT_FILENAME = 'challenge1b_input.json'
PDFS_SUBDIR = 'PDFs'
TOP_N = 5  # Number of top sections to extract per document

# --- Initialize the Semantic Embedding Model ---
embedding_model = SentenceTransformer('all-MiniLM-L6-v2', cache_folder='your_cache_dir', use_auth_token=None, revision=None, trust_remote_code=False, device=None)

# --- Helper: Extract Headings and Text from PDF ---
def extract_headings_and_text(pdf_path):
    headings = []
    sections = []
    try:
        reader = PdfReader(pdf_path)
        for page_num, page in enumerate(reader.pages, 1):
            text = page.extract_text()
            if text:
                lines = text.split('\n')
                for i, line in enumerate(lines):
                    line = line.strip()
                    if 5 < len(line) < 100:
                        if line.isupper() or line.istitle() or (line[0].isdigit() and '.' in line[:3]):
                            headings.append((line, page_num))
                            # Also store the text following the heading as a section
                            if i+1 < len(lines):
                                section_text = lines[i+1].strip()
                                if section_text:
                                    sections.append((line, section_text, page_num))
        # Remove duplicates
        headings = list({(h, p) for h, p in headings})
        sections = list({(h, t, p) for h, t, p in sections})
    except Exception as e:
        print(f"Error processing PDF {pdf_path}: {e}")
    return headings, sections

# --- Main Processing Loop ---
for collection in os.listdir(INPUT_ROOT):
    collection_path = os.path.join(INPUT_ROOT, collection)
    if not os.path.isdir(collection_path):
        continue
    input_json_path = os.path.join(collection_path, INPUT_FILENAME)
    output_json_path = os.path.join(collection_path, OUTPUT_FILENAME)
    pdfs_dir = os.path.join(collection_path, PDFS_SUBDIR)
    if not os.path.exists(input_json_path) or not os.path.exists(pdfs_dir):
        continue
    print(f"Processing collection: {collection}")
    # Read input JSON
    with open(input_json_path, 'r', encoding='utf-8') as f:
        input_data = json.load(f)
    persona = input_data['persona']['role']
    job = input_data['job_to_be_done']['task']
    documents = input_data['documents']
    # Build query
    query_text = f"Persona: {persona}. Job to be done: {job}"
    query_embedding = embedding_model.encode([query_text])[0]
    # Prepare output
    metadata = {
        "test_cases_documents": [doc['filename'] for doc in documents],
        "persona": persona,
        "job_to_be_done": job
    }
    extracted_sections = []
    subsection_analysis = []
    for doc in documents:
        pdf_path = os.path.join(pdfs_dir, doc['filename'])
        if not os.path.exists(pdf_path):
            print(f"  PDF not found: {pdf_path}")
            continue
        headings, sections = extract_headings_and_text(pdf_path)
        if not headings:
            continue
        # Semantic search on headings
        heading_texts = [h for h, _ in headings]
        heading_embeddings = embedding_model.encode(heading_texts, show_progress_bar=False)
        similarities = cosine_similarity([query_embedding], heading_embeddings)[0]
        # Rank headings
        ranked = sorted(zip(headings, similarities), key=lambda x: x[1], reverse=True)
        for rank, ((heading, page_num), score) in enumerate(ranked[:TOP_N], 1):
            extracted_sections.append({
                "document": doc['filename'],
                "section_title": heading,
                "importance_rank": rank,
                "page_number": page_num
            })
        # Subsection analysis: extract text under top headings
        for ((heading, page_num), score) in ranked[:TOP_N]:
            # Find the section text for this heading
            for h, section_text, p in sections:
                if h == heading and p == page_num:
                    subsection_analysis.append({
                        "document": doc['filename'],
                        "refined_text": section_text,
                        "page_number": page_num
                    })
                    break
    # Write output JSON
    output = {
        "metadata": metadata,
        "extracted_sections": extracted_sections,
        "subsection_analysis": subsection_analysis
    }
    with open(output_json_path, 'w', encoding='utf-8') as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    print(f"  Output written to {output_json_path}") 
