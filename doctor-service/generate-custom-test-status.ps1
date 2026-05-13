$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$testResultsDir = Join-Path $projectRoot "build\test-results\test"
$outputDir = Join-Path $projectRoot "build\reports\custom-test-status"
$htmlOutput = Join-Path $outputDir "custom-test-status.html"
$textOutput = Join-Path $outputDir "custom-test-status.txt"
$sourceTextOutput = Join-Path $projectRoot "src\test\resources\doctor-service-test-cases.txt"

$functionMap = @{
    "DOC-SRV-UT-001"  = "createDoctor()"
    "DOC-SRV-UT-002"  = "createDoctor()"
    "DOC-SRV-UT-003"  = "getAllDoctors()"
    "DOC-SRV-UT-004"  = "getDoctorById()"
    "DOC-SRV-UT-005"  = "getDoctorById()"
    "DOC-SRV-UT-006"  = "updateDoctor()"
    "DOC-SRV-UT-007"  = "updateDoctor()"
    "DOC-SRV-UT-008"  = "deleteDoctor()"
    "DOC-SRV-UT-009"  = "deleteDoctor()"
    "DOC-SRV-UT-010"  = "updateDoctor()"

    "DOC-DEG-UT-001"  = "createDoctorDegree()"
    "DOC-DEG-UT-002"  = "createDoctorDegree()"
    "DOC-DEG-UT-003"  = "getAllDoctorDegrees()"
    "DOC-DEG-UT-004"  = "getDoctorDegreeById()"
    "DOC-DEG-UT-005"  = "getDoctorDegreeById()"
    "DOC-DEG-UT-006"  = "getDegreesByDoctorId()"
    "DOC-DEG-UT-007"  = "updateDoctorDegree()"
    "DOC-DEG-UT-008"  = "updateDoctorDegree()"
    "DOC-DEG-UT-009"  = "deleteDoctorDegree()"
    "DOC-DEG-UT-010"  = "deleteDoctorDegree()"

    "DOC-WS-UT-001"   = "createDoctorWorkSchedule()"
    "DOC-WS-UT-002"   = "createDoctorWorkSchedule()"
    "DOC-WS-UT-003"   = "getAllDoctorWorkSchedules()"
    "DOC-WS-UT-004"   = "getDoctorWorkScheduleById()"
    "DOC-WS-UT-005"   = "getDoctorWorkScheduleById()"
    "DOC-WS-UT-006"   = "getDoctorWorkSchedulesByDoctorId()"
    "DOC-WS-UT-007"   = "updateDoctorWorkSchedule()"
    "DOC-WS-UT-008"   = "deleteDoctorWorkSchedule()"
    "DOC-WS-UT-009"   = "deleteDoctorWorkSchedule()"

    "WORK-SRV-UT-001" = "createWorkSchedule()"
    "WORK-SRV-UT-002" = "getAllWorkSchedules()"
    "WORK-SRV-UT-003" = "getWorkScheduleById()"
    "WORK-SRV-UT-004" = "getWorkScheduleById()"
    "WORK-SRV-UT-005" = "updateWorkSchedule()"
    "WORK-SRV-UT-006" = "updateWorkSchedule()"
    "WORK-SRV-UT-007" = "deleteWorkSchedule()"
    "WORK-SRV-UT-008" = "deleteWorkSchedule()"

    "DOC-ENT-UT-001"  = "addDegree()"
    "DOC-ENT-UT-002"  = "removeDegree()"
    "DOC-ENT-UT-003"  = "removeDegreeById()"
    "DOC-ENT-UT-004"  = "removeDegreeById()"
    "DOC-ENT-UT-005"  = "updateDegrees()"
    "DOC-ENT-UT-006"  = "updateDegrees()"
    "DOC-ENT-UT-007"  = "clearDegrees()"
    "DOC-ENT-UT-008"  = "updateBasicInfo()"
    "DOC-ENT-UT-009"  = "removeDegreeById()"
    "DOC-ENT-UT-010"  = "clearDegrees()"

    "MAP-UT-001"      = "toDoctorEntity()"
    "MAP-UT-002"      = "updateDoctorEntity()"
    "MAP-UT-003"      = "updateDoctorEntity()"
    "MAP-UT-004"      = "toDoctorResponse()"
    "MAP-UT-005"      = "toWorkScheduleEntity()"
    "MAP-UT-006"      = "updateWorkScheduleEntity()"
    "MAP-UT-007"      = "toDoctorWorkScheduleEntity()"
    "MAP-UT-008"      = "updateDoctorWorkScheduleEntity()"
    "MAP-UT-009"      = "null-safety mapping methods"
    "MAP-UT-010"      = "updateDoctorEntity()"

    "LIFE-UT-001"     = "DoctorDegree.onCreate()"
    "LIFE-UT-002"     = "WorkSchedule.onCreate()"
    "LIFE-UT-003"     = "DoctorWorkSchedule.onCreate()"
    "LIFE-UT-004"     = "DoctorWorkSchedule.onUpdate()"
}

if (-not (Test-Path $testResultsDir)) {
    throw "Test results directory not found: $testResultsDir"
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$results = New-Object System.Collections.Generic.List[object]

Get-ChildItem -Path $testResultsDir -Filter "TEST-*.xml" | Sort-Object Name | ForEach-Object {
    [xml]$suite = Get-Content $_.FullName

    foreach ($testcase in $suite.testsuite.testcase) {
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
        $testCaseId = (($testcase.name -split ' - ')[0]).Trim()
        $functionName = if ($functionMap.ContainsKey($testCaseId)) { $functionMap[$testCaseId] } else { "N/A" }
        $displayLine = "$simpleClassName > [Function: $functionName] > $($testcase.name) $status"

        $results.Add([PSCustomObject]@{
            ClassName    = $simpleClassName
            DisplayName  = $testcase.name
            TestCaseId   = $testCaseId
            FunctionName = $functionName
            Status       = $status
            Duration     = [double]$testcase.time
            Details      = $details
            ConsoleLine  = $displayLine
        })
    }
}

$summary = @{
    Total   = $results.Count
    Passed  = ($results | Where-Object { $_.Status -eq "PASSED" }).Count
    Failed  = ($results | Where-Object { $_.Status -eq "FAILED" }).Count
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
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Custom Test Status Report</title>
  <style>
    :root {
      --bg: #f4f1ea;
      --panel: #fffdf8;
      --text: #1f2937;
      --muted: #6b7280;
      --line: #ddd6c7;
      --pass: #166534;
      --pass-bg: #dcfce7;
      --fail: #991b1b;
      --fail-bg: #fee2e2;
      --skip: #92400e;
      --skip-bg: #fef3c7;
      --accent: #0f766e;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: "Segoe UI", Arial, sans-serif;
      background: linear-gradient(180deg, #efe9dc 0%, var(--bg) 100%);
      color: var(--text);
    }
    .wrap {
      max-width: 1280px;
      margin: 0 auto;
      padding: 32px 20px 48px;
    }
    .hero {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 20px;
      padding: 24px;
      box-shadow: 0 12px 30px rgba(31, 41, 55, 0.08);
      margin-bottom: 24px;
    }
    h1 {
      margin: 0 0 8px;
      font-size: 32px;
      line-height: 1.15;
    }
    .sub {
      color: var(--muted);
      margin: 0 0 20px;
    }
    .stats {
      display: grid;
      grid-template-columns: repeat(4, minmax(120px, 1fr));
      gap: 12px;
    }
    .stat {
      padding: 16px;
      border-radius: 16px;
      border: 1px solid var(--line);
      background: #faf7f1;
    }
    .stat b {
      display: block;
      font-size: 26px;
      margin-bottom: 4px;
    }
    .list-card, .table-card {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 20px;
      padding: 20px;
      box-shadow: 0 12px 30px rgba(31, 41, 55, 0.06);
      margin-bottom: 24px;
    }
    .list-card pre {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-word;
      font-family: Consolas, "Courier New", monospace;
      line-height: 1.7;
      font-size: 14px;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
    }
    th, td {
      text-align: left;
      vertical-align: top;
      border-bottom: 1px solid var(--line);
      padding: 12px 10px;
    }
    th {
      color: var(--muted);
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }
    .badge {
      display: inline-block;
      padding: 4px 10px;
      border-radius: 999px;
      font-weight: 700;
      font-size: 12px;
      letter-spacing: 0.04em;
    }
    .passed { color: var(--pass); background: var(--pass-bg); }
    .failed { color: var(--fail); background: var(--fail-bg); }
    .skipped { color: var(--skip); background: var(--skip-bg); }
    .foot {
      color: var(--muted);
      font-size: 13px;
    }
    @media (max-width: 800px) {
      .stats { grid-template-columns: repeat(2, minmax(120px, 1fr)); }
      table, thead, tbody, th, td, tr { display: block; }
      thead { display: none; }
      tr {
        border: 1px solid var(--line);
        border-radius: 14px;
        padding: 8px;
        margin-bottom: 12px;
      }
      td { border: 0; padding: 6px 4px; }
    }
  </style>
</head>
<body>
  <div class="wrap">
    <section class="hero">
      <h1>Custom Test Status Report</h1>
      <p class="sub">Direct PASS/FAIL view generated from JUnit XML results in <code>build/test-results/test</code>.</p>
      <div class="stats">
        <div class="stat"><b>$($summary.Total)</b><span>Total</span></div>
        <div class="stat"><b>$($summary.Passed)</b><span>Passed</span></div>
        <div class="stat"><b>$($summary.Failed)</b><span>Failed</span></div>
        <div class="stat"><b>$($summary.Skipped)</b><span>Skipped</span></div>
      </div>
    </section>

    <section class="list-card">
      <h2>Console Style Output</h2>
      <pre>$([System.Net.WebUtility]::HtmlEncode((Get-Content $textOutput -Raw)))</pre>
    </section>

    <section class="table-card">
      <h2>Per Test Status</h2>
      <table>
        <thead>
          <tr>
            <th>Class</th>
            <th>Function</th>
            <th>Test Case</th>
            <th>Status</th>
            <th>Duration</th>
            <th>Details</th>
            <th>Console Line</th>
          </tr>
        </thead>
        <tbody>
$($rows -join "`n")
        </tbody>
      </table>
    </section>

    <p class="foot">Generated by <code>generate-custom-test-status.ps1</code>.</p>
  </div>
</body>
</html>
"@

$html | Set-Content -Path $htmlOutput -Encoding UTF8

Write-Output "TEXT_REPORT=$textOutput"
Write-Output "HTML_REPORT=$htmlOutput"
