# 🔍 Challenge 1B: Semantic PDF Section Retrieval

This solution performs **semantic extraction of relevant sections** from collections of PDF documents, guided by a **persona** and a **job-to-be-done**. Designed for **offline execution**, it processes multiple test cases in batch, ranks section headings by semantic relevance, and returns structured **JSON outputs** for downstream applications.

---

## 📁 Project Structure

```
Challenge_1b/
├── app/
│   ├── challenge1b_multi_collection_analysis.py   # Main processor
│   ├── semantic_pdf_heading_search.py             # CLI-based heading search tool
│   ├── download_model.py                          # Optional: downloads model for offline
│   └── model/                                      # Pre-cached sentence-transformer model
│       ├── config.json
│       ├── model.safetensors
│       └── ...
├── test_cases/
│   ├── Collection 1/
│   ├── Collection 2/
│   ├── Collection 3/
│   └── ...
├── requirements.txt
├── Dockerfile
└── README.md
```

---

## 🧩 Input Format (`challenge1b_input.json`)

```json
{
  "challenge_info": {
    "challenge_id": "round_1b_XXX",
    "test_case_name": "sample_test"
  },
  "documents": [
    { "filename": "sample.pdf", "title": "Sample PDF Title" }
  ],
  "persona": { "role": "Persona Role" },
  "job_to_be_done": { "task": "Specific task to perform" }
}
```

---

## ✅ Output Format (`challenge1b_output.json`)

```json
{
  "metadata": {
    "test_cases_documents": ["sample.pdf"],
    "persona": "Persona Role",
    "job_to_be_done": "Task"
  },
  "extracted_sections": [
    {
      "document": "sample.pdf",
      "section_title": "Relevant Section Heading",
      "importance_rank": 1,
      "page_number": 2
    }
  ],
  "subsection_analysis": [
    {
      "document": "sample.pdf",
      "refined_text": "Relevant paragraph or snippet",
      "page_number": 2
    }
  ]
}
```

---

## 🚀 Key Features

- 🔎 **Semantic understanding** using Sentence-Transformers (`MiniLM-L6-v2`)
- 📚 **Multi-collection batch processing**
- 📄 **Robust heading extraction** using `PyPDF2` with contextual snippets
- 📦 **Structured JSON outputs** suitable for UI or downstream processing
- 🧱 **Offline-ready**, with pre-cached model support
- 🐳 **Dockerized deployment** for consistent execution

---

## 🛠️ How to Run

### ✅ Locally (Python 3.8+)

```bash
pip install -r requirements.txt
python app/challenge1b_multi_collection_analysis.py
```

### 🐳 Docker (Offline-Ready)

```bash
docker build -t challenge1b .
docker run -v ${PWD}/test_cases:/app/test_cases challenge1b
```

---

## 📦 Sample Collections

### 📂 Collection 1: Travel Planning

- 👤 Persona: Travel Planner  
- 🛠️ Task: Plan a 4-day trip to South of France  
- 📄 Documents: 7 travel guides  

### 📂 Collection 2: Adobe Acrobat Learning

- 👤 Persona: HR Professional  
- 🛠️ Task: Build onboarding forms  
- 📄 Documents: 15 Acrobat guides  

### 📂 Collection 3: Recipe Planning

- 👤 Persona: Food Contractor  
- 🛠️ Task: Design buffet-style vegetarian dinner  
- 📄 Documents: 9 recipe PDFs  

---

## 📎 Notes

- Ensure that `app/model/` contains the pre-cached Sentence Transformer model if running offline.
- Place PDFs in the `PDFs/` folder inside each collection.
- Logs will show any missing PDFs or input inconsistencies.

---

> Built with ❤️ for Adobe Hackathon Challenge 1B
