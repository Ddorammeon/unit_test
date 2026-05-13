# Insurance Service API Test Script
$baseUrl = "http://localhost:8087"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Insurance Service APIs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Get all Insurance Policies
Write-Host "`n[TEST 1] GET /insurance-service/insurance-policies" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-policies" -Method Get
    Write-Host "✓ Success: Found $($response.Count) policies" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get specific Insurance Policy
Write-Host "`n[TEST 2] GET /insurance-service/insurance-policies/{id}" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-policies/a1a1a1a1-1111-4111-8111-111111111111" -Method Get
    Write-Host "✓ Success: Policy Number = $($response.policyNumber)" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create new Insurance Policy
Write-Host "`n[TEST 3] POST /insurance-service/insurance-policies" -ForegroundColor Yellow
$newPolicy = @{
    policyNumber = "TEST-POLICY-$(Get-Date -Format 'yyyyMMddHHmmss')"
    policyType = "Platinum"
    coverageAmount = 200000000
    deductible = 5000000
    startDate = "2025-12-01"
    endDate = "2026-12-01"
    status = "ACTIVE"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-policies" -Method Post -Body $newPolicy -ContentType "application/json"
    Write-Host "✓ Success: Created policy with ID = $($response.id)" -ForegroundColor Green
    $createdPolicyId = $response.id
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get all Patient Insurances
Write-Host "`n[TEST 4] GET /insurance-service/patient-insurances" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/patient-insurances" -Method Get
    Write-Host "✓ Success: Found $($response.Count) patient insurances" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get all Insurance Claims
Write-Host "`n[TEST 5] GET /api/insurance-claims" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-claims" -Method Get
    Write-Host "✓ Success: Found $($response.Count) claims" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Get specific Insurance Claim (with new fields)
Write-Host "`n[TEST 6] GET /api/insurance-claims/{id}" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-claims/d4d4d4d4-4444-4444-8444-444444444444" -Method Get
    Write-Host "✓ Success: Claim Status = $($response.status)" -ForegroundColor Green
    Write-Host "  - Total Claim Amount: $($response.totalClaimAmount)" -ForegroundColor Cyan
    Write-Host "  - Total Insurance Pay: $($response.totalInsurancePay)" -ForegroundColor Cyan
    Write-Host "  - Patient Pay Amount: $($response.patientPayAmount)" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Create Insurance Claim with new fields
Write-Host "`n[TEST 7] POST /api/insurance-claims (with new fields)" -ForegroundColor Yellow
$newClaim = @{
    status = "PENDING"
    claimAmount = 5000000
    approvedAmount = 0
    patientPayAmount = 1000000
    totalClaimAmount = 5000000
    totalInsurancePay = 4000000
    claimDate = "2025-11-21T10:00:00Z"
    notes = "Test claim with new fields"
    patientInsuranceId = "c3c3c3c3-3333-4333-8333-333333333333"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/insurance-claims" -Method Post -Body $newClaim -ContentType "application/json"
    Write-Host "✓ Success: Created claim with ID = $($response.id)" -ForegroundColor Green
    $createdClaimId = $response.id
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Get all BHYT Catalogue (NEW)
Write-Host "`n[TEST 8] GET /api/bhyt-catalogue" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/bhyt-catalogue" -Method Get
    Write-Host "✓ Success: Found $($response.Count) BHYT catalogue items" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: Get BHYT Catalogue by service code (NEW)
Write-Host "`n[TEST 9] GET /api/bhyt-catalogue/service-code/{code}" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/bhyt-catalogue/service-code/SV-RANG-001" -Method Get
    Write-Host "✓ Success: Service = $($response.serviceName)" -ForegroundColor Green
    Write-Host "  - Max Coverage: $($response.maxCoverageAmount)" -ForegroundColor Cyan
    Write-Host "  - Is Covered: $($response.isCovered)" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 10: Create BHYT Catalogue (NEW)
Write-Host "`n[TEST 10] POST /api/bhyt-catalogue" -ForegroundColor Yellow
$newBhyt = @{
    serviceCode = "SV-TEST-$(Get-Date -Format 'HHmmss')"
    serviceName = "Test Service"
    serviceType = "GENERAL"
    isCovered = $true
    maxCoverageAmount = 3000000
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/bhyt-catalogue" -Method Post -Body $newBhyt -ContentType "application/json"
    Write-Host "✓ Success: Created BHYT catalogue with ID = $($response.id)" -ForegroundColor Green
    $createdBhytId = $response.id
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 11: Get all Claim Items (NEW)
Write-Host "`n[TEST 11] GET /insurance-service/claim-items" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/claim-items" -Method Get
    Write-Host "✓ Success: Found $($response.Count) claim items" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 12: Get Claim Items by Claim ID (NEW)
Write-Host "`n[TEST 12] GET /insurance-service/claim-items/claim/{claimId}" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/claim-items/claim/d4d4d4d4-4444-4444-8444-444444444444" -Method Get
    Write-Host "✓ Success: Found $($response.Count) items for this claim" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 13: Create Claim Item (NEW)
Write-Host "`n[TEST 13] POST /insurance-service/claim-items" -ForegroundColor Yellow
$newClaimItem = @{
    quantity = 1
    unitPrice = 2000000
    totalAmount = 2000000
    insurancePayRatio = 0.8
    insurancePayAmount = 1600000
    patientPayAmount = 400000
    insuranceClaimId = "d4d4d4d4-4444-4444-8444-444444444444"
    bhytCatalogueId = "f5f5f5f5-5555-4555-8555-555555555555"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/claim-items" -Method Post -Body $newClaimItem -ContentType "application/json"
    Write-Host "✓ Success: Created claim item with ID = $($response.id)" -ForegroundColor Green
    $createdClaimItemId = $response.id
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 14: Get all Claim Documents
Write-Host "`n[TEST 14] GET /insurance-service/claim-documents" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/insurance-service/claim-documents" -Method Get
    Write-Host "✓ Success: Found $($response.Count) claim documents" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All insurance-service tests completed!" -ForegroundColor Green
Write-Host "Check the results above for any failures." -ForegroundColor Yellow