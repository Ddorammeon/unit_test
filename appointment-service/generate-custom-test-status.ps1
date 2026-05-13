$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$testResultsDir = Join-Path $projectRoot "build\test-results\test"
$outputDir = Join-Path $projectRoot "build\reports\custom-test-status"
$htmlOutput = Join-Path $outputDir "custom-test-status.html"
$textOutput = Join-Path $outputDir "custom-test-status.txt"

$functionMap = @{
    "APP-SRV-UT-011"="createAppointment()"; "APP-SRV-UT-012"="createAppointment()"; "APP-SRV-UT-013"="createAppointment()"; "APP-SRV-UT-014"="createAppointment()"
    "APP-SRV-UT-015"="createAppointment()"; "APP-SRV-UT-016"="createAppointment()"; "APP-SRV-UT-017"="createAppointment()"; "APP-SRV-UT-018"="updateAppointment()"
    "APP-SRV-UT-019"="updateAppointment()"; "APP-SRV-UT-020"="updateAppointment()"; "APP-SRV-UT-021"="updateAppointment()"; "APP-SRV-UT-022"="updateAppointmentStatus()"
    "APP-SRV-UT-023"="startAppointment()"; "APP-SRV-UT-024"="startAppointment()"; "APP-SRV-UT-025"="startAppointment()"; "APP-SRV-UT-026"="deleteAppointment()"
    "APP-SRV-UT-027"="deleteAppointment()"; "APP-SRV-UT-028"="validateDoctor()"; "APP-SRV-UT-029"="validateDoctor()"; "APP-SRV-UT-030"="validatePatient()"; "APP-SRV-UT-031"="validateDoctor()"
    "MED-SRV-UT-008"="createMedicalService()"; "MED-SRV-UT-009"="updateMedicalService()"; "MED-SRV-UT-010"="updateMedicalService()"; "MED-SRV-UT-011"="deactivateMedicalServiceName()"; "MED-SRV-UT-012"="deactivateMedicalServiceName()"
    "MED-SRV-UT-013"="deactivateMedicalService()"; "MED-SRV-UT-014"="deactivateMedicalService()"; "MED-SRV-UT-015"="deactivateMedicalServiceType()"; "MED-SRV-UT-016"="deactivateMedicalServiceType()"
    "SLOT-SRV-UT-001"="getAvailableSlots()"; "SLOT-SRV-UT-002"="getAvailableSlots()"; "SLOT-SRV-UT-003"="lockSlot()"; "SLOT-SRV-UT-004"="lockSlot()"
    "SLOT-SRV-UT-005"="unlockSlot()"; "SLOT-SRV-UT-006"="validateAndUnlockSlot()"; "SLOT-SRV-UT-007"="validateAndUnlockSlot()"; "SLOT-SRV-UT-008"="validateAndUnlockSlot()"
    "SLOT-SRV-UT-009"="validateAndUnlockSlot()"
    "SLOT-SRV-UT-013"="calculateServiceDurationMinutes()"; "SLOT-SRV-UT-014"="calculateServiceDurationMinutes()"; "SLOT-SRV-UT-015"="calculateServiceDurationMinutes()"
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
$results = New-Object System.Collections.Generic.List[object]

Get-ChildItem -Path $testResultsDir -Filter "TEST-*.xml" | Sort-Object Name | ForEach-Object {
    [xml]$suite = Get-Content $_.FullName
    foreach ($testcase in $suite.testsuite.testcase) {
        $status = "PASSED"
        $details = ""
        if ($testcase.failure) { $status = "FAILED"; $details = ($testcase.failure.message | Select-Object -First 1) }
        elseif ($testcase.error) { $status = "FAILED"; $details = ($testcase.error.message | Select-Object -First 1) }
        elseif ($testcase.skipped) { $status = "SKIPPED"; $details = "Skipped" }

        $simpleClassName = ($testcase.classname -split '\.')[-1]
        $testCaseId = (($testcase.name -split ' - ')[0]).Trim()
        $functionName = if ($functionMap.ContainsKey($testCaseId)) { $functionMap[$testCaseId] } else { "N/A" }
        $displayLine = "$simpleClassName > [Function: $functionName] > $($testcase.name) $status"

        $results.Add([PSCustomObject]@{
            ClassName = $simpleClassName; DisplayName = $testcase.name; TestCaseId = $testCaseId; FunctionName = $functionName
            Status = $status; Duration = [double]$testcase.time; Details = $details; ConsoleLine = $displayLine
        })
    }
}

$summary = @{ Total=$results.Count; Passed=($results | ? { $_.Status -eq "PASSED" }).Count; Failed=($results | ? { $_.Status -eq "FAILED" }).Count; Skipped=($results | ? { $_.Status -eq "SKIPPED" }).Count }
$results | Sort-Object ClassName, DisplayName | % { $_.ConsoleLine } | Set-Content -Path $textOutput

$rows = $results | Sort-Object ClassName, DisplayName | ForEach-Object {
    $statusClass = switch ($_.Status) { "PASSED" { "passed" } "FAILED" { "failed" } default { "skipped" } }
    $safeDetails = [System.Net.WebUtility]::HtmlEncode($_.Details)
    $safeLine = [System.Net.WebUtility]::HtmlEncode($_.ConsoleLine)
@"
<tr>
  <td><code>$([System.Net.WebUtility]::HtmlEncode($_.ClassName))</code></td>
  <td><code>$([System.Net.WebUtility]::HtmlEncode($_.FunctionName))</code></td>
  <td>$([System.Net.WebUtility]::HtmlEncode($_.DisplayName))</td>
  <td><span class="badge $statusClass">$($_.Status)</span></td>
  <td>$("{0:N3}" -f $_.Duration)s</td>
  <td>$safeDetails</td>
  <td><code>$safeLine</code></td>
</tr>
"@
}

$html = @"
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Custom Test Status Report</title>
<style>
:root { --bg:#f4f1ea; --panel:#fffdf8; --text:#1f2937; --muted:#6b7280; --line:#ddd6c7; --pass:#166534; --pass-bg:#dcfce7; --fail:#991b1b; --fail-bg:#fee2e2; --skip:#92400e; --skip-bg:#fef3c7; }
* { box-sizing:border-box; } body { margin:0; font-family:"Segoe UI", Arial, sans-serif; background:linear-gradient(180deg, #efe9dc 0%, var(--bg) 100%); color:var(--text); }
.wrap { max-width:1280px; margin:0 auto; padding:32px 20px 48px; } .hero,.list-card,.table-card { background:var(--panel); border:1px solid var(--line); border-radius:20px; padding:24px; box-shadow:0 12px 30px rgba(31,41,55,.08); margin-bottom:24px; }
h1 { margin:0 0 8px; font-size:32px; } .sub,.foot { color:var(--muted); } .stats { display:grid; grid-template-columns:repeat(4,minmax(120px,1fr)); gap:12px; } .stat { padding:16px; border-radius:16px; border:1px solid var(--line); background:#faf7f1; } .stat b { display:block; font-size:26px; margin-bottom:4px; }
pre { margin:0; white-space:pre-wrap; word-break:break-word; font-family:Consolas,"Courier New",monospace; line-height:1.7; font-size:14px; } table { width:100%; border-collapse:collapse; font-size:14px; } th,td { text-align:left; vertical-align:top; border-bottom:1px solid var(--line); padding:12px 10px; } th { color:var(--muted); font-size:12px; text-transform:uppercase; letter-spacing:.08em; } .badge { display:inline-block; padding:4px 10px; border-radius:999px; font-weight:700; font-size:12px; } .passed { color:var(--pass); background:var(--pass-bg); } .failed { color:var(--fail); background:var(--fail-bg); } .skipped { color:var(--skip); background:var(--skip-bg); }
</style></head><body><div class="wrap">
<section class="hero"><h1>Custom Test Status Report</h1><p class="sub">Direct PASS/FAIL view generated from JUnit XML results in <code>build/test-results/test</code>.</p>
<div class="stats"><div class="stat"><b>$($summary.Total)</b><span>Total</span></div><div class="stat"><b>$($summary.Passed)</b><span>Passed</span></div><div class="stat"><b>$($summary.Failed)</b><span>Failed</span></div><div class="stat"><b>$($summary.Skipped)</b><span>Skipped</span></div></div></section>
<section class="list-card"><h2>Console Style Output</h2><pre>$([System.Net.WebUtility]::HtmlEncode((Get-Content $textOutput -Raw)))</pre></section>
<section class="table-card"><h2>Per Test Status</h2><table><thead><tr><th>Class</th><th>Function</th><th>Test Case</th><th>Status</th><th>Duration</th><th>Details</th><th>Console Line</th></tr></thead><tbody>
$($rows -join "`n")
</tbody></table></section><p class="foot">Generated by <code>generate-custom-test-status.ps1</code>.</p></div></body></html>
"@

$html | Set-Content -Path $htmlOutput -Encoding UTF8
Write-Output "TEXT_REPORT=$textOutput"
Write-Output "HTML_REPORT=$htmlOutput"
