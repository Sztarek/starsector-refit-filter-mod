# Builds jars/RefitFilters.jar from src/ with the Kotlin command-line compiler.
#
# Requires: JDK 17 on PATH and kotlinc 2.1.x (matching the Kotlin runtime shipped by LazyLib).
# Default kotlinc location: %USERPROFILE%\.starsector-tools\kotlinc  (override with $env:KOTLINC_HOME)
#
# Usage (from anywhere):  powershell -ExecutionPolicy Bypass -File ".\build.ps1"

$ErrorActionPreference = "Stop"

$modDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$gameDir = (Resolve-Path (Join-Path $modDir "..\..")).Path
$core    = Join-Path $gameDir "starsector-core"
$mods    = Join-Path $gameDir "mods"

$kotlincHome = $env:KOTLINC_HOME
if (-not $kotlincHome) { $kotlincHome = Join-Path $env:USERPROFILE ".starsector-tools\kotlinc" }
$kotlinc = Join-Path $kotlincHome "bin\kotlinc.bat"
if (-not (Test-Path $kotlinc)) { throw "kotlinc not found at $kotlinc (set `$env:KOTLINC_HOME)" }

# Pick the LazyLib / LunaLib folders that actually contain the jars we need.
$lazyLib = Get-ChildItem $mods -Directory -Filter "LazyLib*" |
    Where-Object { Test-Path (Join-Path $_.FullName "jars\internal\Kotlin-Runtime.jar") } |
    Select-Object -First 1
$lunaLib = Get-ChildItem $mods -Directory -Filter "LunaLib*" |
    Where-Object { Test-Path (Join-Path $_.FullName "jars\LunaLib.jar") } |
    Select-Object -First 1
if (-not $lazyLib) { throw "LazyLib (with jars\internal\Kotlin-Runtime.jar) not found under $mods" }
if (-not $lunaLib) { throw "LunaLib (with jars\LunaLib.jar) not found under $mods" }

$cp = @(
    (Join-Path $core "starfarer.api.jar"),
    (Join-Path $core "starfarer_obf.jar"),
    (Join-Path $core "fs.common_obf.jar"),
    (Join-Path $core "lwjgl.jar"),
    (Join-Path $core "lwjgl_util.jar"),
    (Join-Path $core "log4j-1.2.9.jar"),
    (Join-Path $core "json.jar"),
    (Join-Path $core "xstream-1.4.10.jar"),
    (Join-Path $lazyLib.FullName "jars\LazyLib.jar"),
    (Join-Path $lazyLib.FullName "jars\LazyLib-Kotlin.jar"),
    (Join-Path $lazyLib.FullName "jars\internal\Kotlin-Runtime.jar"),
    (Join-Path $lunaLib.FullName "jars\LunaLib.jar"),
    (Join-Path $lunaLib.FullName "jars\libs\fuzzywuzzy-1.3.0.jar")
) -join ";"

$src = Join-Path $modDir "src"
$out = Join-Path $modDir "jars\RefitFilters.jar"
New-Item -ItemType Directory -Force (Split-Path $out) | Out-Null

Write-Host "kotlinc : $kotlinc"
Write-Host "LazyLib : $($lazyLib.FullName)"
Write-Host "LunaLib : $($lunaLib.FullName)"
Write-Host "output  : $out"

# -no-stdlib: use the exact Kotlin runtime LazyLib ships instead of the compiler's bundled copy.
& $kotlinc -jvm-target 17 -no-stdlib -no-reflect -Xno-call-assertions -Xno-param-assertions `
    -cp $cp $src -d $out
if ($LASTEXITCODE -ne 0) { throw "kotlinc failed with exit code $LASTEXITCODE" }

Write-Host "Built $out"
