#Requires -Version 5.1
<#
.SYNOPSIS
    冷冻海产品溯源系统 一键启动脚本（后端 Spring Boot + 前端 Vue3）。

.DESCRIPTION
    默认同时启动后端（8080）与前端（5173）。会自动：
      - 缺少 application-dev.yml 时从模板生成（随后请填写数据库密码）；
      - 校验 Java / Node 环境；
      - 可选执行 db/schema.sql、db/area.sql、db/data.sql 初始化数据库（-InitDb，会清库重建）；
      - 可选重新安装前端依赖（-Install）。

.PARAMETER Mode
    both（默认）| backend | frontend。
    both：后端、前端各开一个新窗口后本脚本退出；
    backend / frontend：在当前窗口前台运行。

.PARAMETER InitDb
    依次执行 db/schema.sql、db/area.sql、db/data.sql（需 mysql 客户端在 PATH）。
    注意：schema.sql 会先 DROP 表并重建，将清空现有数据。

.PARAMETER Install
    强制在启动前端前执行 npm install。

.PARAMETER NoWait
    both 模式下不等待后端就绪直接启动前端。

.PARAMETER CheckOnly
    仅检查环境与配置，不启动任何进程。

.EXAMPLE
    .\run.ps1
    .\run.ps1 -Mode backend -InitDb
    .\run.ps1 -Mode frontend -Install
    powershell -ExecutionPolicy Bypass -File .\run.ps1
#>
[CmdletBinding()]
param(
    [ValidateSet('both', 'backend', 'frontend')]
    [string]$Mode = 'both',
    [switch]$InitDb,
    [switch]$Install,
    [switch]$NoWait,
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $root

$devConfigPath = Join-Path $root 'src\main\resources\application-dev.yml'
$templatePath = Join-Path $root 'src\main\resources\application-dev-template.yml'
$backendUrl = 'http://localhost:8080'
$frontendUrl = 'http://localhost:5173'

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg) { Write-Host "  ok  $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  !!  $msg" -ForegroundColor Yellow }
function Stop-Script($msg) { Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

function Get-Tool([string]$name) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($null -eq $cmd) { return $null }
    return $cmd.Source
}

function Ensure-DevConfig {
    if (Test-Path -LiteralPath $devConfigPath) { return }
    if (-not (Test-Path -LiteralPath $templatePath)) {
        Stop-Script "缺少模板 $templatePath"
    }
    Copy-Item -LiteralPath $templatePath -Destination $devConfigPath
    Write-Warn "已从模板生成 application-dev.yml，请填写数据库密码后重新运行：$devConfigPath"
}

function Read-DevConfig {
    $map = @{}
    if (-not (Test-Path -LiteralPath $devConfigPath)) { return $map }
    foreach ($line in Get-Content -LiteralPath $devConfigPath) {
        if ($line -match '^\s*(host|port|database|username|password)\s*:\s*(.+?)\s*$') {
            $key = $Matches[1]
            $val = $Matches[2].Trim().Trim('"').Trim("'")
            if (-not $map.ContainsKey($key)) { $map[$key] = $val }
        }
    }
    return $map
}

function Get-ConfigValue($cfg, $key, $fallback) {
    if ($cfg.ContainsKey($key) -and $cfg[$key]) { return $cfg[$key] }
    return $fallback
}

function Assert-Java {
    if (-not (Get-Tool 'java')) { Stop-Script '未找到 java，请安装 JDK 17 并加入 PATH' }
    $ver = (cmd /c "java -version 2>&1" | Select-Object -First 1)
    Write-Ok "java: $ver"
}

function Assert-Node {
    if (-not (Get-Tool 'npm')) { Stop-Script '未找到 npm，请安装 Node.js 并加入 PATH' }
    $nodeVer = (cmd /c "node -v")
    $npmVer = (cmd /c "npm -v")
    Write-Ok "node: $nodeVer / npm: $npmVer"
}

function Initialize-Db {
    $mysql = Get-Tool 'mysql'
    if (-not $mysql) {
        Write-Warn '未找到 mysql 客户端，跳过建库；请手动执行 db/schema.sql、db/area.sql、db/data.sql'
        return
    }
    $cfg = Read-DevConfig
    $hostArg = Get-ConfigValue $cfg 'host' 'localhost'
    $portArg = Get-ConfigValue $cfg 'port' '3306'
    $userArg = Get-ConfigValue $cfg 'username' 'root'
    $passArg = Get-ConfigValue $cfg 'password' ''

    Write-Step '初始化数据库（schema → area → data，将清空现有数据）'
    $auth = "-h $hostArg -P $portArg -u $userArg"
    if ($passArg) { $auth += " -p$passArg" }

    foreach ($name in @('schema', 'area', 'data')) {
        $file = Join-Path $root "db\$name.sql"
        if (-not (Test-Path -LiteralPath $file)) { Stop-Script "缺少脚本 $file" }
        cmd /c "`"$mysql`" $auth --default-character-set=utf8mb4 2>nul < `"$file`""
        if ($LASTEXITCODE -ne 0) { Stop-Script "执行 db/$name.sql 失败（请检查数据库账号与连接）" }
        Write-Ok "已执行 db/$name.sql"
    }
}

function Ensure-FrontendDeps {
    $ErrorActionPreference = 'Continue'
    $frontend = Join-Path $root 'frontend'
    $nodeModules = Join-Path $frontend 'node_modules'
    if ($Install -or -not (Test-Path -LiteralPath $nodeModules)) {
        Write-Step '安装前端依赖（npm install）...'
        Push-Location $frontend
        try { & npm install } finally { Pop-Location }
        if ($LASTEXITCODE -ne 0) { Stop-Script 'npm install 失败' }
        Write-Ok '前端依赖已就绪'
    } else {
        Write-Ok '前端依赖已存在（如需重装加 -Install）'
    }
}

function Wait-Backend([int]$seconds = 120) {
    Write-Step "等待后端就绪（最多 $seconds 秒）..."
    $deadline = (Get-Date).AddSeconds($seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri "$backendUrl/api/trace/info/probe" -UseBasicParsing -TimeoutSec 2 | Out-Null
            Write-Ok '后端已就绪'
            return
        } catch {
            if ($_.Exception.Response) { Write-Ok '后端已就绪'; return }
        }
        Start-Sleep -Seconds 2
    }
    Write-Warn '等待超时，请查看后端窗口日志'
}

function Start-BackendWindow {
    $mvnw = Join-Path $root 'mvnw.cmd'
    if (-not (Test-Path -LiteralPath $mvnw)) { Stop-Script '未找到 mvnw.cmd' }
    Write-Step "启动后端（新窗口，$backendUrl）..."
    Start-Process -FilePath $mvnw -ArgumentList 'spring-boot:run' -WorkingDirectory $root -WindowStyle Normal | Out-Null
}

function Run-BackendForeground {
    $ErrorActionPreference = 'Continue'
    $mvnw = Join-Path $root 'mvnw.cmd'
    if (-not (Test-Path -LiteralPath $mvnw)) { Stop-Script '未找到 mvnw.cmd' }
    Write-Step "启动后端（当前窗口，$backendUrl）..."
    & $mvnw spring-boot:run
}

function Start-FrontendWindow {
    $frontend = Join-Path $root 'frontend'
    Write-Step "启动前端（新窗口，$frontendUrl）..."
    Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', 'npm run dev' -WorkingDirectory $frontend -WindowStyle Normal | Out-Null
}

function Run-FrontendForeground {
    $ErrorActionPreference = 'Continue'
    $frontend = Join-Path $root 'frontend'
    Write-Step "启动前端（当前窗口，$frontendUrl）..."
    Push-Location $frontend
    try { & npm run dev } finally { Pop-Location }
}

Write-Host '冷冻海产品溯源系统 · 启动脚本' -ForegroundColor White
Write-Host '--------------------------------' -ForegroundColor DarkGray

Ensure-DevConfig

if ($Mode -ne 'frontend') { Assert-Java }
if ($Mode -ne 'backend') { Assert-Node }
Write-Ok "模式：$Mode"

if ($CheckOnly) {
    if (-not (Test-Path -LiteralPath $devConfigPath)) {
        Write-Warn 'application-dev.yml 尚未就绪'
    } else {
        Write-Ok 'application-dev.yml 已就绪'
    }
    if ($InitDb) {
        if (Get-Tool 'mysql') { Write-Ok 'mysql 客户端可用（-InitDb 可执行）' } else { Write-Warn '未找到 mysql 客户端，-InitDb 将跳过' }
    }
    if ($Mode -ne 'backend') {
        if (Test-Path -LiteralPath (Join-Path $root 'frontend\node_modules')) { Write-Ok '前端依赖存在' } else { Write-Warn '前端依赖缺失，启动时将自动 npm install' }
    }
    Write-Host '检查完成，未启动任何进程。' -ForegroundColor Green
    exit 0
}

if ($InitDb) { Initialize-Db }

switch ($Mode) {
    'backend' {
        Run-BackendForeground
    }
    'frontend' {
        Ensure-FrontendDeps
        Run-FrontendForeground
    }
    default {
        Start-BackendWindow
        if (-not $NoWait) { Wait-Backend }
        Ensure-FrontendDeps
        Start-FrontendWindow
        Write-Host ''
        Write-Host '启动完成：' -ForegroundColor Green
        Write-Host "  后端  $backendUrl"
        Write-Host "  前端  $frontendUrl"
        Write-Host "  初始账号  FARM001 / PROC001 / WHOL001 / RETA001 / admin（密码 123456）"
        Write-Host '关闭对应窗口即可停止服务。' -ForegroundColor DarkGray
    }
}
