# Appointment Service API Test Script
# PowerShell script to exercise Appointment & Medical Service endpoints

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Add-Type -AssemblyName System.Web

$baseUrl = "http://localhost:8082"
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
            # ignore secondary errors
        }
    }
}

$medicalServiceId = $null
$appointmentId = $null
$doctorId = "99999999-0000-0000-0000-000000000001"
$patientId = "11111111-0000-0000-0000-000000000001"
$slotStart = (Get-Date).ToUniversalTime().AddHours(2)
$slotEnd = $slotStart.AddMinutes(45)
$slotStartStr = $slotStart.ToString("o")
$slotEndStr = $slotEnd.ToString("o")

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Appointment Service API Test Script" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 1. MEDICAL SERVICE APIs
# ============================================
Write-Host "1. Testing Medical Service APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

# 1.1 Create Medical Service
Write-Host "1.1 Creating Medical Service..." -ForegroundColor Green
$medicalServiceBody = @{
    serviceName = "Khám Tim mạch chuyên sâu"
    serviceType = "Consultation"
    serviceTime = 45
    price = 350000
    status = "ACTIVE"
} | ConvertTo-Json -Depth 3

try {
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($medicalServiceBody)
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
    $medicalServiceId = $response.id
    Write-Host "   [OK] Created Medical Service with ID: $medicalServiceId" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.2 Get All Medical Services
Write-Host "1.2 Getting All Medical Services..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) medical services" -ForegroundColor Green
    if (-not $medicalServiceId -and $response.Count -gt 0) {
        $medicalServiceId = $response[0].id
        Write-Host "   [INFO] Using medical service ID: $medicalServiceId" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.3 Get Medical Service By ID
if ($medicalServiceId) {
    Write-Host "1.3 Getting Medical Service By ID..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services/$medicalServiceId" -Method Get -Headers $headers
        Write-Host "   [OK] Retrieved Medical Service: $($response.serviceName)" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# 1.4 Search Medical Services
Write-Host "1.4 Searching Medical Services By Keyword..." -ForegroundColor Green
try {
    $keyword = [System.Web.HttpUtility]::UrlEncode("khám")
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services/search?keyword=$keyword" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) medical services with keyword" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.5 Count By Type
Write-Host "1.5 Counting Medical Services By Type..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services/count/type/Consultation" -Method Get -Headers $headers
    Write-Host "   [OK] Consultation services: $response" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 1.6 Update Medical Service
if ($medicalServiceId) {
    Write-Host "1.6 Updating Medical Service..." -ForegroundColor Green
    $updateBody = @{
        serviceName = "Khám Tim mạch chuyên sâu (Updated)"
        serviceType = "Consultation"
        serviceTime = 50
        price = 400000
        status = "ACTIVE"
    } | ConvertTo-Json -Depth 3

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($updateBody)
        Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services/$medicalServiceId" -Method Put -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "   [OK] Updated Medical Service" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 2. SLOT HOLD & APPOINTMENT APIs
# ============================================
Write-Host "2. Testing Appointment APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

# 2.1 Hold Slot
Write-Host "2.1 Holding Slot..." -ForegroundColor Green
$medicalServiceList = if ($medicalServiceId) { @($medicalServiceId) } else { @() }
$holdSlotBody = @{
    doctorId = $doctorId
    patientId = $patientId
    appointmentStartTime = $slotStartStr
    appointmentEndTime = $slotEndStr
    medicalServiceIds = [System.Collections.Generic.List[string]]$medicalServiceList
} | ConvertTo-Json -Depth 5

try {
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($holdSlotBody)
    Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/slots/hold" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
    Write-Host "   [OK] Slot locked successfully" -ForegroundColor Green
} catch {
    Write-Host "   [WARN] Failed to hold slot (continuing anyway): $($_.Exception.Message)" -ForegroundColor Yellow
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.2 Create Appointment
Write-Host "2.2 Creating Appointment..." -ForegroundColor Green
if ($medicalServiceId) {
    $appointmentBody = @{
        doctorId = $doctorId
        patientId = $patientId
        appointmentStartTime = $slotStartStr
        appointmentEndTime = $slotEndStr
        status = "CHECKED"
        medicalServiceIds = [System.Collections.Generic.List[string]]@($medicalServiceId)
    } | ConvertTo-Json -Depth 5

    try {
        $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($appointmentBody)
        $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments" -Method Post -Headers $headers -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        $appointmentId = $response.id
        Write-Host "   [OK] Created Appointment with ID: $appointmentId" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
} else {
    Write-Host "   [SKIP] Need a medical service ID to create appointment" -ForegroundColor Yellow
}
Write-Host ""

# 2.3 Get All Appointments
Write-Host "2.3 Getting All Appointments..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) appointments" -ForegroundColor Green
    if (-not $appointmentId -and $response.Count -gt 0) {
        $appointmentId = $response[0].id
        Write-Host "   [INFO] Using appointment ID: $appointmentId" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.4 Get Appointment By ID
if ($appointmentId) {
    Write-Host "2.4 Getting Appointment By ID..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/$appointmentId" -Method Get -Headers $headers
        Write-Host "   [OK] Retrieved Appointment status: $($response.status)" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# 2.5 Get Appointments By Doctor
Write-Host "2.5 Getting Appointments By Doctor..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/doctor/$doctorId" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) appointments for doctor" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.6 Get Appointments By Patient
Write-Host "2.6 Getting Appointments By Patient..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/patient/$patientId" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) appointments for patient" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.7 Get Appointments By Status
Write-Host "2.7 Getting Appointments By Status..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/status/CONFIRMED" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) CONFIRMED appointments" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.8 Get Appointments By Range
Write-Host "2.8 Getting Appointments By Range..." -ForegroundColor Green
$rangeStart = (Get-Date).AddHours(-1).ToUniversalTime().ToString("o")
$rangeEnd = (Get-Date).AddDays(3).ToUniversalTime().ToString("o")
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/range?start=$([System.Web.HttpUtility]::UrlEncode($rangeStart))&end=$([System.Web.HttpUtility]::UrlEncode($rangeEnd))" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) appointments in range" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.9 Get Appointments By Date
Write-Host "2.9 Getting Appointments By Date..." -ForegroundColor Green
$dateStr = (Get-Date).ToString("dd/MM/yyyy")
try {
    $encodedDate = [System.Web.HttpUtility]::UrlEncode($dateStr)
    $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/date?date=$encodedDate" -Method Get -Headers $headers
    Write-Host "   [OK] Found $($response.Count) appointments on $dateStr" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Show-ErrorDetail $_.Exception
}
Write-Host ""

# 2.10 Update Appointment Status
if ($appointmentId) {
    Write-Host "2.10 Updating Appointment Status..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/$appointmentId/status?status=PROGRESSING" -Method Patch -Headers $headers
        Write-Host "   [OK] Updated status to PROGRESSING" -ForegroundColor Green
    } catch {
        Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
        Show-ErrorDetail $_.Exception
    }
    Write-Host ""
}

# ============================================
# 3. CLEANUP (Optional)
# ============================================
Write-Host "3. Cleanup (Optional)..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

# Uncomment to delete created entities
# if ($appointmentId) {
#     try {
#         Invoke-RestMethod -Uri "$baseUrl/appointment-service/appointments/$appointmentId" -Method Delete -Headers $headers
#         Write-Host "   [OK] Marked appointment as CANCELLED" -ForegroundColor Green
#     } catch {
#         Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
#         Show-ErrorDetail $_.Exception
#     }
# }
#
# if ($medicalServiceId) {
#     try {
#         Invoke-RestMethod -Uri "$baseUrl/appointment-service/medical-services/$medicalServiceId" -Method Delete -Headers $headers
#         Write-Host "   [OK] Deleted medical service" -ForegroundColor Green
#     } catch {
#         Write-Host "   [ERROR] $($_.Exception.Message)" -ForegroundColor Red
#         Show-ErrorDetail $_.Exception
#     }
# }

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Test Script Completed!" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan


