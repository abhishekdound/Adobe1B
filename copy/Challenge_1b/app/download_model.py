from sentence_transformers import SentenceTransformer

# This will download the model and show where it is cached
model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
print("Model loaded successfully.")
print(f"Model path: {model.cache_folder}")
