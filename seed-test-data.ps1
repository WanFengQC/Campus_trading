param(
    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    [string]$DbName = "campus_trading",
    [string]$MySqlCmd = "mysql"
)

$ErrorActionPreference = "Stop"

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK]   $Message" -ForegroundColor Green
}

function Get-MySqlArgs {
    param([string]$Database = "")

    $args = @(
        "--host=$DbHost",
        "--port=$DbPort",
        "--user=$DbUser",
        "--default-character-set=utf8mb4",
        "--batch",
        "--silent",
        "--raw"
    )

    if ($Database -ne "") {
        $args += "--database=$Database"
    }

    return $args
}

function Normalize-MySqlOutput {
    param([object[]]$RawOutput)

    $lines = @()
    foreach ($item in $RawOutput) {
        if ($null -eq $item) {
            continue
        }

        $line = $item.ToString()
        if ($line -match "Using a password on the command line interface can be insecure") {
            continue
        }

        if (-not [string]::IsNullOrWhiteSpace($line)) {
            $lines += $line
        }
    }

    return $lines
}

function Invoke-MySql {
    param(
        [string]$Sql,
        [string]$Database = ""
    )

    $args = Get-MySqlArgs -Database $Database
    $args += "--execute=$Sql"

    $stdoutFile = [System.IO.Path]::GetTempFileName()
    $stderrFile = [System.IO.Path]::GetTempFileName()
    $previousErrorAction = $ErrorActionPreference
    $previousMysqlPwd = $env:MYSQL_PWD
    $hasNativeErrorPreference = Test-Path variable:PSNativeCommandUseErrorActionPreference
    $previousNativeErrorPreference = $null

    try {
        # Prevent native stderr text from becoming a terminating PowerShell error.
        $ErrorActionPreference = "Continue"
        if ($hasNativeErrorPreference) {
            $previousNativeErrorPreference = $PSNativeCommandUseErrorActionPreference
            $PSNativeCommandUseErrorActionPreference = $false
        }
        if ($DbPassword -ne "") {
            $env:MYSQL_PWD = $DbPassword
        }
        else {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        }
        & $MySqlCmd @args 1> $stdoutFile 2> $stderrFile
        $exitCode = $LASTEXITCODE

        $stdoutLines = @()
        $stderrLines = @()
        if (Test-Path $stdoutFile) {
            $stdoutLines = Get-Content -Path $stdoutFile
        }
        if (Test-Path $stderrFile) {
            $stderrLines = Get-Content -Path $stderrFile
        }

        $output = Normalize-MySqlOutput -RawOutput @($stdoutLines + $stderrLines)
        if ($exitCode -ne 0) {
            $message = $output -join [System.Environment]::NewLine
            if ([string]::IsNullOrWhiteSpace($message)) {
                $message = "No output from mysql."
            }
            throw "MySQL execute failed.`n$message"
        }

        return $output
    }
    finally {
        $ErrorActionPreference = $previousErrorAction
        if ($hasNativeErrorPreference) {
            $PSNativeCommandUseErrorActionPreference = $previousNativeErrorPreference
        }
        if ($null -ne $previousMysqlPwd) {
            $env:MYSQL_PWD = $previousMysqlPwd
        }
        else {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        }
        if (Test-Path $stdoutFile) {
            Remove-Item -Path $stdoutFile -Force -ErrorAction SilentlyContinue
        }
        if (Test-Path $stderrFile) {
            Remove-Item -Path $stderrFile -Force -ErrorAction SilentlyContinue
        }
    }
}

function Invoke-MySqlFile {
    param(
        [string]$SqlFilePath,
        [string]$Database = ""
    )

    if (-not (Test-Path $SqlFilePath)) {
        throw "SQL file not found: $SqlFilePath"
    }

    $pathForMySql = $SqlFilePath.Replace("\", "/")
    Invoke-MySql -Database $Database -Sql "SOURCE $pathForMySql;" | Out-Null
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$schemaSqlPath = Join-Path $root "backend\src\main\resources\db\schema.sql"
$seedSqlPath = Join-Path $root "backend\src\main\resources\db\seed-test-data.sql"

if (-not (Test-Path $schemaSqlPath)) {
    throw "schema.sql not found: $schemaSqlPath"
}

if (-not (Test-Path $seedSqlPath)) {
    throw "seed-test-data.sql not found: $seedSqlPath"
}

try {
    $null = Get-Command $MySqlCmd -ErrorAction Stop
}
catch {
    throw "mysql command not found: $MySqlCmd. Pass full mysql.exe path with -MySqlCmd."
}

Write-Info "Using mysql command: $MySqlCmd"
Write-Info "Target database: $DbName"

Invoke-MySql -Database "" -Sql "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
Write-Ok "Database ready: $DbName"

Invoke-MySqlFile -Database $DbName -SqlFilePath $schemaSqlPath
Write-Ok "Schema ready"

Invoke-MySqlFile -Database $DbName -SqlFilePath $seedSqlPath
Write-Ok "Seed data import completed"

Write-Host ""
Write-Host "Demo login accounts (password: 123456):"
Write-Host "  - 20260001"
Write-Host "  - 20260002"
Write-Host "  - 20260003"
Write-Host "  - admin"

