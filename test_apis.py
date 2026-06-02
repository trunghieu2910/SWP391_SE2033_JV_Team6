import urllib.request
import urllib.error
import json
import time

base_url = "http://localhost:8080"

# Mảng lưu thông tin token
tokens = {
    "admin": None,
    "doctor": None,
    "patient": None
}

def authenticate():
    # Login admin (giả định)
    req = urllib.request.Request(base_url + "/api/auth/login", method="POST")
    req.add_header('Content-Type', 'application/json')
    try:
        res = urllib.request.urlopen(req, data=json.dumps({"login": "namvipnhatgt@gmail.com", "password": "123456"}).encode('utf-8'))
        if res.getcode() == 200:
            tokens["admin"] = json.loads(res.read())["accessToken"]
    except: pass

    # Login patient
    try:
        res = urllib.request.urlopen(req, data=json.dumps({"login": "patient_nam", "password": "123456"}).encode('utf-8'))
        if res.getcode() == 200:
            tokens["patient"] = json.loads(res.read())["accessToken"]
    except: pass

authenticate()

tests = [
    # AUTHENTICATION
    {"id": "TC_AUTH_01", "name": "User registration (valid)", "method": "POST", "url": "/api/auth/register", "body": { "fullName": "Nguyen Thi Phuong Hoa", "gender": "nu", "dob": "2005-09-21", "address": "hn", "nationalID": str(int(time.time())), "phoneNumber": "09" + str(int(time.time()))[-8:], "userName": "hoa_" + str(int(time.time())), "password": "Hofv123@", "email": str(int(time.time())) + "@gmail.com" }, "expected": 200},
    {"id": "TC_AUTH_02", "name": "Register missing email", "method": "POST", "url": "/api/auth/register", "body": { "fullName": "Hoa", "gender": "nu", "dob": "2005-09-21", "address": "hn", "nationalID": "458909098709", "phoneNumber": "0912345678", "userName": "hoa2", "password": "Hofv123@", "email": "" }, "expected": 400},
    {"id": "TC_AUTH_03", "name": "Register missing phone", "method": "POST", "url": "/api/auth/register", "body": { "fullName": "Hoa", "gender": "nu", "dob": "2005-09-21", "address": "hn", "nationalID": "458909098709", "phoneNumber": "", "userName": "hoa2", "password": "Hofv123@", "email": "a@gmail.com" }, "expected": 400},
    {"id": "TC_AUTH_04", "name": "Register missing userName", "method": "POST", "url": "/api/auth/register", "body": { "fullName": "Hoa", "gender": "nu", "dob": "2005-09-21", "address": "hn", "nationalID": "458909098709", "phoneNumber": "0123456789", "userName": "", "password": "Hofv123@", "email": "a@gmail.com" }, "expected": 400},
    {"id": "TC_AUTH_22", "name": "Login valid email", "method": "POST", "url": "/api/auth/login", "body": {"login": "namvipnhatgt@gmail.com", "password": "123456"}, "expected": 200},
    {"id": "TC_AUTH_26", "name": "Login invalid password", "method": "POST", "url": "/api/auth/login", "body": {"login": "namvipnhatgt@gmail.com", "password": "wrong_password"}, "expected": 401},
    {"id": "TC_AUTH_29", "name": "Forgot password", "method": "POST", "url": "/api/auth/forgot-password", "body": {"email":"namvipnhatgt@gmail.com"}, "expected": 200},
    
    # ADMIN
    {"id": "TC_ADM_DASH_001", "name": "Admin Dashboard", "method": "GET", "url": "/api/admin/dashboard", "auth": "admin", "expected": 200},
    {"id": "TC_ADM_USER_001", "name": "Get Users", "method": "GET", "url": "/api/admin/users?page=0&size=10", "auth": "admin", "expected": 200},
    {"id": "TC_ADM_DETAIL_001", "name": "Get User Detail", "method": "GET", "url": "/api/admin/users/1", "auth": "admin", "expected": 200},
    {"id": "TC_ADM_DETAIL_002", "name": "Get User Detail 9999", "method": "GET", "url": "/api/admin/users/9999", "auth": "admin", "expected": 404},
    {"id": "TC_LOG_001", "name": "Get Logs", "method": "GET", "url": "/api/admin/logs?page=0&size=10", "auth": "admin", "expected": 200},
    
    # PROFILE
    {"id": "TC_PROFILE_GET_004", "name": "Get Profile no auth", "method": "GET", "url": "/api/profile", "expected": 401},
    {"id": "TC_PROFILE_GET_001", "name": "Get Profile admin", "method": "GET", "url": "/api/profile", "auth": "admin", "expected": 200},
    
    # MEDICAL RECORDS
    {"id": "TC_MED_033", "name": "Get all records", "method": "GET", "url": "/api/medical-records", "auth": "admin", "expected": 200},
    {"id": "TC_MED_035", "name": "Get records by patientId", "method": "GET", "url": "/api/medical-records/patient/1", "auth": "admin", "expected": 200},
    {"id": "TC_MED_044", "name": "Get detail missing", "method": "GET", "url": "/api/medical-records/detail/9999", "auth": "admin", "expected": 404}
]

print(f"{'ID':<15} | {'NAME':<35} | {'EXPECTED':<8} | {'ACTUAL':<6} | {'RESULT'}")
print("-" * 80)

for test in tests:
    url = base_url + test["url"]
    req = urllib.request.Request(url, method=test["method"])
    req.add_header('Content-Type', 'application/json')
    
    if "auth" in test and test["auth"] and tokens[test["auth"]]:
        req.add_header('Authorization', f'Bearer {tokens[test["auth"]]}')
    
    data = None
    if "body" in test and test["body"]:
        data = json.dumps(test["body"]).encode('utf-8')
        
    try:
        response = urllib.request.urlopen(req, data=data)
        status = response.getcode()
        body = response.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        status = e.code
        body = e.read().decode('utf-8')
    except Exception as e:
        status = 0
        body = str(e)
        
    passed = status == test["expected"]
    res_text = "PASS" if passed else "FAIL"
    print(f"{test['id']:<15} | {test['name']:<35} | {test['expected']:<8} | {status:<6} | {res_text}")
    
    if not passed:
        print(f"   -> Body: {body[:200]}")
