# Deduplicate .properties files by keeping the first occurrence of each key

$files = Get-ChildItem "core/src/main/assets/messages" -Recurse -Filter "*_*.properties"

$totalCleaned = 0
$totalDupesRemoved = 0

foreach ($file in $files) {
    $lines = Get-Content $file.FullName -Encoding UTF8
    $seenKeys = @{}
    $newLines = [System.Collections.Generic.List[string]]::new()
    $fileDupes = 0

    foreach ($line in $lines) {
        if ($line -match "^\s*([^=#]+)=(.*)$") {
            $key = $matches[1].Trim()
            if ($seenKeys.ContainsKey($key)) {
                $fileDupes++
            } else {
                $seenKeys[$key] = $true
                $newLines.Add($line)
            }
        } else {
            # Preserve comments and empty lines
            $newLines.Add($line)
        }
    }

    if ($fileDupes -gt 0) {
        [System.IO.File]::WriteAllLines($file.FullName, $newLines, [System.Text.Encoding]::UTF8)
        Write-Host "Cleaned $($file.Name): Removed $fileDupes duplicate keys."
        $totalCleaned++
        $totalDupesRemoved += $fileDupes
    }
}

Write-Host "=================================================="
Write-Host "Deduplication Complete!"
Write-Host "Cleaned files: $totalCleaned"
Write-Host "Total duplicate keys removed: $totalDupesRemoved"
Write-Host "=================================================="
