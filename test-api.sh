
#!/bin/bash

echo "=== Adobe Challenge Backend API Testing ==="
echo ""

BASE_URL="http://localhost:8080"

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -X GET "$BASE_URL/api/analysis/health" | jq '.' 2>/dev/null || echo "Health check response received"
echo -e "\n"

# Test 2: Get Frontend Config
echo "2. Testing Frontend Configuration..."
curl -X GET "$BASE_URL/api/frontend/config" | jq '.' 2>/dev/null || echo "Config response received"
echo -e "\n"

# Test 3: Get Demo Data
echo "3. Testing Demo Data..."
curl -X GET "$BASE_URL/api/frontend/demo-data" | jq '.' 2>/dev/null || echo "Demo data response received"
echo -e "\n"

# Test 4: System Metrics
echo "4. Testing System Metrics..."
curl -X GET "$BASE_URL/api/analysis/metrics" | jq '.' 2>/dev/null || echo "Metrics response received"
echo -e "\n"

# Test 5: Adobe Requirements Validation
echo "5. Testing Adobe Requirements Validation..."
curl -X GET "$BASE_URL/api/compliance/validate" | jq '.' 2>/dev/null || echo "Validation response received"
echo -e "\n"

# Test 6: Test PDF Upload (you'll need to add actual PDF files)
echo "6. Testing PDF Upload (using sample data)..."
echo "Note: For actual PDF testing, use the following command with real PDF files:"
echo "curl -X POST -F 'files=@path/to/your.pdf' -F 'persona=student' -F 'jobToBeDone=analyze document' '$BASE_URL/api/adobe/analyze'"
echo -e "\n"

echo "=== Testing Complete ==="
