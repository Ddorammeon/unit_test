# Doctor Service API Test Script
# PowerShell script to exercise Doctor Service endpoints end-to-end

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Add-Type -AssemblyName System.Web

$baseUrl = "http://localhost:8090"
$token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwOTgxMDcyNzczIiwiZnVsbF9uYW1lIjoiTmd1eWVuIFZhbiBTb24iLCJ1c2VyX2lkIjoiNTJkZGQ1NGQtNGM2My00NzE5LWJiZDAtZGFhNDE5N2NjNDMyIiwicGhvbmUiOiIwOTgxMDcyNzczIiwicHJpbWFyeV9yb2xlIjoiUEFUSUVOVCIsInJvbGVzIjpbIlBBVElFTlQiXSwiaXNzIjoiZGV2IiwiZXhwIjoxNzY0MTMzMDQ5LCJpYXQiOjE3NjQwNDY2NDksImp0aSI6ImJiOGQ0MThjLWE5YWQtNGFjMi05YjA2LTI2NTE2YzRlZDdjMiJ9.bz3wT7wQVapP-ZSWvEF7A8KMCaL0N0LupvvIPZW41H-D8MtRilDzDC9yU-NK4gQ-GosTIxdXErOqLbXScYUuLbihhlibla3Cn8RCYC6Et-teg1L6ImuhBYrGpC0FLYGGCd3qHJRnHlaIwl1Yx5kCZLK9DQxu35fU7_slkhzgF5QUlAlunTC94Z8cGo5H0Tw4hF5L86ReD_YT9BY5biQeqTLoHsEQg-4xToNR87izwdskUNnnxpe6SNhKEWv9sbJftqjH62vb3hs0l8mWr_61GkWWtE2Cq7jyauiGt1XVpVgrdsn-IJwPdwK9r5wMSqNR1a192sd6MwxHLsGq89DtMg"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

function Show-ErrorDetail {
    param([System.Exception]$ex)
    if ($ex -and $ex.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($ex.Response.GetResponseStream())
            $body = $reader.ReadToEnd()
            if ($body) {
                Write-Host "   [DETAIL] $body" -ForegroundColor Yellow
            }
        } catch {
            # ignore parsing issues
        }
    }
}

$doctorId = [Guid]::NewGuid().ToString()
$workScheduleId = $null
$doctorWorkScheduleId = $null
$doctorDegreeId = $null

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Doctor Service API Test Script" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 1. DOCTOR APIs
# ============================================
Write-Host "1. Testing Doctor APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

# 1.1 Create Doctor
Write-Host "1.1 Creating Doctor..." -ForegroundColor Green
$doctorBody = @{
    userId = $doctorId
    specializationCode = "CARDIO"
    workingHospital = "108 Military Central Hospital"
    licenseNumber = "MOH-2024-0001"
    consultationFeeAmount = 500000
} | ConvertTo-Json -Depth 3

try {
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($doctorBody)
    $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctors" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
    $doctorId = $response.userId
    Write-Host "   [OK] Created Doctor with ID: $doctorId" -ForegroundColor Green
    Write-Host "   Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.2 Get All Doctors
Write-Host "1.2 Getting All Doctors..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctors" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) doctors" -ForegroundColor Green
    if (-not $doctorId -and $response.Count -gt 0) {
        $doctorId = $response[0].userId
    }
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.3 Get Doctor By ID
if ($doctorId) {
    Write-Host "1.3 Getting Doctor By ID..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctors/$doctorId" -Method Get -Headers $headers
        Write-Host "   [OK] Retrieved Doctor with license: $($response.licenseNumber)" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# 1.4 Update Doctor
if ($doctorId) {
    Write-Host "1.4 Updating Doctor..." -ForegroundColor Green
    $updateBody = @{
        userId = $doctorId
        specializationCode = "CARDIO_SURGEON"
        workingHospital = "Cho Ray Hospital"
        licenseNumber = "MOH-2024-0001-REV"
        consultationFeeAmount = 650000
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($updateBody)
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctors/$doctorId" -Method Put -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "   [OK] Updated Doctor" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 2. WORK SCHEDULE APIs
# ============================================
Write-Host "2. Testing Work Schedule APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

$workDate = (Get-Date).Date
$startTime = [DateTimeOffset]::UtcNow.AddDays(1).AddHours(9)
$endTime = $startTime.AddHours(2)

# 2.1 Create Work Schedule
Write-Host "2.1 Creating Work Schedule..." -ForegroundColor Green
$workScheduleBody = @{
    workDate = $workDate.ToString("yyyy-MM-dd")
    startTime = $startTime.ToString("o")
    endTime = $endTime.ToString("o")
} | ConvertTo-Json -Depth 3

try {
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($workScheduleBody)
    $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/work-schedules" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
    $workScheduleId = $response.id
    Write-Host "   [OK] Created Work Schedule: $workScheduleId" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.2 Get All Work Schedules
Write-Host "2.2 Getting All Work Schedules..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/work-schedules" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) work schedules" -ForegroundColor Green
    if (-not $workScheduleId -and $response.Count -gt 0) {
        $workScheduleId = $response[0].id
    }
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.3 Update Work Schedule
if ($workScheduleId) {
    Write-Host "2.3 Updating Work Schedule..." -ForegroundColor Green
    $updateScheduleBody = @{
        workDate = $workDate.AddDays(1).ToString("yyyy-MM-dd")
        startTime = $startTime.AddDays(1).ToString("o")
        endTime = $endTime.AddDays(1).ToString("o")
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($updateScheduleBody)
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/work-schedules/$workScheduleId" -Method Put -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "   [OK] Updated Work Schedule" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 3. DOCTOR WORK SCHEDULE APIs
# ============================================
Write-Host "3. Testing Doctor Work Schedule APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

if ($doctorId -and $workScheduleId) {
    # 3.1 Create Doctor Work Schedule
    Write-Host "3.1 Creating Doctor Work Schedule..." -ForegroundColor Green
    $doctorWorkScheduleBody = @{
        doctorId = $doctorId
        workScheduleId = $workScheduleId
        status = "AVAILABLE"
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($doctorWorkScheduleBody)
        $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-work-schedules" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        $doctorWorkScheduleId = $response.id
        Write-Host "   [OK] Created Doctor Work Schedule: $doctorWorkScheduleId" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
} else {
    Write-Host "   [SKIP] Need doctor and work schedule IDs to continue" -ForegroundColor Yellow
}

# 3.2 List Doctor Work Schedules
Write-Host "3.2 Getting Doctor Work Schedules..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-work-schedules" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) doctor work schedules" -ForegroundColor Green
    if (-not $doctorWorkScheduleId -and $response.Count -gt 0) {
        $doctorWorkScheduleId = $response[0].id
    }
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 3.3 Get By Doctor
if ($doctorId) {
    Write-Host "3.3 Getting Doctor Work Schedules By Doctor..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-work-schedules/doctor/$doctorId" -Method Get -Headers $headers
        Write-Host "   [OK] Found $($response.Count) schedules for doctor $doctorId" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# 3.4 Update Doctor Work Schedule
if ($doctorWorkScheduleId -and $doctorId -and $workScheduleId) {
    Write-Host "3.4 Updating Doctor Work Schedule..." -ForegroundColor Green
    $updateDoctorWorkScheduleBody = @{
        doctorId = $doctorId
        workScheduleId = $workScheduleId
        status = "BOOKED"
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($updateDoctorWorkScheduleBody)
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-work-schedules/$doctorWorkScheduleId" -Method Put -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "   [OK] Updated Doctor Work Schedule" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 4. DOCTOR DEGREE APIs
# ============================================
Write-Host "4. Testing Doctor Degree APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

if ($doctorId) {
    # 4.1 Create Doctor Degree
    Write-Host "4.1 Creating Doctor Degree..." -ForegroundColor Green
    $doctorDegreeBody = @{
        doctorId = $doctorId
        degreeName = "MD Internal Medicine"
        institution = "University of Medicine and Pharmacy HCMC"
        yearObtained = 2017
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($doctorDegreeBody)
        $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-degrees" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        $doctorDegreeId = $response.id
        Write-Host "   [OK] Created Doctor Degree: $doctorDegreeId" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
} else {
    Write-Host "   [SKIP] Need doctor ID before creating degrees" -ForegroundColor Yellow
}

# 4.2 Get Degrees By Doctor
if ($doctorId) {
    Write-Host "4.2 Getting Degrees By Doctor..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-degrees/doctor/$doctorId" -Method Get -Headers $headers
        Write-Host "   [OK] Found $($response.Count) degrees for doctor $doctorId" -ForegroundColor Green
        if (-not $doctorDegreeId -and $response.Count -gt 0) {
            $doctorDegreeId = $response[0].id
        }
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# 4.3 Update Doctor Degree
if ($doctorDegreeId -and $doctorId) {
    Write-Host "4.3 Updating Doctor Degree..." -ForegroundColor Green
    $updateDegreeBody = @{
        doctorId = $doctorId
        degreeName = "MD Cardiology"
        institution = "University of Medicine and Pharmacy HCMC"
        yearObtained = 2019
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($updateDegreeBody)
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-degrees/$doctorDegreeId" -Method Put -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "   [OK] Updated Doctor Degree" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 5. CLEANUP (optional)
# ============================================
Write-Host "5. Cleanup..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

if ($doctorDegreeId) {
    Write-Host "5.1 Deleting Doctor Degree..." -ForegroundColor Green
    try {
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-degrees/$doctorDegreeId" -Method Delete -Headers $headers
        Write-Host "   [OK] Deleted Doctor Degree" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
}

if ($doctorWorkScheduleId) {
    Write-Host "5.2 Deleting Doctor Work Schedule..." -ForegroundColor Green
    try {
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctor-work-schedules/$doctorWorkScheduleId" -Method Delete -Headers $headers
        Write-Host "   [OK] Deleted Doctor Work Schedule" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
}

if ($workScheduleId) {
    Write-Host "5.3 Deleting Work Schedule..." -ForegroundColor Green
    try {
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/work-schedules/$workScheduleId" -Method Delete -Headers $headers
        Write-Host "   [OK] Deleted Work Schedule" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
}

if ($doctorId) {
    Write-Host "5.4 Deleting Doctor..." -ForegroundColor Green
    try {
        Invoke-RestMethod -Uri "$baseUrl/doctor-service/doctors/$doctorId" -Method Delete -Headers $headers
        Write-Host "   [OK] Deleted Doctor" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
}

Write-Host ""
Write-Host "All Doctor Service API tests complete." -ForegroundColor Cyan

