$ErrorActionPreference='SilentlyContinue'
$base="http://localhost:8080/api"
$pass=0; $fail=0; $failures=@()

function T($m,$u,$b,$t){
  $h=@{'Content-Type'='application/json'}; if($t){$h['Authorization']="Bearer $t"}
  try{$r=Invoke-WebRequest -Method $m -Uri $u -Headers $h -Body $b -UseBasicParsing -TimeoutSec 15
      return @{c=[int]$r.StatusCode;b=$r.Content}}
  catch{
      $body=$null
      try{ $st=$_.Exception.Response.GetResponseStream(); $rd=New-Object IO.StreamReader($st); $body=$rd.ReadToEnd() }catch{}
      return @{c=[int]$_.Exception.Response.StatusCode.value__;b=$body}
  }
}
function Chk($name,$expected,$actual){
  if($expected -eq $actual){ $script:pass++; "  ok   $name ($actual)" }
  else { $script:fail++; $script:failures+="${name}: expected $expected got $actual"; "  FAIL $name (expected $expected got $actual)" }
}
function Login($u,$p){ ((T POST "$base/auth/login" "{`"username`":`"$u`",`"password`":`"$p`"}" $null).b|ConvertFrom-Json).token }

"########## PHASE 11 FULL INTEGRATION ##########"
""
"=== A. AUTHENTICATION (all three roles log in) ==="
$adm=Login admin Password123
$smith=Login t.smith Password123
$khan=Login t.khan Password123
$s1=Login cs.student1 Password123
Chk "admin login"    $true ($adm.Length -gt 20)
Chk "teacher login"  $true ($smith.Length -gt 20)
Chk "student login"  $true ($s1.Length -gt 20)
Chk "bad password"   401 (T POST "$base/auth/login" '{"username":"admin","password":"nope"}' $null).c
Chk "no token"       401 (T GET "$base/users" $null $null).c
Chk "tampered token" 401 (T GET "$base/auth/me" $null ($adm.Substring(0,$adm.Length-4)+'AAAA')).c

"=== B. AUTHORIZATION MATRIX (the critical set) ==="
$stu1=((T GET "$base/students?search=CS2023001" $null $adm).b|ConvertFrom-Json).content[0].id
$stu2=((T GET "$base/students?search=CS2023002" $null $adm).b|ConvertFrom-Json).content[0].id
$csCourse=((T GET "$base/courses?search=Database" $null $adm).b|ConvertFrom-Json).content[0].id
$e1=((T GET "$base/enrollments/student/$stu1" $null $adm).b|ConvertFrom-Json)[0]
$enr1=$e1.id
Chk "student->POST marks (403)"        403 (T POST "$base/marks" "{`"enrollmentId`":$enr1,`"assessmentId`":1,`"marksObtained`":5}" $s1).c
Chk "student->POST attendance (403)"   403 (T POST "$base/attendance" "{`"enrollmentId`":$enr1,`"attendanceDate`":`"2026-07-20`",`"status`":`"PRESENT`"}" $s1).c
Chk "student->admin route (403)"       403 (T GET "$base/users" $null $s1).c
Chk "student reads other student (403)" 403 (T GET "$base/attendance/student/$stu2" $null $s1).c
Chk "student reads other transcript (403)" 403 (T GET "$base/transcripts/student/$stu2" $null $s1).c
Chk "teacherB on A's course marks (403)" 403 (T GET "$base/marks/course/$csCourse" $null $khan).c
Chk "teacherB on A's attendance (403)"   403 (T GET "$base/attendance/summary/course/$csCourse" $null $khan).c

"=== C. CRUD round-trip (create->read->update->delete) ==="
$dep=T POST "$base/departments" '{"name":"Integration Test Dept","code":"ITD"}' $adm
Chk "create dept (201)" 201 $dep.c
$depId=($dep.b|ConvertFrom-Json).id
Chk "read dept (200)"   200 (T GET "$base/departments/$depId" $null $adm).c
Chk "update dept (200)" 200 (T PUT "$base/departments/$depId" '{"name":"Integration Test Dept 2","code":"ITD"}' $adm).c
Chk "dup code (409)"    409 (T POST "$base/departments" '{"name":"Another","code":"CS"}' $adm).c
Chk "delete empty dept (204)" 204 (T DELETE "$base/departments/$depId" $null $adm).c
Chk "read deleted (404)" 404 (T GET "$base/departments/$depId" $null $adm).c

"=== D. SEARCH / FILTER / PAGINATION ==="
Chk "students paginated (200)" 200 (T GET "$base/students?page=0&size=5" $null $adm).c
$p=(T GET "$base/students?page=0&size=5" $null $adm).b|ConvertFrom-Json
Chk "page size respected"      5 $p.size
Chk "search filter (200)"      200 (T GET "$base/students?search=Amina" $null $adm).c
$sr=(T GET "$base/students?search=Amina" $null $adm).b|ConvertFrom-Json
Chk "search finds Amina"       1 $sr.totalElements
Chk "course dept filter (200)" 200 (T GET "$base/courses?departmentId=1&semester=3" $null $adm).c

"=== E. ATTENDANCE end-to-end ==="
Chk "percentage (200)"     200 (T GET "$base/attendance/percentage?studentId=$stu1&courseId=$csCourse" $null $adm).c
$pct=(T GET "$base/attendance/percentage?studentId=$stu1&courseId=$csCourse" $null $adm).b|ConvertFrom-Json
Chk "student1 att = 90pct"  "90.00" $pct.percentage
Chk "course summary (200)" 200 (T GET "$base/attendance/summary/course/$csCourse" $null $smith).c
Chk "low attendance (200)" 200 (T GET "$base/attendance/low" $null $adm).c
$low=(T GET "$base/attendance/low" $null $adm).b|ConvertFrom-Json
Chk "2 students flagged low" 2 $low.Count

"=== F. MARKS & GRADES end-to-end ==="
Chk "grades for student (200)" 200 (T GET "$base/grades/student/$stu1" $null $adm).c
Chk "gpa (200)"                200 (T GET "$base/grades/gpa/$stu1" $null $adm).c
$gpa=(T GET "$base/grades/gpa/$stu1" $null $adm).b|ConvertFrom-Json
Chk "student1 GPA = 4.00"     "4.00" $gpa.gpa

"=== G. TRANSCRIPT ==="
Chk "own transcript (200)"    200 (T GET "$base/transcripts/my" $null $s1).c
$tr=(T GET "$base/transcripts/my" $null $s1).b|ConvertFrom-Json
Chk "transcript has cumGPA"   "4.00" $tr.cumulativeGpa

"=== H. ALL 8 REPORTS ==="
Chk "students-by-dept (200)"       200 (T GET "$base/reports/students-by-department" $null $adm).c
Chk "student-performance (200)"    200 (T GET "$base/reports/student-performance/$stu1" $null $adm).c
Chk "attendance-by-course (200)"   200 (T GET "$base/reports/attendance/course/$csCourse" $null $smith).c
Chk "low-attendance report (200)"  200 (T GET "$base/reports/low-attendance" $null $adm).c
Chk "course-performance (200)"     200 (T GET "$base/reports/course-performance/$csCourse" $null $smith).c
Chk "grade-distribution (200)"     200 (T GET "$base/reports/grade-distribution?courseId=$csCourse" $null $smith).c
Chk "pass-fail (200)"              200 (T GET "$base/reports/pass-fail?courseId=$csCourse" $null $smith).c
$pf=(T GET "$base/reports/pass-fail?courseId=$csCourse" $null $smith).b|ConvertFrom-Json
Chk "pass-fail: 6 passed"          6 $pf.passed
Chk "department-performance (200)" 200 (T GET "$base/reports/department-performance/1" $null $adm).c

"=== I. DASHBOARDS (all three) ==="
Chk "admin dashboard (200)"   200 (T GET "$base/dashboard/admin" $null $adm).c
Chk "teacher dashboard (200)" 200 (T GET "$base/dashboard/teacher" $null $smith).c
Chk "student dashboard (200)" 200 (T GET "$base/dashboard/student" $null $s1).c
Chk "admin on teacher dash (403)"  403 (T GET "$base/dashboard/teacher" $null $adm).c
Chk "student on admin dash (403)"  403 (T GET "$base/dashboard/admin" $null $s1).c

"=== J. VALIDATION & ERROR HANDLING ==="
Chk "validation 400+fields"   400 (T POST "$base/auth/register" '{"username":"ab","email":"bad","password":"1"}' $null).c
$v=(T POST "$base/auth/register" '{"username":"ab","email":"bad","password":"1"}' $null).b|ConvertFrom-Json
Chk "has fieldErrors"         $true ($v.fieldErrors -ne $null)
Chk "missing resource 404"    404 (T GET "$base/courses/999999" $null $adm).c
Chk "self-register admin 400" 400 (T POST "$base/auth/register" '{"username":"hax","email":"h@x.com","password":"Password123","role":"ADMIN"}' $null).c

""
"##############################################"
"RESULT:  PASS=$pass  FAIL=$fail"
if($fail -gt 0){ ""; "FAILURES:"; $failures | ForEach-Object { "  - $_" } }
"##############################################"
