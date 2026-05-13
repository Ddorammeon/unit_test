# User Service API Test Script
# Tests all CRUD operations for user-service

$baseUrl = "http://localhost:8085/user-service"
$headers = @{
    "Content-Type" = "application/json"
}

# Store created IDs
$userId = $null
$patientId = $null
$doctorId = $null
$doctorUserId = $null
$pharmacistId = $null
$pharmacistUserId = $null
$labTechId = $null
$labTechUserId = $null
$adminId = $null
$adminUserId = $null

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "   USER SERVICE API COMPREHENSIVE TEST SUITE   " -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# ==================================================
# 1. USER TESTS
# ==================================================
Write-Host "`n[SECTION 1] USER CRUD TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 1.1: Create User
Write-Host "`n[TEST 1.1] POST /users - Create User" -ForegroundColor Cyan
$userBody = @{
    username = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    fullName = "Test User Auto"
    phone = "0901234567"
    hashedPassword = '$2a$10$abcdefghijklmnopqrstuvwxyz'
    isActive = $true
    roleNames = @("PATIENT")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $userBody -ContentType "application/json" -ErrorAction Stop
    $userId = $response.id
    Write-Host "SUCCESS: Created User ID = $userId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 1.2: Get User by ID
Write-Host "`n[TEST 1.2] GET /users/{id}" -ForegroundColor Cyan
if ($userId) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/users/$userId" -Method Get -ErrorAction Stop
        Write-Host "SUCCESS: Found User '$($response.username)'" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: User ID not available" -ForegroundColor DarkYellow
}

# Test 1.3: Update User
Write-Host "`n[TEST 1.3] PUT /users/{id}" -ForegroundColor Cyan
if ($userId) {
    $updateBody = @{
        email = "updated_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
        fullName = "Updated Test User"
        phone = "0987654321"
        isActive = $true
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/users/$userId" -Method Put -Body $updateBody -ContentType "application/json" -ErrorAction Stop
        Write-Host "SUCCESS: User updated to '$($response.fullName)'" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: User ID not available" -ForegroundColor DarkYellow
}

# Test 1.4: Get All Users
Write-Host "`n[TEST 1.4] GET /users" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) users" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# 2. PATIENT TESTS
# ==================================================
Write-Host "`n[SECTION 2] PATIENT CRUD TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 2.1: Create Patient
Write-Host "`n[TEST 2.1] POST /patients" -ForegroundColor Cyan
if ($userId) {
    $patientBody = @{
        userId = $userId
        dob = "1990-01-15"
        gender = "Male"
        address = "123 Test Street, Hanoi"
        bloodType = "O+"
        allergy = "Penicillin"
        insuranceNumber = "INS$(Get-Date -Format 'yyyyMMddHHmmss')"
        fullName = "Patient Test User"
        phone = "0901111111"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/patients" -Method Post -Body $patientBody -ContentType "application/json" -ErrorAction Stop
        $patientId = $response.id
        Write-Host "SUCCESS: Created Patient ID = $patientId" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: User ID not available" -ForegroundColor DarkYellow
}

Start-Sleep -Seconds 1

# Test 2.2: Get Patient by ID
Write-Host "`n[TEST 2.2] GET /patients/{id}" -ForegroundColor Cyan
if ($patientId) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/patients/$patientId" -Method Get -ErrorAction Stop
        Write-Host "SUCCESS: Found Patient '$($response.fullName)'" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Patient ID not available" -ForegroundColor DarkYellow
}

# Test 2.3: Update Patient
Write-Host "`n[TEST 2.3] PUT /patients/{id}" -ForegroundColor Cyan
if ($patientId) {
    $updateBody = @{
        address = "456 Updated Street, Hanoi"
        allergy = "Penicillin, Aspirin"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/patients/$patientId" -Method Put -Body $updateBody -ContentType "application/json" -ErrorAction Stop
        Write-Host "SUCCESS: Patient updated" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Patient ID not available" -ForegroundColor DarkYellow
}

# Test 2.4: Get All Patients
Write-Host "`n[TEST 2.4] GET /patients" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/patients" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) patients" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# 3. DOCTOR TESTS
# ==================================================
Write-Host "`n[SECTION 3] DOCTOR CRUD TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 3.1: Create Doctor User
Write-Host "`n[TEST 3.1] POST /users - Create Doctor User" -ForegroundColor Cyan
$doctorUserBody = @{
    username = "doctor_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "doctor_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    fullName = "Dr. Test Doctor"
    phone = "0902222222"
    hashedPassword = '$2a$10$abcdefghijklmnopqrstuvwxyz'
    isActive = $true
    roleNames = @("DOCTOR")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $doctorUserBody -ContentType "application/json" -ErrorAction Stop
    $doctorUserId = $response.id
    Write-Host "SUCCESS: Created Doctor User ID = $doctorUserId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 3.2: Create Doctor Profile
Write-Host "`n[TEST 3.2] POST /doctors" -ForegroundColor Cyan
if ($doctorUserId) {
    $doctorBody = @{
        userId = $doctorUserId
        specializationCode = "CARDIOLOGY"
        workingHospital = "Vietnam National Heart Institute"
        licenseNumber = "DOC$(Get-Date -Format 'yyyyMMddHHmmss')"
        consultationFeeAmount = 500000
        degrees = @(
            @{
                degreeName = "Doctor of Medicine"
                institution = "Hanoi Medical University"
                yearObtained = 2010
            }
        )
        fullName = "Dr. Test Doctor"
        phone = "0902222222"
    } | ConvertTo-Json -Depth 10
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/doctors" -Method Post -Body $doctorBody -ContentType "application/json" -ErrorAction Stop
        $doctorId = $response.id
        Write-Host "SUCCESS: Created Doctor ID = $doctorId" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Doctor User ID not available" -ForegroundColor DarkYellow
}

Start-Sleep -Seconds 1

# Test 3.3: Update Doctor
Write-Host "`n[TEST 3.3] PUT /doctors/{id}" -ForegroundColor Cyan
if ($doctorId) {
    $updateBody = @{
        specializationCode = "NEUROLOGY"
        consultationFeeAmount = 600000
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/doctors/$doctorId" -Method Put -Body $updateBody -ContentType "application/json" -ErrorAction Stop
        Write-Host "SUCCESS: Doctor updated to $($response.specializationCode)" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Doctor ID not available" -ForegroundColor DarkYellow
}

# Test 3.4: Get All Doctors
Write-Host "`n[TEST 3.4] GET /doctors/all" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/doctors/all" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) doctors" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# 4. PHARMACIST TESTS
# ==================================================
Write-Host "`n[SECTION 4] PHARMACIST CRUD TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 4.1: Create Pharmacist User
Write-Host "`n[TEST 4.1] POST /users - Create Pharmacist User" -ForegroundColor Cyan
$pharmacistUserBody = @{
    username = "pharmacist_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "pharmacist_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    fullName = "Pharmacist Test User"
    phone = "0904444444"
    hashedPassword = '$2a$10$abcdefghijklmnopqrstuvwxyz'
    isActive = $true
    roleNames = @("PHARMACIST")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $pharmacistUserBody -ContentType "application/json" -ErrorAction Stop
    $pharmacistUserId = $response.id
    Write-Host "SUCCESS: Created Pharmacist User ID = $pharmacistUserId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 4.2: Create Pharmacist Profile
Write-Host "`n[TEST 4.2] POST /pharmacists" -ForegroundColor Cyan
if ($pharmacistUserId) {
    $pharmacistBody = @{
        userId = $pharmacistUserId
        degree = "Bachelor of Pharmacy"
        certificate = "CERT-PH-$(Get-Date -Format 'yyyyMMddHHmmss')"
        fullName = "Pharmacist Test User"
        phone = "0904444444"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/pharmacists" -Method Post -Body $pharmacistBody -ContentType "application/json" -ErrorAction Stop
        $pharmacistId = $response.id
        Write-Host "SUCCESS: Created Pharmacist ID = $pharmacistId" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Pharmacist User ID not available" -ForegroundColor DarkYellow
}

# Test 4.3: Get All Pharmacists
Write-Host "`n[TEST 4.3] GET /pharmacists" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/pharmacists" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) pharmacists" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# 5. LAB TECHNICIAN TESTS
# ==================================================
Write-Host "`n[SECTION 5] LAB TECHNICIAN CRUD TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 5.1: Create Lab Tech User
Write-Host "`n[TEST 5.1] POST /users - Create Lab Tech User" -ForegroundColor Cyan
$labTechUserBody = @{
    username = "labtech_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "labtech_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    fullName = "Lab Tech Test User"
    phone = "0906666666"
    hashedPassword = '$2a$10$abcdefghijklmnopqrstuvwxyz'
    isActive = $true
    roleNames = @("LABTECHNICIAN")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $labTechUserBody -ContentType "application/json" -ErrorAction Stop
    $labTechUserId = $response.id
    Write-Host "SUCCESS: Created Lab Tech User ID = $labTechUserId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 5.2: Create Lab Tech Profile
Write-Host "`n[TEST 5.2] POST /lab-technicians" -ForegroundColor Cyan
if ($labTechUserId) {
    $labTechBody = @{
        userId = $labTechUserId
        field = "Clinical Chemistry"
        fullName = "Lab Tech Test User"
        phone = "0906666666"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/lab-technicians" -Method Post -Body $labTechBody -ContentType "application/json" -ErrorAction Stop
        $labTechId = $response.id
        Write-Host "SUCCESS: Created Lab Tech ID = $labTechId" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Lab Tech User ID not available" -ForegroundColor DarkYellow
}

# Test 5.3: Get All Lab Technicians
Write-Host "`n[TEST 5.3] GET /lab-technicians" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/lab-technicians" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) lab technicians" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# 6. ADMIN TESTS
# ==================================================
Write-Host "`n[SECTION 6] ADMIN TESTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Test 6.1: Create Admin User
Write-Host "`n[TEST 6.1] POST /users - Create Admin User" -ForegroundColor Cyan
$adminUserBody = @{
    username = "admin_$(Get-Date -Format 'yyyyMMddHHmmss')"
    email = "admin_$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    fullName = "Admin Test User"
    phone = "0908888888"
    hashedPassword = '$2a$10$abcdefghijklmnopqrstuvwxyz'
    isActive = $true
    roleNames = @("ADMIN")
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $adminUserBody -ContentType "application/json" -ErrorAction Stop
    $adminUserId = $response.id
    Write-Host "SUCCESS: Created Admin User ID = $adminUserId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 6.2: Create Admin Profile
Write-Host "`n[TEST 6.2] POST /admin/profiles?userId=xxx" -ForegroundColor Cyan
if ($adminUserId) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/admin/profiles?userId=$adminUserId" -Method Post -ContentType "application/json" -ErrorAction Stop
        $adminId = $response.id
        Write-Host "SUCCESS: Created Admin ID = $adminId" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIPPED: Admin User ID not available" -ForegroundColor DarkYellow
}

# Test 6.3: Get All Roles
Write-Host "`n[TEST 6.3] GET /admin/roles" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/admin/roles" -Method Get -ErrorAction Stop
    Write-Host "SUCCESS: Found $($response.Count) roles" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================================================
# CLEANUP
# ==================================================
Write-Host "`n[CLEANUP] Deleting Test Data" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# Delete Profiles
if ($labTechId) {
    Write-Host "Deleting Lab Technician..." -ForegroundColor DarkYellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/lab-technicians/$labTechId" -Method Delete -ErrorAction Stop | Out-Null
        Write-Host "SUCCESS: Deleted Lab Technician" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if ($pharmacistId) {
    Write-Host "Deleting Pharmacist..." -ForegroundColor DarkYellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/pharmacists/$pharmacistId" -Method Delete -ErrorAction Stop | Out-Null
        Write-Host "SUCCESS: Deleted Pharmacist" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if ($doctorId) {
    Write-Host "Deleting Doctor..." -ForegroundColor DarkYellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/doctors/$doctorId" -Method Delete -ErrorAction Stop | Out-Null
        Write-Host "SUCCESS: Deleted Doctor" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if ($patientId) {
    Write-Host "Deleting Patient..." -ForegroundColor DarkYellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/patients/$patientId" -Method Delete -ErrorAction Stop | Out-Null
        Write-Host "SUCCESS: Deleted Patient" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if ($adminId) {
    Write-Host "Deleting Admin..." -ForegroundColor DarkYellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/admin/profiles/$adminId" -Method Delete -ErrorAction Stop | Out-Null
        Write-Host "SUCCESS: Deleted Admin" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Delete Users
$usersToDelete = @($userId, $doctorUserId, $pharmacistUserId, $labTechUserId, $adminUserId)
foreach ($uid in $usersToDelete) {
    if ($uid) {
        Write-Host "Deleting User $uid..." -ForegroundColor DarkYellow
        try {
            Invoke-RestMethod -Uri "$baseUrl/users/$uid" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "SUCCESS: Deleted User" -ForegroundColor Green
        } catch {
            Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# ==================================================
# SUMMARY
# ==================================================
Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "           TEST COMPLETED                      " -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

Write-Host "All API tests have been executed." -ForegroundColor Green
Write-Host "Check the output above for any failures.`n" -ForegroundColor Yellow
