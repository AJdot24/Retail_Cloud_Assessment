# API JSON Schemas

Request and response JSON schemas for all endpoints of the Employee Management System.
Formats: dates are ISO-8601 strings (`yyyy-MM-dd`), money is a decimal number (JSON number).

---

## EmployeeRequest (POST /api/employees, PUT /api/employees/{id})

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "EmployeeRequest",
  "type": "object",
  "additionalProperties": false,
  "required": ["name", "dateOfBirth", "salary", "departmentId", "address", "role", "joiningDate", "yearlyBonusPercentage"],
  "properties": {
    "name":                   { "type": "string", "minLength": 1, "maxLength": 120 },
    "dateOfBirth":            { "type": "string", "format": "date" },
    "salary":                 { "type": "number", "exclusiveMinimum": 0 },
    "departmentId":           { "type": "integer", "minimum": 1 },
    "address":                { "type": "string", "minLength": 1, "maxLength": 255 },
    "role":                   { "type": "string", "minLength": 1, "maxLength": 100 },
    "joiningDate":            { "type": "string", "format": "date" },
    "yearlyBonusPercentage":  { "type": "number", "minimum": 0, "maximum": 100 },
    "reportingManagerId":     { "type": "integer", "minimum": 1 }
  }
}
```

Example:

```json
{
  "name": "Neha Krishnan",
  "dateOfBirth": "1996-04-22",
  "salary": 950000.00,
  "departmentId": 1,
  "address": "Mumbai, Maharashtra",
  "role": "Software Engineer",
  "joiningDate": "2025-02-01",
  "yearlyBonusPercentage": 9.0,
  "reportingManagerId": 5
}
```

`reportingManagerId` is optional: it must be omitted only for the single top-level employee
(creating a second top-level employee returns 409).

---

## EmployeeResponse (returned by employee endpoints)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "EmployeeResponse",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "id", "name", "dateOfBirth", "salary", "departmentId", "departmentName",
    "address", "role", "joiningDate", "yearlyBonusPercentage",
    "reportingManagerId", "reportingManagerName"
  ],
  "properties": {
    "id":                       { "type": "integer" },
    "name":                     { "type": "string" },
    "dateOfBirth":              { "type": "string", "format": "date" },
    "salary":                   { "type": "number" },
    "departmentId":             { "type": "integer" },
    "departmentName":           { "type": "string" },
    "address":                  { "type": "string" },
    "role":                     { "type": "string" },
    "joiningDate":              { "type": "string", "format": "date" },
    "yearlyBonusPercentage":    { "type": "number" },
    "reportingManagerId":       { "type": ["integer", "null"] },
    "reportingManagerName":     { "type": ["string", "null"] }
  }
}
```

`reportingManagerId` / `reportingManagerName` are `null` for the top-level employee.

---

## DepartmentRequest (POST /api/departments, PUT /api/departments/{id})

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "DepartmentRequest",
  "type": "object",
  "additionalProperties": false,
  "required": ["name"],
  "properties": {
    "name":              { "type": "string", "minLength": 1, "maxLength": 100 },
    "creationDate":      { "type": "string", "format": "date" },
    "departmentHeadId":  { "type": "integer", "minimum": 1 }
  }
}
```

- `creationDate` defaults to today when omitted.
- `departmentHeadId` is optional (the head is linked once the employee exists;
  passing `null` on update clears the head).

---

## DepartmentResponse (returned by department endpoints)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "DepartmentResponse",
  "type": "object",
  "additionalProperties": false,
  "required": ["id", "name", "creationDate", "departmentHeadId", "departmentHeadName", "employeeCount", "employees"],
  "properties": {
    "id":                 { "type": "integer" },
    "name":               { "type": "string" },
    "creationDate":       { "type": "string", "format": "date" },
    "departmentHeadId":   { "type": ["integer", "null"] },
    "departmentHeadName": { "type": ["string", "null"] },
    "employeeCount":      { "type": "integer" },
    "employees": {
      "description": "Present only when expand=employee was passed; otherwise null",
      "type": ["array", "null"],
      "items": { "$ref": "#/definitions/employee" }
    }
  }
}
```

---

## PageResponse (envelope of every list endpoint)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "PageResponse",
  "type": "object",
  "additionalProperties": false,
  "required": ["content", "page", "size", "totalElements", "totalPages", "first", "last"],
  "properties": {
    "content":        { "type": "array" },
    "page":           { "type": "integer", "description": "zero-based current page" },
    "size":           { "type": "integer", "description": "items per page (default 20)" },
    "totalElements":  { "type": "integer" },
    "totalPages":     { "type": "integer" },
    "first":          { "type": "boolean" },
    "last":           { "type": "boolean" }
  }
}
```

Example (`GET /api/employees?lookup=true`):

```json
{
  "content": [
    { "id": 1, "name": "Aarav Sharma" },
    { "id": 2, "name": "Priya Nair" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 25,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

---

## Reporting chain (GET /api/employees/{id}/reporting-chain)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ReportingChain",
  "type": "array",
  "items": {
    "type": "object",
    "additionalProperties": false,
    "required": ["id", "name", "role"],
    "properties": {
      "id":   { "type": "integer" },
      "name": { "type": "string" },
      "role": { "type": "string" }
    }
  }
}
```

Example (`GET /api/employees/5/reporting-chain`):

```json
[
  { "id": 5,  "name": "Vikram Reddy",  "role": "Senior Software Engineer" },
  { "id": 2,  "name": "Priya Nair",    "role": "Engineering Manager" },
  { "id": 1,  "name": "Aarav Sharma",  "role": "Chief Executive Officer" }
]
```

---

## Analytics (GET /api/analytics/departments)

Paginated `PageResponse` whose `content` items are:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "DepartmentAnalytics",
  "type": "object",
  "additionalProperties": false,
  "required": ["id", "name", "headcount", "averageSalary", "totalSalary", "totalYearlyBonus"],
  "properties": {
    "id":               { "type": "integer" },
    "name":             { "type": "string" },
    "headcount":        { "type": "integer" },
    "averageSalary":    { "type": "number" },
    "totalSalary":      { "type": "number" },
    "totalYearlyBonus": { "type": "number" }
  }
}
```

`totalYearlyBonus` = Σ (salary × yearlyBonusPercentage ÷ 100).

---

## ApiError (uniform error body)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ApiError",
  "type": "object",
  "additionalProperties": false,
  "required": ["timestamp", "status", "error", "message", "path", "fieldErrors"],
  "properties": {
    "timestamp":    { "type": "string", "format": "date-time" },
    "status":       { "type": "integer" },
    "error":        { "type": "string" },
    "message":      { "type": "string" },
    "path":         { "type": "string" },
    "fieldErrors":  { "type": ["object", "null"], "additionalProperties": { "type": "string" } }
  }
}
```

`fieldErrors` is populated only for 400 validation failures (field name → reason).

---

## HTTP status usage

| Method/Path                                   | Success          | Errors                                                                 |
|-----------------------------------------------|------------------|------------------------------------------------------------------------|
| POST `/api/employees`                         | 201              | 400 invalid body, 404 unknown dept/manager, 409 second top-level employee |
| PUT `/api/employees/{id}`                     | 200              | 400, 404, 409 (self manager, circular chain, second top-level)         |
| PATCH `/api/employees/{id}/department/{id}`   | 200              | 404 unknown employee/department                                        |
| GET `/api/employees`                          | 200              | —                                                                      |
| GET `/api/employees/{id}`                     | 200              | 404                                                                    |
| GET `/api/employees/{id}/reporting-chain`     | 200              | 404, 409 (corrupt circular data)                                       |
| POST `/api/departments`                       | 201              | 400, 404 unknown head, 409 duplicate name                              |
| PUT `/api/departments/{id}`                   | 200              | 400, 404, 409 duplicate name                                           |
| DELETE `/api/departments/{id}`                | 204              | 404, **409 employees still assigned**                                  |
| GET `/api/departments`                        | 200              | —                                                                      |
| GET `/api/departments/{id}`                   | 200              | 404                                                                    |
| GET `/api/analytics/departments`              | 200              | —                                                                      |
