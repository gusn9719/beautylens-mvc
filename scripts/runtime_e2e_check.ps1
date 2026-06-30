$ErrorActionPreference = 'Continue'

$baseUrl = 'http://localhost:8088/beautylens-mvc'
$java = 'C:\Program Files\Java\jdk-21\bin\java.exe'
$classpath = 'D:\Lecture\bin\apache-tomcat-11.0.18\bin\bootstrap.jar;D:\Lecture\bin\apache-tomcat-11.0.18\bin\tomcat-juli.jar'
$tomcatArgs = @(
    '--add-opens=java.base/java.lang=ALL-UNNAMED',
    '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
    '--add-opens=java.base/java.io=ALL-UNNAMED',
    '--add-opens=java.base/java.util=ALL-UNNAMED',
    '--add-opens=java.base/java.util.concurrent=ALL-UNNAMED',
    '--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED',
    '-Dcatalina.home=D:\Lecture\bin\apache-tomcat-11.0.18',
    '-Dcatalina.base=D:\Lecture\eclipse-server',
    '-Djava.io.tmpdir=D:\Lecture\eclipse-server\temp',
    '-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager',
    '-classpath',
    $classpath,
    'org.apache.catalina.startup.Bootstrap',
    'start'
)

function Invoke-Check {
    param(
        [string] $Name,
        [string] $Method,
        [string] $Url,
        $Session = $null,
        $Body = $null,
        [string] $ContentType = 'application/json; charset=utf-8'
    )
    try {
        $params = @{
            UseBasicParsing = $true
            Uri = $Url
            Method = $Method
        }
        if ($Session -ne $null) { $params.WebSession = $Session }
        if ($Body -ne $null) {
            $params.Body = ($Body | ConvertTo-Json -Depth 8)
            $params.ContentType = $ContentType
        }
        $r = Invoke-WebRequest @params
        "$Name STATUS=$([int]$r.StatusCode) OK"
        $script:LastResponse = $r
    } catch {
        if ($_.Exception.Response) {
            "$Name STATUS=$([int]$_.Exception.Response.StatusCode) ERROR"
        } else {
            "$Name STATUS=ERR ERROR $($_.Exception.Message)"
        }
        $script:LastResponse = $null
    }
}

$existing = Get-NetTCPConnection -LocalPort 8088 -ErrorAction SilentlyContinue
if ($existing) {
    "PORT_8088_ALREADY_IN_USE PID=$($existing[0].OwningProcess)"
    exit 2
}

$p = Start-Process -FilePath $java -ArgumentList $tomcatArgs -WorkingDirectory 'D:\Lecture\bin\apache-tomcat-11.0.18' -WindowStyle Hidden -PassThru
try {
    $healthy = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        try {
            $health = Invoke-WebRequest -UseBasicParsing -Uri "$baseUrl/api/health"
            if ($health.StatusCode -eq 200) {
                $healthy = $true
                break
            }
        } catch {}
    }
    if (-not $healthy) {
        "TOMCAT_START STATUS=FAIL"
        exit 3
    }
    "TOMCAT_START STATUS=OK PID=$($p.Id)"

    $pages = @(
        '/', '/products', '/recommend', '/products/1446', '/mypage',
        '/admin', '/admin/products', '/admin/comments', '/admin/comment-reports',
        '/admin/logs', '/api/health'
    )
    foreach ($path in $pages) {
        Invoke-Check "PAGE $path" 'GET' "$baseUrl$path"
    }

    $publicApis = @(
        '/api/products?sortBy=score&size=20',
        '/api/products?imageOnly=true&sortBy=score&size=20',
        '/api/recommendations?skinType=dry&size=20'
    )
    foreach ($path in $publicApis) {
        Invoke-Check "PUBLIC_API $path" 'GET' "$baseUrl$path"
    }

    $anonProtected = @(
        '/api/members/me/favorites',
        '/api/members/me/ratings',
        '/api/members/me/recent-products',
        '/api/admin/summary',
        '/api/admin/products',
        '/api/admin/comment-reports',
        '/api/admin/logs'
    )
    foreach ($path in $anonProtected) {
        Invoke-Check "ANON_PROTECTED $path" 'GET' "$baseUrl$path"
    }

    $testSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-Check 'LOGIN test01' 'POST' "$baseUrl/api/auth/login" $testSession @{ loginId = 'test01'; password = '1234' }
    Invoke-Check 'TEST01 me' 'GET' "$baseUrl/api/members/me" $testSession
    Invoke-Check 'TEST01 recommendations me' 'GET' "$baseUrl/api/recommendations/me?size=20" $testSession
    Invoke-Check 'TEST01 admin summary should be 403' 'GET' "$baseUrl/api/admin/summary" $testSession
    Invoke-Check 'TEST01 detail event ORA17004 check' 'POST' "$baseUrl/api/products/743/events" $testSession @{ eventType = 'DETAIL_VIEW' }
    Invoke-Check 'TEST01 favorite' 'POST' "$baseUrl/api/products/743/favorite" $testSession @{}
    Invoke-Check 'TEST01 unfavorite' 'DELETE' "$baseUrl/api/products/743/favorite" $testSession
    Invoke-Check 'TEST01 rating' 'POST' "$baseUrl/api/products/743/rating" $testSession @{ rating = 4; irritationYn = 'N'; repurchaseYn = 'Y'; reviewText = 'runtime verification' }
    Invoke-Check 'TEST01 recommendation feedback' 'POST' "$baseUrl/api/products/743/recommendation-feedback" $testSession @{ feedbackType = 'LIKE' }
    Invoke-Check 'TEST01 my favorites' 'GET' "$baseUrl/api/members/me/favorites" $testSession
    Invoke-Check 'TEST01 my ratings' 'GET' "$baseUrl/api/members/me/ratings" $testSession
    Invoke-Check 'TEST01 recent products' 'GET' "$baseUrl/api/members/me/recent-products" $testSession
    Invoke-Check 'TEST01 my recommendation feedback' 'GET' "$baseUrl/api/members/me/recommendation-feedback" $testSession
    Invoke-Check 'TEST01 comment create' 'POST' "$baseUrl/api/products/743/comments" $testSession @{ content = 'runtime verification comment' }
    Invoke-Check 'TEST01 comments for report lookup' 'GET' "$baseUrl/api/members/me/comments" $testSession
    $myComments = $script:LastResponse
    $commentId = $null
    if ($myComments -and $myComments.Content) {
        $json = $myComments.Content | ConvertFrom-Json
        $comment = @($json.data | Where-Object { $_.productId -eq 743 -and $_.status -eq 'ACTIVE' } | Select-Object -First 1)
        if ($comment.Count -gt 0) { $commentId = $comment[0].commentId }
    }
    if ($commentId) {
        Invoke-Check "TEST01 comment report $commentId" 'POST' "$baseUrl/api/comments/$commentId/report" $testSession @{ reasonType = 'ETC'; reasonText = 'runtime verification report' }
    } else {
        'TEST01 comment report STATUS=SKIP no_comment_id'
    }

    $adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-Check 'LOGIN admin' 'POST' "$baseUrl/api/auth/login" $adminSession @{ loginId = 'admin'; password = '1234' }
    Invoke-Check 'ADMIN summary' 'GET' "$baseUrl/api/admin/summary" $adminSession
    Invoke-Check 'ADMIN products' 'GET' "$baseUrl/api/admin/products?size=5" $adminSession
    Invoke-Check 'ADMIN flags' 'GET' "$baseUrl/api/admin/products/743/flags" $adminSession
    Invoke-Check 'ADMIN hide' 'POST' "$baseUrl/api/admin/products/743/hide" $adminSession @{ reason = 'RUNTIME_TEST' }
    Invoke-Check 'ADMIN hidden product public detail should be 404' 'GET' "$baseUrl/api/products/743"
    Invoke-Check 'ADMIN restore' 'POST' "$baseUrl/api/admin/products/743/restore" $adminSession @{}
    Invoke-Check 'ADMIN restored product public detail' 'GET' "$baseUrl/api/products/743"
    Invoke-Check 'ADMIN exclude recommendation' 'POST' "$baseUrl/api/admin/products/743/exclude-recommendation" $adminSession @{}
    Invoke-Check 'ADMIN include recommendation' 'POST' "$baseUrl/api/admin/products/743/include-recommendation" $adminSession @{}
    Invoke-Check 'ADMIN save flags' 'PUT' "$baseUrl/api/admin/products/743/flags" $adminSession @{ isVisible = 'Y'; excludeRecommendation = 'N'; isFeatured = 'N'; qualityStatus = 'NORMAL'; adminMemo = 'runtime verification memo' }
    Invoke-Check 'ADMIN comment reports' 'GET' "$baseUrl/api/admin/comment-reports?size=20" $adminSession
    if ($commentId) {
        Invoke-Check "ADMIN comment delete $commentId" 'DELETE' "$baseUrl/api/admin/comments/$commentId" $adminSession
        Invoke-Check "ADMIN comment restore $commentId" 'POST' "$baseUrl/api/admin/comments/$commentId/restore" $adminSession @{}
    }
    Invoke-Check 'ADMIN logs' 'GET' "$baseUrl/api/admin/logs?size=20" $adminSession

    try {
        $face = Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8090/health'
        "FACE_SERVER STATUS=$([int]$face.StatusCode) OK"
    } catch {
        "FACE_SERVER STATUS=ERR $($_.Exception.Message)"
    }
} finally {
    if ($p -and -not $p.HasExited) {
        Stop-Process -Id $p.Id -Force
        "TOMCAT_STOP STATUS=OK PID=$($p.Id)"
    }
}
