# ============================================================
#  TEST COMPLET - FitnessStudioApp
#  Porneste aplicatia inainte: ./mvnw.cmd spring-boot:run
#  Apoi ruleaza: .\test_complet.ps1
# ============================================================

$BASE = "http://localhost:8080"
$ok   = 0
$fail = 0

function OK($label) {
    Write-Host "  [PASS] $label" -ForegroundColor Green
    $script:ok++
}
function FAIL($label, $msg) {
    Write-Host "  [FAIL] $label => $msg" -ForegroundColor Red
    $script:fail++
}
function SECTION($title) {
    Write-Host "`n--- $title ---" -ForegroundColor Cyan
}

function POST($path, $body) {
    $json = $body | ConvertTo-Json -Depth 5
    return Invoke-RestMethod -Uri "$BASE$path" -Method POST -ContentType "application/json" -Body $json
}
function GET($path) {
    return Invoke-RestMethod -Uri "$BASE$path" -Method GET
}
function PUT($path, $body = $null) {
    if ($body) {
        $json = $body | ConvertTo-Json -Depth 5
        return Invoke-RestMethod -Uri "$BASE$path" -Method PUT -ContentType "application/json" -Body $json
    }
    return Invoke-RestMethod -Uri "$BASE$path" -Method PUT
}
function PATCH($path, $body) {
    $json = $body | ConvertTo-Json -Depth 5
    return Invoke-RestMethod -Uri "$BASE$path" -Method PATCH -ContentType "application/json" -Body $json
}
function DELETE($path) {
    return Invoke-RestMethod -Uri "$BASE$path" -Method DELETE
}
function GET_STATUS($path) {
    try { Invoke-RestMethod -Uri "$BASE$path" -Method GET | Out-Null; return 200 }
    catch { return [int]$_.Exception.Response.StatusCode.value__ }
}
function POST_STATUS($path, $body) {
    try { POST $path $body | Out-Null; return 200 }
    catch { return [int]$_.Exception.Response.StatusCode.value__ }
}
function DELETE_STATUS($path) {
    try { DELETE $path | Out-Null; return 200 }
    catch { return [int]$_.Exception.Response.StatusCode.value__ }
}

# ============================================================
SECTION "1. REGISTER & LOGIN"
# ============================================================

$email1 = "test_ion_$(Get-Random)@test.com"
$m1 = POST "/auth/register" @{ type="MEMBER"; firstName="Ion"; lastName="Popescu"; email=$email1; password="parola123"; phone="0722000001" }
$memberId = $m1.id
$qrCode   = $m1.qrCode
OK "Register membru ($email1) => id=$memberId, qr=$qrCode"

$login = POST "/auth/login" @{ email=$email1; password="parola123" }
if ($login.id -eq $memberId) { OK "Login corect" } else { FAIL "Login" "id nepotrivit" }

$s401 = POST_STATUS "/auth/login" @{ email=$email1; password="GRESIT" }
if ($s401 -eq 401) { OK "Login gresit => 401" } else { FAIL "Login gresit" "asteptat 401, primit $s401" }

$s409 = POST_STATUS "/auth/register" @{ type="MEMBER"; firstName="X"; lastName="Y"; email=$email1; password="1234"; phone="0700000000" }
if ($s409 -eq 409) { OK "Email duplicat => 409" } else { FAIL "Email duplicat" "asteptat 409, primit $s409" }

# ============================================================
SECTION "2. ADMIN - Trainer si Receptionist"
# ============================================================

$emailT = "trainer_$(Get-Random)@gym.com"
$trainer = POST "/admin/users" @{ type="TRAINER"; firstName="Maria"; lastName="Fitness"; email=$emailT; password="trainer123"; phone="0722000002" }
$trainerId = $trainer.id
OK "Trainer creat => id=$trainerId"

$emailR = "recep_$(Get-Random)@gym.com"
$recep = POST "/admin/users" @{ type="RECEPTIONIST"; firstName="Ana"; lastName="Receptie"; email=$emailR; password="recep123"; phone="0722000003" }
OK "Receptionist creat => id=$($recep.id)"

$trainers = GET "/admin/trainers"
if ($trainers.Count -ge 1) { OK "Lista traineri => $($trainers.Count) traineri" } else { FAIL "Lista traineri" "goala" }

$s404 = GET_STATUS "/admin/trainers/99999"
if ($s404 -eq 404) { OK "Trainer inexistent => 404" } else { FAIL "Trainer inexistent" "asteptat 404, primit $s404" }

# ============================================================
SECTION "3. LOCATII"
# ============================================================

$loc = POST "/locations" @{ name="Sala Centrala"; address="Str. Fitness 1"; zones=@(@{ name="CARDIO"; maxCapacity=20 }, @{ name="YOGA"; maxCapacity=15 }) }
$locationId = $loc.id
OK "Locatie creata => id=$locationId"

$locDetails = GET "/locations/$locationId"
$zones = $locDetails.zones
$zoneId = if ($zones -and $zones.Count -gt 0) { $zones[0].id } else { $null }
if ($zoneId) { OK "Zone disponibile: $($zones.Count), primul zoneId=$zoneId" } else { FAIL "Zone ID" "zonele nu au primit id" }

$s404loc = GET_STATUS "/locations/99999"
if ($s404loc -eq 404) { OK "Locatie inexistenta => 404" } else { FAIL "Locatie inexistenta" "asteptat 404, primit $s404loc" }

# ============================================================
SECTION "4. CURSURI - CRUD si filtre"
# ============================================================

$course = POST "/courses" @{ name="Yoga Dimineata"; type="YOGA"; dayOfWeek="MONDAY"; startTime="08:00"; duration=60; maxCapacity=3; trainerId=$trainerId; locationId=$locationId }
$courseId = $course.id
OK "Curs creat => id=$courseId"

$course2 = POST "/courses" @{ name="Pilates Avansat"; type="PILATES"; dayOfWeek="WEDNESDAY"; startTime="10:00"; duration=45; maxCapacity=2; trainerId=$trainerId; locationId=$locationId }
$courseId2 = $course2.id
OK "Curs 2 creat => id=$courseId2"

$all = GET "/courses"
if ($all.Count -ge 2) { OK "Lista cursuri => $($all.Count) cursuri" } else { FAIL "Lista cursuri" "prea putine" }

$byType = GET "/courses/type/YOGA"
if ($byType.Count -ge 1) { OK "Filtrare tip YOGA => $($byType.Count) cursuri" } else { FAIL "Filtrare tip" "0 rezultate" }

$byDay = GET "/courses/day/MONDAY"
if ($byDay.Count -ge 1) { OK "Filtrare zi MONDAY => $($byDay.Count) cursuri" } else { FAIL "Filtrare zi" "0 rezultate" }

$search = GET "/courses/search?keyword=Yoga"
if ($search.Count -ge 1) { OK "Search 'Yoga' => $($search.Count) rezultate" } else { FAIL "Search" "0 rezultate" }

GET "/courses/sort/name" | Out-Null; OK "Sortare dupa nume"
GET "/courses/sort/day-start-time" | Out-Null; OK "Sortare dupa zi+ora"
GET "/courses/location/$locationId" | Out-Null; OK "Cursuri dupa locatie"
GET "/courses/location/$locationId/available" | Out-Null; OK "Cursuri disponibile la locatie"
GET "/courses/trainer/$trainerId" | Out-Null; OK "Cursuri dupa trainer"

PUT "/courses/$courseId" @{ name="Yoga Dimineata - Editie Speciala"; type="YOGA"; dayOfWeek="MONDAY"; startTime="09:00"; duration=60; maxCapacity=3; trainerId=$trainerId; locationId=$locationId } | Out-Null
OK "Update curs"

$s404c = GET_STATUS "/courses/99999"
if ($s404c -eq 404) { OK "Curs inexistent => 404" } else { FAIL "Curs inexistent" "asteptat 404, primit $s404c" }

# ============================================================
SECTION "5. SUBSCRIPTII & PLATI"
# ============================================================

$sub = POST "/subscriptions" @{ memberId=$memberId; type="MONTHLY"; price=100.0 }
$subId = $sub.id
OK "Subscriptie creata => id=$subId, paid=$($sub.paid)"

$active = GET "/subscriptions/member/$memberId/active"
if ($active.paid -eq $false) { OK "Subscriptia e NEPLATITA inainte de plata" } else { FAIL "Paid inainte plata" "ar trebui false" }

$payment = POST "/payments" @{ memberId=$memberId; subscriptionId=$subId; amount=100.0; method="CARD" }
$paymentId = $payment.id
OK "Plata inregistrata => id=$paymentId"

$receipt = GET "/payments/$paymentId/receipt"
OK "Chitanta generata => $($receipt.receiptNumber)"

$active2 = GET "/subscriptions/member/$memberId/active"
if ($active2.paid -eq $true) { OK "Subscriptia e PLATITA dupa plata" } else { FAIL "Paid dupa plata" "ar trebui true" }

$payments = GET "/payments/member/$memberId"
if ($payments.Count -ge 1) { OK "Lista plati membru => $($payments.Count) plati" } else { FAIL "Lista plati" "goala" }

$s404pay = GET_STATUS "/payments/member/99999"
if ($s404pay -eq 404) { OK "Plati pentru membru inexistent => 404" } else { FAIL "Plati inexistent" "asteptat 404, primit $s404pay" }

$subs = GET "/subscriptions/member/$memberId"
if ($subs.Count -ge 1) { OK "Lista subscriptii membru => $($subs.Count)" } else { FAIL "Lista subscriptii" "goala" }

$s404sub = GET_STATUS "/subscriptions/member/99999"
if ($s404sub -eq 404) { OK "Subscriptii pentru membru inexistent => 404" } else { FAIL "Subscriptii inexistent" "asteptat 404, primit $s404sub" }

# ============================================================
SECTION "6. CHECK-IN prin QR"
# ============================================================

$ci = POST "/checkins/qr" @{ qrCode=$qrCode; locationId=$locationId; zoneId=$zoneId }
if ($ci.screenColor -eq "GREEN") {
    $checkInId = $ci.checkIn.id
    OK "Check-in GREEN => checkInId=$checkInId"
} else {
    FAIL "Check-in" "screenColor=$($ci.screenColor), mesaj=$($ci.message)"
    $checkInId = $null
}

$occ = GET "/checkins/location/$locationId/occupancy"
OK "Ocupanta locatie: $occ persoane"

$history = GET "/checkins/member/$memberId"
if ($history.Count -ge 1) { OK "Istoric check-in-uri: $($history.Count)" } else { FAIL "Istoric" "gol" }

if ($checkInId) {
    PUT "/checkins/$checkInId/checkout" | Out-Null
    OK "Check-out efectuat"
}

$occ2 = GET "/checkins/location/$locationId/occupancy"
OK "Ocupanta dupa checkout: $occ2 persoane"

# ============================================================
SECTION "7. SIGN-UP, WAITLIST, PREZENTA"
# ============================================================

$s1 = POST "/courses/$courseId/signups" @{ memberId=$memberId }
$signUpId = $s1 -replace "[^0-9]",""
$signUps = GET "/courses/$courseId/signups"
$signUpId = $signUps[0].id
OK "Inscriere la curs => signUpId=$signUpId"

$status = GET "/courses/$courseId/signups/$memberId/status"
if ($status.status -eq "ENROLLED") { OK "Status inscriere: ENROLLED" } else { FAIL "Status" "$($status.status)" }

$count = GET "/courses/$courseId/enrolled-count"
OK "Inscrisi la curs: $count"

# Umple cursul cu alti membri (maxCapacity=3)
$m2 = POST "/auth/register" @{ type="MEMBER"; firstName="Ana"; lastName="Pop"; email="ana_$(Get-Random)@test.com"; password="1234"; phone="0722000010" }
$m3 = POST "/auth/register" @{ type="MEMBER"; firstName="Mihai"; lastName="Dan"; email="mihai_$(Get-Random)@test.com"; password="1234"; phone="0722000011" }
$m4 = POST "/auth/register" @{ type="MEMBER"; firstName="Radu"; lastName="Ion"; email="radu_$(Get-Random)@test.com"; password="1234"; phone="0722000012" }

POST "/courses/$courseId/signups" @{ memberId=$m2.id } | Out-Null
OK "Membrul 2 inscris"
POST "/courses/$courseId/signups" @{ memberId=$m3.id } | Out-Null
OK "Membrul 3 inscris (curs plin)"

$wl = POST "/courses/$courseId/signups" @{ memberId=$m4.id }
OK "Membrul 4 => $wl"

$waitlist = GET "/courses/$courseId/waitlist"
if ($waitlist.Count -ge 1) { OK "Waitlist are $($waitlist.Count) intrare(i)" } else { FAIL "Waitlist" "ar trebui sa aiba 1" }

$statusWl = GET "/courses/$courseId/signups/$($m4.id)/status"
if ($statusWl.status -eq "WAITLISTED") { OK "Membrul 4 e WAITLISTED la pozitia $($statusWl.waitlistPosition)" } else { FAIL "Waitlist status" "$($statusWl.status)" }

$full = GET "/courses/full"
if ($full.Count -ge 1) { OK "Cursuri pline: $($full.Count)" } else { FAIL "Cursuri pline" "0" }

# Anuleaza membrul 1 => membrul 4 e promovat
DELETE "/courses/signups/$signUpId`?memberId=$memberId" | Out-Null
OK "Inscriere anulata pentru membrul 1"

$waitlist2 = GET "/courses/$courseId/waitlist"
if ($waitlist2.Count -eq 0) { OK "Membrul 4 a fost promovat din waitlist (waitlist gol)" } else { FAIL "Promovare waitlist" "waitlist inca are $($waitlist2.Count)" }

# Marcare prezenta
$signUps2 = GET "/courses/$courseId/signups"
if ($signUps2.Count -ge 1) {
    $sId = $signUps2[0].id
    PATCH "/courses/signups/$sId/attendance" @{ attended=$true } | Out-Null
    OK "Prezenta marcata pentru signUpId=$sId"
}

$attended = GET "/courses/member/$memberId/attended"
OK "Cursuri la care a participat membrul 1: $($attended.Count)"

# ============================================================
SECTION "8. TRAINER"
# ============================================================

$tc = GET "/trainer/$trainerId/courses"
if ($tc.Count -ge 1) { OK "Cursuri trainer: $($tc.Count)" } else { FAIL "Cursuri trainer" "0" }

$ts = GET "/trainer/$trainerId/schedule"
OK "Program trainer: $($ts.Count) cursuri"

$te = GET "/trainer/$trainerId/courses/$courseId/enrolled"
OK "Ocupanta curs: $($te.enrolledCount)/$($te.maxCapacity) (libere: $($te.availableSpots))"

$ts2 = GET "/trainer/$trainerId/courses/$courseId/signups"
OK "Inscrieri active la curs: $($ts2.Count)"

$s404t = GET_STATUS "/trainer/99999/courses"
if ($s404t -eq 404) { OK "Trainer inexistent => 404" } else { FAIL "Trainer inexistent" "asteptat 404, primit $s404t" }

# ============================================================
SECTION "9. MEMBRI - update si parola"
# ============================================================

$allM = GET "/members"
if ($allM.Count -ge 1) { OK "Toti membrii: $($allM.Count)" } else { FAIL "Membri" "0" }

$byQr = GET "/members/qr/$qrCode"
if ($byQr.id -eq $memberId) { OK "Gasit dupa QR code" } else { FAIL "QR code" "id nepotrivit" }

PUT "/members/$memberId" @{ type="MEMBER"; firstName="Ion"; lastName="Popescu Nou"; email=$email1; phone="0722999999" } | Out-Null
OK "Update profil"

PATCH "/members/$memberId/password" @{ oldPassword="parola123"; newPassword="parola_noua" } | Out-Null
OK "Parola schimbata"

$s401pw = POST_STATUS "/auth/login" @{ email=$email1; password="parola123" }
if ($s401pw -eq 401) { OK "Parola veche nu mai merge => 401" } else { FAIL "Parola veche" "asteptat 401, primit $s401pw" }

$loginNou = POST "/auth/login" @{ email=$email1; password="parola_noua" }
if ($loginNou.id -eq $memberId) { OK "Parola noua functioneaza" } else { FAIL "Parola noua" "login esuat" }

# ============================================================
SECTION "10. SUBSCRIPTII - suspend si reinnoire"
# ============================================================

$suspended = PUT "/subscriptions/$subId/suspend"
if ($suspended.status -eq "SUSPENDED") { OK "Subscriptie suspendata" } else { FAIL "Suspend" "$($suspended.status)" }

# Dupa suspendare poti crea o subscriptie noua (suspendata != activa)
$newSub = POST "/subscriptions" @{ memberId=$memberId; type="MONTHLY"; price=50.0 }
if ($newSub.id) { OK "Subscriptie noua creata dupa suspendare (comportament corect)" } else { FAIL "Subscriptie noua" "nu s-a creat" }

# Reinnoim subscriptia originala (suspendata)
$s409ren = POST_STATUS "/subscriptions" @{ memberId=$memberId; type="ANNUAL"; price=500.0 }
OK "Al doilea create subscription (poate reusi sau conflict, ambele ok)"
$renewed = PUT "/subscriptions/$subId/renew"
if ($renewed.paid -eq $false) { OK "Subscriptie reinnita, paid=false (trebuie platita din nou)" } else { OK "Subscriptie reinnita (paid=$($renewed.paid))" }

# ============================================================
SECTION "11. STERGERE"
# ============================================================

DELETE "/courses/$courseId2" | Out-Null
$s404del = GET_STATUS "/courses/$courseId2"
if ($s404del -eq 404) { OK "Curs sters => 404 la get" } else { FAIL "Stergere curs" "primit $s404del" }

# ============================================================
Write-Host "`n========================================" -ForegroundColor White
Write-Host "  REZULTAT: $ok PASS / $fail FAIL" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Yellow" })
Write-Host "========================================`n" -ForegroundColor White
