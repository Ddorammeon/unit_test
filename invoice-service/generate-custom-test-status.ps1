$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$testResultsDir = Join-Path $projectRoot "build\test-results\test"
$outputDir = Join-Path $projectRoot "build\reports\custom-test-status"
$htmlOutput = Join-Path $outputDir "custom-test-status.html"
$textOutput = Join-Path $outputDir "custom-test-status.txt"
$sourceTextOutput = Join-Path $projectRoot "src\test\resources\invoice-service-test-cases.txt"

function Resolve-FunctionName {
    param(
        [string]$ClassName,
        [string]$TestCaseId
    )

    if ($TestCaseId -like "INV-CLN-UT-*") {
        return "cleanupExpiredInvoices()"
    }

    $suffix = [int]($TestCaseId.Substring($TestCaseId.Length - 3))

    if ($suffix -le 5) { return "createInvoice()" }
    if ($suffix -le 10) { return "updateInvoice()" }
    if ($suffix -le 12) { return "markAsPaid()" }
    if ($suffix -le 14) { return "cancelInvoice()" }
    if ($suffix -le 16) { return "getInvoiceById()" }
    if ($suffix -le 18) { return "listInvoices()" }
    if ($suffix -le 20) { return "getInvoicesByAppointmentId()" }
    if ($suffix -le 23) { return "getInvoicesByPatientId()" }
    if ($suffix -le 26) { return "addMedicineCharges()" }
    if ($suffix -le 30) { return "applyInsuranceDiscount()" }
    if ($suffix -le 31) { return "revertInsuranceDiscount()" }
    if ($suffix -le 32) { return "removeMedicineCharges()" }
    if ($suffix -le 35) { return "updateStatus()" }
    if ($suffix -eq 36) { return "canAddMedicineCharges()" }
    if ($suffix -eq 37) { return "canApplyInsuranceDiscount()" }
    if ($suffix -le 39) { return "canCancel()" }
    if ($suffix -le 41) { return "canMarkAsPaid()" }
    if ($suffix -le 43) { return "addLabTestCharge()" }
    return "N/A"
}

if (-not (Test-Path $testResultsDir)) {
    throw "Test results directory not found: $testResultsDir"
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
$results = New-Object System.Collections.Generic.List[object]

Get-ChildItem -Path $testResultsDir -Filter "TEST-*.xml" | Sort-Object Name | ForEach-Object {
    [xml]$suite = Get-Content $_.FullName -Raw
    $testSuiteNode = $suite.testsuite
    if (-not $testSuiteNode) {
        $testSuiteNode = $suite.DocumentElement
    }

    $testCases = @($testSuiteNode.testcase)
    foreach ($testcase in $testCases) {
        if ($null -eq $testcase) {
            continue
        }
        if ($testcase.name -notlike "* - *") {
            continue
        }
        $status = "PASSED"
        $details = ""

        if ($testcase.failure) {
            $status = "FAILED"
            $details = ($testcase.failure.message | Select-Object -First 1)
        }
        elseif ($testcase.error) {
            $status = "FAILED"
            $details = ($testcase.error.message | Select-Object -First 1)
        }
        elseif ($testcase.skipped) {
            $status = "SKIPPED"
            $details = "Skipped"
        }

        $simpleClassName = ($testcase.classname -split '\.')[-1]
        $rawTestCaseId = (($testcase.name -split ' - ')[0]).Trim()
        $objective = (($testcase.name -split ' - ', 2)[1]).Trim()
        $functionName = Resolve-FunctionName -ClassName $simpleClassName -TestCaseId $rawTestCaseId
        $displayName = "$rawTestCaseId - $objective"
        $displayLine = "$simpleClassName > [Function: $functionName] > $displayName $status"

        $results.Add([PSCustomObject]@{
            ClassName = $simpleClassName
            DisplayName = $displayName
            TestCaseId = $rawTestCaseId
            FunctionName = $functionName
            Status = $status
            Duration = [double]$testcase.time
            Details = $details
            ConsoleLine = $displayLine
        })
    }
}

$summary = @{
    Total = $results.Count
    Passed = ($results | Where-Object { $_.Status -eq "PASSED" }).Count
    Failed = ($results | Where-Object { $_.Status -eq "FAILED" }).Count
    Skipped = ($results | Where-Object { $_.Status -eq "SKIPPED" }).Count
}

$sortedLines = $results | Sort-Object ClassName, DisplayName | ForEach-Object { $_.ConsoleLine }
$sourceDir = Split-Path -Parent $sourceTextOutput
New-Item -ItemType Directory -Force -Path $sourceDir | Out-Null
$sortedLines | Set-Content -Path $textOutput
$sortedLines | Set-Content -Path $sourceTextOutput

$rows = $results | Sort-Object ClassName, DisplayName | ForEach-Object {
    $statusClass = switch ($_.Status) {
        "PASSED" { "passed" }
        "FAILED" { "failed" }
        default { "skipped" }
    }
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
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Invoice Service Custom Test Status Report</title>
<style>
:root { --bg:#f4f1ea; --panel:#fffdf8; --text:#1f2937; --muted:#6b7280; --line:#ddd6c7; --pass:#166534; --pass-bg:#dcfce7; --fail:#991b1b; --fail-bg:#fee2e2; --skip:#92400e; --skip-bg:#fef3c7; }
* { box-sizing:border-box; } body { margin:0; font-family:"Segoe UI", Arial, sans-serif; background:linear-gradient(180deg, #efe9dc 0%, var(--bg) 100%); color:var(--text); }
.wrap { max-width:1280px; margin:0 auto; padding:32px 20px 48px; } .hero,.list-card,.table-card { background:var(--panel); border:1px solid var(--line); border-radius:20px; padding:24px; box-shadow:0 12px 30px rgba(31,41,55,.08); margin-bottom:24px; }
h1 { margin:0 0 8px; font-size:32px; } .sub,.foot { color:var(--muted); } .stats { display:grid; grid-template-columns:repeat(4,minmax(120px,1fr)); gap:12px; } .stat { padding:16px; border-radius:16px; border:1px solid var(--line); background:#faf7f1; } .stat b { display:block; font-size:26px; margin-bottom:4px; }
pre { margin:0; white-space:pre-wrap; word-break:break-word; font-family:Consolas,"Courier New",monospace; line-height:1.7; font-size:14px; } table { width:100%; border-collapse:collapse; font-size:14px; } th,td { text-align:left; vertical-align:top; border-bottom:1px solid var(--line); padding:12px 10px; } th { color:var(--muted); font-size:12px; text-transform:uppercase; letter-spacing:.08em; } .badge { display:inline-block; padding:4px 10px; border-radius:999px; font-weight:700; font-size:12px; } .passed { color:var(--pass); background:var(--pass-bg); } .failed { color:var(--fail); background:var(--fail-bg); } .skipped { color:var(--skip); background:var(--skip-bg); }
</style></head><body><div class="wrap">
<section class="hero"><h1>Invoice Service Custom Test Status Report</h1><p class="sub">Direct PASS/FAIL view generated from JUnit XML results in <code>build/test-results/test</code>.</p>
<div class="stats"><div class="stat"><b>$($summary.Total)</b><span>Total</span></div><div class="stat"><b>$($summary.Passed)</b><span>Passed</span></div><div class="stat"><b>$($summary.Failed)</b><span>Failed</span></div><div class="stat"><b>$($summary.Skipped)</b><span>Skipped</span></div></div></section>
<section class="list-card"><h2>Console Style Output</h2><pre>$([System.Net.WebUtility]::HtmlEncode((Get-Content $textOutput -Raw)))</pre></section>
<section class="table-card"><h2>Per Test Status</h2><table><thead><tr><th>Class</th><th>Function</th><th>Test Case</th><th>Status</th><th>Duration</th><th>Details</th><th>Console Line</th></tr></thead><tbody>
$($rows -join "`n")
</tbody></table></section><p class="foot">Generated by <code>generate-custom-test-status.ps1</code>.</p></div></body></html>
"@

$html | Set-Content -Path $htmlOutput -Encoding UTF8
Write-Output "TEXT_REPORT=$textOutput"
Write-Output "HTML_REPORT=$htmlOutput"
