$baseDir = "D:\Desarollo\InfinityPixelDungeon\Infinite-Pixel-Dungeon\core\src\main\assets\messages"
$langCodes = @("be", "cs", "de", "el", "eo", "fr", "hu", "in", "it", "ja", "ko", "nl", "pl", "pt", "ru", "sv", "tr", "uk", "vi", "zh", "zh-hant")

$baseFiles = Get-ChildItem -Path $baseDir -Recurse -Filter "*.properties" | Where-Object { $_.Name -notmatch "_[a-z]{2}(-[a-z]+)?\.properties" }

foreach ($lang in $langCodes) {
    Write-Host "Verifying & syncing keys for language: $lang..."
    foreach ($baseFile in $baseFiles) {
        $dir = $baseFile.DirectoryName
        $baseName = $baseFile.BaseName
        $targetFile = Join-Path $dir "$($baseName)_$($lang).properties"
        
        if (-not (Test-Path $targetFile)) {
            Copy-Item -Path $baseFile.FullName -Destination $targetFile -Force
            Write-Host "  Created missing property file: $($baseName)_$($lang).properties"
            continue
        }
        
        $baseLines = Get-Content -Path $baseFile.FullName -Encoding UTF8
        $targetLines = Get-Content -Path $targetFile -Encoding UTF8
        
        $targetKeyMap = @{}
        foreach ($line in $targetLines) {
            if ($line -match "^([a-zA-Z0-9_\-\.\$]+)\s*=") {
                $targetKeyMap[$matches[1]] = $true
            }
        }
        
        $missingLines = @()
        foreach ($line in $baseLines) {
            if ($line -match "^([a-zA-Z0-9_\-\.\$]+)\s*=") {
                $k = $matches[1]
                if (-not $targetKeyMap.ContainsKey($k)) {
                    $missingLines += $line
                    $targetKeyMap[$k] = $true
                }
            }
        }
        
        if ($missingLines.Count -gt 0) {
            Add-Content -Path $targetFile -Value "`n# Eternity PD Fallback Keys" -Encoding UTF8
            Add-Content -Path $targetFile -Value $missingLines -Encoding UTF8
            Write-Host "  Appended $($missingLines.Count) missing keys to $($baseName)_$($lang).properties"
        }
    }
}
Write-Host "All language files synchronized cleanly!"
