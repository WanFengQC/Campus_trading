param(
    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    [string]$DbName = "campus_trading",
    [string]$MySqlCmd = "mysql",
    [switch]$ForceConfigOverwrite
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

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
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

function Escape-SqlLiteral {
    param([string]$Value)

    if ($null -eq $Value) {
        return ""
    }

    return $Value.Replace("'", "''")
}

function Get-ColumnExists {
    param(
        [string]$TableName,
        [string]$ColumnName
    )

    $dbNameEscaped = Escape-SqlLiteral -Value $DbName
    $tableEscaped = Escape-SqlLiteral -Value $TableName
    $columnEscaped = Escape-SqlLiteral -Value $ColumnName

    $sql = @"
SELECT COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='$dbNameEscaped'
  AND TABLE_NAME='$tableEscaped'
  AND COLUMN_NAME='$columnEscaped';
"@

    $result = Invoke-MySql -Sql $sql -Database ""
    $firstLine = ($result | Select-Object -First 1)

    if ($null -eq $firstLine -or [string]::IsNullOrWhiteSpace($firstLine.ToString())) {
        return 0
    }

    return [int]$firstLine.ToString().Trim()
}

function Ensure-Column {
    param(
        [string]$TableName,
        [string]$ColumnName,
        [string]$DefinitionSql
    )

    if ((Get-ColumnExists -TableName $TableName -ColumnName $ColumnName) -eq 0) {
        Write-Info "Adding column $TableName.$ColumnName ..."
        Invoke-MySql -Database $DbName -Sql "ALTER TABLE $TableName ADD COLUMN $DefinitionSql;"
        Write-Ok "Added column $TableName.$ColumnName"
    }
    else {
        Write-Ok "Column exists: $TableName.$ColumnName"
    }
}

function Ensure-ConfigFile {
    param(
        [string]$ExamplePath,
        [string]$TargetPath
    )

    if ((-not (Test-Path $TargetPath)) -or $ForceConfigOverwrite.IsPresent) {
        Copy-Item $ExamplePath $TargetPath -Force
        Write-Ok "Created config file: $TargetPath"
    }
    else {
        Write-Ok "Config file exists: $TargetPath"
    }

    $jdbcUrl = "jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" -f $DbHost, $DbPort, $DbName

    $raw = [System.IO.File]::ReadAllText($TargetPath)
    $raw = [System.Text.RegularExpressions.Regex]::Replace($raw, "url:\s*jdbc:mysql://[^\r\n]+", "url: $jdbcUrl")
    $raw = [System.Text.RegularExpressions.Regex]::Replace($raw, "username:\s*[^\r\n]*", "username: $DbUser")
    $raw = [System.Text.RegularExpressions.Regex]::Replace($raw, "password:\s*[^\r\n]*", "password: $DbPassword")

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($TargetPath, $raw, $utf8NoBom)
    Write-Ok "Updated datasource in application-local.yml"
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root "backend"
$schemaPath = Join-Path $backend "src/main/resources/db/schema.sql"
$exampleCfg = Join-Path $backend "src/main/resources/application-local.example.yml"
$localCfg = Join-Path $backend "src/main/resources/application-local.yml"

if (-not (Test-Path $schemaPath)) {
    throw "schema.sql not found: $schemaPath"
}

if (-not (Test-Path $exampleCfg)) {
    throw "application-local.example.yml not found: $exampleCfg"
}

try {
    $null = Get-Command $MySqlCmd -ErrorAction Stop
}
catch {
    throw "mysql command not found: $MySqlCmd. Pass full mysql.exe path with -MySqlCmd."
}

Write-Info "Using mysql command: $MySqlCmd"
Write-Info "Initializing database: $DbName"

Invoke-MySql -Database "" -Sql "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
Write-Ok "Database ready: $DbName"

Invoke-MySqlFile -Database $DbName -SqlFilePath $schemaPath
Write-Ok "Executed schema.sql"

# Idempotent safety patch for existing old schema versions.
Ensure-Column -TableName "user" -ColumnName "avatar_url" -DefinitionSql "avatar_url VARCHAR(255) NULL AFTER nickname"
Ensure-Column -TableName "user" -ColumnName "audit_status" -DefinitionSql "audit_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED' AFTER status"
Ensure-Column -TableName "user" -ColumnName "audit_note" -DefinitionSql "audit_note VARCHAR(255) NULL AFTER audit_status"
Ensure-Column -TableName "user" -ColumnName "audit_time" -DefinitionSql "audit_time DATETIME NULL AFTER audit_note"

Ensure-Column -TableName "goods" -ColumnName "condition_level" -DefinitionSql "condition_level VARCHAR(32) NOT NULL DEFAULT 'NINE_TENTHS_NEW' AFTER price"
Ensure-Column -TableName "goods" -ColumnName "contact_info" -DefinitionSql "contact_info VARCHAR(64) NOT NULL DEFAULT '' AFTER condition_level"
Ensure-Column -TableName "goods" -ColumnName "cover_image_url" -DefinitionSql "cover_image_url VARCHAR(255) NULL AFTER contact_info"
Ensure-Column -TableName "goods" -ColumnName "audit_status" -DefinitionSql "audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' AFTER status"
Ensure-Column -TableName "goods" -ColumnName "audit_note" -DefinitionSql "audit_note VARCHAR(255) NULL AFTER audit_status"
Ensure-Column -TableName "goods" -ColumnName "audit_time" -DefinitionSql "audit_time DATETIME NULL AFTER audit_note"

Ensure-Column -TableName "trade_order" -ColumnName "amount" -DefinitionSql "amount DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER seller_id"
Ensure-Column -TableName "trade_order" -ColumnName "buyer_remark" -DefinitionSql "buyer_remark VARCHAR(255) NULL AFTER amount"
Ensure-Column -TableName "trade_order" -ColumnName "meetup_time" -DefinitionSql "meetup_time DATETIME NULL AFTER buyer_remark"
Ensure-Column -TableName "trade_order" -ColumnName "meetup_location" -DefinitionSql "meetup_location VARCHAR(255) NULL AFTER meetup_time"
Ensure-Column -TableName "trade_order" -ColumnName "meetup_note" -DefinitionSql "meetup_note VARCHAR(255) NULL AFTER meetup_location"

Invoke-MySql -Database $DbName -Sql "ALTER TABLE goods MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'OFF_SHELF';"
Write-Ok "Ensured goods.status default OFF_SHELF"

Invoke-MySql -Database $DbName -Sql "ALTER TABLE goods MODIFY COLUMN audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING';"
Write-Ok "Ensured goods.audit_status default PENDING"

Invoke-MySql -Database $DbName -Sql @"
INSERT INTO notice (id, title, content, status, sort_no, publisher, created_at)
VALUES
(90001, 'Platform Notice: Trade Safely', 'Use public places on campus for offline trade and avoid private transfer risk.', 1, 20, 'System', NOW()),
(90002, 'Platform Notice: New Modules', 'Rental, donation, review and report modules are enabled for demo.', 1, 15, 'System', NOW())
ON DUPLICATE KEY UPDATE
title = VALUES(title),
content = VALUES(content),
status = VALUES(status),
sort_no = VALUES(sort_no),
publisher = VALUES(publisher);
"@
Write-Ok "Ensured default notices"

Invoke-MySql -Database $DbName -Sql @"
INSERT INTO admin_user (username, password, nickname, status, created_at)
VALUES ('admin', '$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2', 'System Admin', 1, NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status);
"@
Write-Ok "Ensured default admin account: admin / 123456"

Ensure-ConfigFile -ExamplePath $exampleCfg -TargetPath $localCfg

Write-Host ""
Write-Ok "Initialization completed."
Write-Host "Next steps:"
Write-Host "  cd $backend"
Write-Host "  mvn spring-boot:run"
Write-Host ""
Write-Warn "If mvn is not in PATH, run Maven with its full path."

