import os
from PyPDF2 import PdfReader
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

# --- Configuration ---
PDF_DIRECTORY = "test_cases"  # Directory containing your PDF files
OUTPUT_TOP_N_HEADINGS = 10  # How many top relevant headings to return

# --- Step 1: Initialize the Semantic Embedding Model ---
try:
    embedding_model = SentenceTransformer('all-MiniLM-L6-v2')
    print("Sentence Transformer model loaded successfully.")
except Exception as e:
    print(f"Error loading model. Ensure you have internet for first-time download: {e}")
    exit()

# --- Step 2: Define Persona and Job-to-be-Done (The Query) ---
persona_description = input("Enter persona description: ")
job_to_be_done = input("Enter job-to-be-done: ")
query_text = f"Persona: {persona_description}. Job to be done: {job_to_be_done}"
print(f"\nSemantic Query: '{query_text}'")

# --- Step 3: Extract Headings from PDFs ---
def extract_headings_from_pdf(pdf_path):
    headings = []
    try:
        reader = PdfReader(pdf_path)
        for page in reader.pages:
            text = page.extract_text()
            if text:
                lines = text.split('\n')
                for i, line in enumerate(lines):
                    line = line.strip()
                    if 5 < len(line) < 100:
                        if line.isupper() or line.istitle() or (line[0].isdigit() and '.' in line[:3]):
                            headings.append(line)
        return list(set(headings))
    except Exception as e:
        print(f"Error processing PDF {pdf_path}: {e}")
        return []

all_extracted_headings = []
pdf_files = [f for f in os.listdir(PDF_DIRECTORY) if f.lower().endswith('.pdf')]

if not pdf_files:
    print(f"No PDF files found in {PDF_DIRECTORY}. Please check the path.")
    exit()
else:
    print(f"\nProcessing {len(pdf_files)} PDF files...")
    for pdf_file in pdf_files:
        pdf_path = os.path.join(PDF_DIRECTORY, pdf_file)
        print(f"  Extracting headings from {pdf_file}...")
        headings = extract_headings_from_pdf(pdf_path)
        all_extracted_headings.extend(headings)
    all_extracted_headings = list(set(all_extracted_headings))
    print(f"Extracted {len(all_extracted_headings)} unique potential headings.")

if not all_extracted_headings:
    print("No headings could be extracted. Exiting.")
    exit()

# --- Step 4: Generate Embeddings ---
print("\nGenerating embeddings for query and headings...")
query_embedding = embedding_model.encode([query_text])[0]
heading_embeddings = embedding_model.encode(all_extracted_headings, show_progress_bar=True)

# --- Step 5: Calculate Cosine Similarity and Rank ---
print("\nCalculating similarities and ranking headings...")
similarities = cosine_similarity([query_embedding], heading_embeddings)[0]
scored_headings = list(zip(all_extracted_headings, similarities))
scored_headings.sort(key=lambda x: x[1], reverse=True)

# --- Step 6: Return Most Relevant Headings ---
print(f"\n--- Top {OUTPUT_TOP_N_HEADINGS} Most Relevant Headings ---")
for i, (heading, score) in enumerate(scored_headings[:OUTPUT_TOP_N_HEADINGS]):
    print(f"{i+1}. Heading: '{heading}' (Similarity: {score:.4f})")

print("\nProgram finished.") 
