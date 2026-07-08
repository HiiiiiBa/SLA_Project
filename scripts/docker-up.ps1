param(
    [switch]$Build,
    [switch]$LocalOnly
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Invoke-LocalBuild {
    Write-Host ""
    Write-Host "Build local (sans Docker Hub)..." -ForegroundColor Cyan

    Write-Host "  -> frontend: npm run build" -ForegroundColor Gray
    Push-Location frontend
    npm run build
    if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
    Pop-Location

    Write-Host "  -> backend: mvn package -DskipTests" -ForegroundColor Gray
    Push-Location backend
    mvn package -DskipTests -q
    if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
    Pop-Location

    Write-Host "  -> docker compose (from-local)" -ForegroundColor Gray
    docker compose -f docker-compose.yml -f docker-compose.from-local.yml up --build -d
    return $LASTEXITCODE
}

if ($Build -or $LocalOnly) {
    if ($LocalOnly) {
        $exitCode = Invoke-LocalBuild
        if ($exitCode -ne 0) { exit $exitCode }
    } else {
        Write-Host "Build demande : tentative docker compose up --build -d ..." -ForegroundColor Cyan
        docker compose up --build -d
        if ($LASTEXITCODE -ne 0) {
            Write-Host ""
            Write-Host "Echec Docker Hub (DNS: registry-1.docker.io / auth.docker.io)." -ForegroundColor Yellow
            Write-Host "Bascule automatique vers le build local..." -ForegroundColor Yellow
            $exitCode = Invoke-LocalBuild
            if ($exitCode -ne 0) { exit $exitCode }
        }
    }
} else {
    Write-Host "Demarrage sans rebuild (images deja construites)..." -ForegroundColor Cyan
    docker compose up -d
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

docker compose ps
