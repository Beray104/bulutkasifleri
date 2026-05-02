param(
    [string]$SqlServer = $(if ($env:MSSQL_HOST) { $env:MSSQL_HOST } else { "localhost" }),
    [string]$SqlPort = $(if ($env:MSSQL_PORT) { $env:MSSQL_PORT } else { "1433" }),
    [string]$SqlUser = $(if ($env:MSSQL_USER) { $env:MSSQL_USER } else { "sa" }),
    [string]$SqlPassword = $(if ($env:MSSQL_SA_PASSWORD) { $env:MSSQL_SA_PASSWORD } else { "StrongPassw0rd!" }),
    [string]$ElasticsearchUrl = $(if ($env:ELASTICSEARCH_URL) { $env:ELASTICSEARCH_URL } else { "http://localhost:9200" })
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptDir "init-mssql.sql"
$mappingFile = Join-Path $scriptDir "elasticsearch-social-media-analytics-mapping.json"

Write-Host "MS SQL Server schema initialization started..."
sqlcmd -S "$SqlServer,$SqlPort" -U $SqlUser -P $SqlPassword -b -i $sqlFile
if ($LASTEXITCODE -ne 0) {
    throw "MS SQL Server schema initialization failed with exit code $LASTEXITCODE."
}
Write-Host "MS SQL Server schema initialization completed."

Write-Host "Elasticsearch index initialization started..."
$indexUrl = "$($ElasticsearchUrl.TrimEnd('/'))/social_media_analytics"
$mappingBody = Get-Content -Raw -Path $mappingFile

try {
    Invoke-RestMethod -Uri $indexUrl -Method Put -ContentType "application/json" -Body $mappingBody | Out-Null
    Write-Host "Elasticsearch index created: $indexUrl"
}
catch {
    $response = $_.Exception.Response
    if ($response -and [int]$response.StatusCode -eq 400) {
        Write-Host "Elasticsearch index already exists: $indexUrl"
    }
    else {
        throw
    }
}

Write-Host "Database initialization completed."
