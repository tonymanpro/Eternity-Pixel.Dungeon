param(
    [string]$TargetLang = "es"
)

$baseDir = "core/src/main/assets/messages"
$files = Get-ChildItem -Path $baseDir -Recurse -Filter "*.properties" | Where-Object { $_.Name -notmatch "_[a-z]{2}(-[a-z]+)?\.properties" }

$totalMissing = 0

Write-Output "=================================================="
Write-Output " Verificación de Traducciones (Idioma: $TargetLang)"
Write-Output "=================================================="

foreach ($file in $files) {
    $dir = $file.DirectoryName
    $baseName = $file.BaseName
    $targetFile = Join-Path $dir "$($baseName)_$($TargetLang).properties"
    
    if (Test-Path $targetFile) {
        $enKeys = (Get-Content $file.FullName | Where-Object { $_ -match "^([a-zA-Z0-9_\-\.\$]+)\s*=" } | ForEach-Object { $matches[1] })
        $targetKeys = (Get-Content $targetFile | Where-Object { $_ -match "^([a-zA-Z0-9_\-\.\$]+)\s*=" } | ForEach-Object { $matches[1] })
        $missing = $enKeys | Where-Object { $_ -notin $targetKeys }
        
        if ($missing.Count -gt 0) {
            Write-Output "[FAIL] $($file.Name) -> Faltan $($missing.Count) claves en $($baseName)_$($TargetLang).properties"
            $missing | Select-Object -First 10 | ForEach-Object { Write-Output "   - $_" }
            if ($missing.Count -gt 10) { Write-Output "   ... y $($missing.Count - 10) más." }
            $totalMissing += $missing.Count
        } else {
            Write-Output "[ OK ] $($file.Name) -> 100% traducido ($($enKeys.Count) claves)"
        }
    } else {
        Write-Output "[MISS] No existe el archivo: $($baseName)_$($TargetLang).properties"
        $totalMissing += 1
    }
}

Write-Output "=================================================="
if ($totalMissing -eq 0) {
    Write-Output " RESULTADO: ¡Todas las traducciones están al 100% sincronizadas!"
    exit 0
} else {
    Write-Output " RESULTADO: Faltan un total de $totalMissing claves por traducir."
    exit 1
}
