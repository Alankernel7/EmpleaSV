# Evidencias de pruebas — Cliente consumidor y API REST
# Se ejecuta contra http://localhost:8080/EmpleaSV/api
$b = "http://localhost:8080/EmpleaSV/api"
$out = @()

function Show($titulo, $resp, $err) {
    if ($err) {
        $code = $err.Exception.Response.StatusCode.value__
        $body = $err.ErrorDetails.Message
        $script:out += "### $titulo`n- HTTP: $code`n- $body`n"
    } else {
        $script:out += "### $titulo`n- HTTP: $($resp.StatusCode)`n- $($resp.Content)`n"
    }
}

# 1. GET público de ofertas
try { $r = Invoke-WebRequest "$b/ofertas?titulo=java" -UseBasicParsing; Show "1. GET /ofertas?titulo=java" $r $null } catch { Show "1. GET /ofertas?titulo=java" $null $_ }

# 2. 404 oferta inexistente
try { $r = Invoke-WebRequest "$b/ofertas/999999" -UseBasicParsing; Show "2. GET /ofertas/999999" $r $null } catch { Show "2. GET /ofertas/999999" $null $_ }

# 3. 401 sin sesión
try { $r = Invoke-WebRequest "$b/perfiles/candidato" -UseBasicParsing; Show "3. GET /perfiles/candidato (sin sesion)" $r $null } catch { Show "3. GET /perfiles/candidato (sin sesion)" $null $_ }

# 4. Login inválido
try { $r = Invoke-WebRequest "$b/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"no-existe@prueba.com","password":"mala"}' -UseBasicParsing; Show "4. POST /auth/login invalido" $r $null } catch { Show "4. POST /auth/login invalido" $null $_ }

# 5. Login empresa OK
try { $r = Invoke-WebRequest "$b/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"empresa@prueba.com","password":"Prueba123"}' -UseBasicParsing -SessionVariable ses; Show "5. POST /auth/login (empresa)" $r $null } catch { Show "5. POST /auth/login (empresa)" $null $_ }

# 6. 403: empresa intenta dashboard admin
try { $r = Invoke-WebRequest "$b/admin/dashboard" -UseBasicParsing -WebSession $ses; Show "6. GET /admin/dashboard (rol EMPRESA)" $r $null } catch { Show "6. GET /admin/dashboard (rol EMPRESA)" $null $_ }

# 7. Crear oferta (201)
$nueva = '{"titulo":"Prueba Cliente REST","descripcion":"Oferta creada por el cliente independiente durante las pruebas.","requisitos":"Java","ubicacion":"Santa Ana","salario":750.00,"tipoContrato":"Tiempo completo","horario":"8:00 AM - 4:00 PM","estado":"ACTIVA"}'
try { $r = Invoke-WebRequest "$b/ofertas" -Method Post -ContentType "application/json" -Body $nueva -UseBasicParsing -WebSession $ses; Show "7. POST /ofertas (empresa)" $r $null; $id = ($r.Content | ConvertFrom-Json).datos.id } catch { Show "7. POST /ofertas (empresa)" $null $_; $id = $null }

if ($id) {
    # 8. PUT actualizar
    $act = '{"titulo":"Prueba Cliente REST v2","descripcion":"Oferta actualizada con PUT.","ubicacion":"Santa Ana","salario":800.00,"tipoContrato":"Tiempo completo"}'
    try { $r = Invoke-WebRequest "$b/ofertas/$id" -Method Put -ContentType "application/json" -Body $act -UseBasicParsing -WebSession $ses; Show "8. PUT /ofertas/$id" $r $null } catch { Show "8. PUT /ofertas/$id" $null $_ }

    # 9. PATCH pausar
    try { $r = Invoke-WebRequest "$b/ofertas/$id/estado" -Method Patch -ContentType "application/json" -Body '{"estado":"PAUSADA"}' -UseBasicParsing -WebSession $ses; Show "9. PATCH /ofertas/$id/estado -> PAUSADA" $r $null } catch { Show "9. PATCH /ofertas/$id/estado -> PAUSADA" $null $_ }

    # 10. 409 transición inválida (PAUSADA -> PAUSADA)
    try { $r = Invoke-WebRequest "$b/ofertas/$id/estado" -Method Patch -ContentType "application/json" -Body '{"estado":"PAUSADA"}' -UseBasicParsing -WebSession $ses; Show "10. PATCH estado repetido (409 esperado)" $r $null } catch { Show "10. PATCH estado repetido (409 esperado)" $null $_ }

    # 11. DELETE
    try { $r = Invoke-WebRequest "$b/ofertas/$id" -Method Delete -UseBasicParsing -WebSession $ses; Show "11. DELETE /ofertas/$id" $r $null } catch { Show "11. DELETE /ofertas/$id" $null $_ }
}

# 12. Login admin + dashboard
try { $r = Invoke-WebRequest "$b/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@prueba.com","password":"Prueba123"}' -UseBasicParsing -SessionVariable sesA; Show "12. POST /auth/login (admin)" $r $null } catch { Show "12. POST /auth/login (admin)" $null $_; $sesA = $null }
if ($sesA) {
    try { $r = Invoke-WebRequest "$b/admin/dashboard" -UseBasicParsing -WebSession $sesA; Show "13. GET /admin/dashboard (admin)" $r $null } catch { Show "13. GET /admin/dashboard (admin)" $null $_ }
}

# 14. Empresas públicas
try { $r = Invoke-WebRequest "$b/empresas/1/ofertas" -UseBasicParsing; Show "14. GET /empresas/1/ofertas" $r $null } catch { Show "14. GET /empresas/1/ofertas" $null $_ }

# 15. Logout
try { $r = Invoke-WebRequest "$b/auth/logout" -Method Post -UseBasicParsing -WebSession $ses; Show "15. POST /auth/logout" $r $null } catch { Show "15. POST /auth/logout" $null $_ }

# 16. Tras logout: 401
try { $r = Invoke-WebRequest "$b/ofertas?mias=true" -UseBasicParsing -WebSession $ses; Show "16. GET /ofertas?mias=true tras logout (401 esperado)" $r $null } catch { Show "16. GET /ofertas?mias=true tras logout (401 esperado)" $null $_ }

$out | Out-File -FilePath "C:\Users\Julissa\OneDrive\Escritorio\EmpleaSV\docs\fase3\evidencias_pruebas.txt" -Encoding utf8
Write-Output "LISTO - $(($out | Measure-Object -Line).Lines) lineas"
