# PocketNews - Quick Reference Card

## 🚀 Quick Start

```bash
# Build
mvn clean install

# Run
java -jar target/PocketNews-1.0-SNAPSHOT.jar

# Server
http://localhost:8080/api
```

---

## 📱 API Quick Reference

### Onboarding
```
GET  /onboarding/languages
POST /onboarding/step1       { deviceId, preferredLanguage }
POST /onboarding/step2       { deviceId, age }
POST /onboarding/step3       { deviceId, preferredCategoryIds[] }
GET  /onboarding/profile/{deviceId}
GET  /onboarding/preferences/{deviceId}
```

### News
```
GET  /news?page=0&size=10
GET  /news/featured
GET  /news/category/{categoryId}
GET  /news/{id}
POST /news
PUT  /news/{id}
DELETE /news/{id}
```

### Comments
```
GET    /news/{newsId}/comments?page=0&size=10
POST   /news/{newsId}/comments?deviceId=X     { content }
PUT    /news/{newsId}/comments/{commentId}
DELETE /news/{newsId}/comments/{commentId}
```

### Likes & Bookmarks
```
GET  /news/{newsId}/likes?deviceId=X
POST /news/{newsId}/likes?deviceId=X
GET  /news/{newsId}/bookmarks?deviceId=X
POST /news/{newsId}/bookmarks?deviceId=X
GET  /user/bookmarks?deviceId=X&page=0&size=10
```

### Health
```
GET /db
```

---

## 🔑 Key Points

| Aspect | Details |
|--------|---------|
| **Auth** | NONE - All endpoints public |
| **User ID** | deviceId (UUID string) |
| **Headers** | No Authorization needed |
| **Format** | JSON (application/json) |
| **Comments** | Show userUUID (device identifier) |
| **News TTL** | 5 days (auto-delete) |
| **Cleanup Task** | Hourly via scheduler |

---

## 📊 Request Examples

### Create Comment
```bash
curl -X POST \
  "http://localhost:8080/api/news/1/comments?deviceId=device-abc-123" \
  -H "Content-Type: application/json" \
  -d '{"content": "Great news!"}'
```

### Like a News
```bash
curl -X POST \
  "http://localhost:8080/api/news/1/likes?deviceId=device-abc-123"
```

### Complete Onboarding Step 1
```bash
curl -X POST \
  "http://localhost:8080/api/onboarding/step1" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "device-abc-123",
    "preferredLanguage": "hi"
  }'
```

---

## 🌍 Supported Languages

```
en - English      |  ta - Tamil       |  bn - Bengali
hi - Hindi        |  te - Telugu      |  pa - Punjabi
kn - Kannada      |  ml - Malayalam   |  mr - Marathi
gu - Gujarati     |  ur - Urdu        |  od - Odia
```

---

## 📊 Response Examples

### News Object
```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Technology",
  "title": "Breaking News",
  "description": "Short description",
  "content": "Full content",
  "imageUrl": "https://...",
  "likesCount": 50,
  "commentsCount": 25,
  "publishedAt": "2026-02-21T10:30:00"
}
```

### Comment Object
```json
{
  "id": 1,
  "newsId": 1,
  "userUUID": "device-abc-123",
  "content": "Great article!",
  "likesCount": 5,
  "createdAt": "2026-02-21T10:30:00"
}
```

### User Profile
```json
{
  "id": 1,
  "deviceId": "device-abc-123",
  "age": 25,
  "preferredLanguage": "hi"
}
```

---

## ⚠️ Common Errors

| Code | Message | Fix |
|------|---------|-----|
| 400 | Invalid language code | Use correct language code |
| 400 | User must be 13+ | Age must be >= 13 |
| 400 | 1+ categories required | Select at least one category |
| 404 | News not found | Check news ID exists |
| 404 | User not found | Complete onboarding first |

---

## 🔍 Debugging Tips

1. **Missing deviceId?** → Add `?deviceId=device-uuid` to URL
2. **404 on news?** → Check if news > 5 days old (expired)
3. **Can't post comment?** → Verify deviceId parameter
4. **No languages showing?** → Check server is running

---

## 📅 Database Migrations

```sql
V1 - Initial schema
V2 - Anonymous users (remove auth, add deviceId)
V3 - News expiration (add expires_at, index)
```

All run automatically on startup.

---

## ⏱️ Performance

| Operation | Time |
|-----------|------|
| News load (10 items) | ~500ms |
| Comment load | ~300ms |
| Like toggle | ~200ms |
| Scheduled cleanup | <100ms |

---

## 📋 Documentation Map

| Document | Purpose |
|----------|---------|
| **API_DOCUMENTATION.md** | Complete API reference |
| **BACKEND_SETUP_COMPLETE.md** | Architecture details |
| **FRONTEND_IMPLEMENTATION_GUIDE.md** | For frontend devs |
| **IMPLEMENTATION_COMPLETE.md** | What changed |

---

## ✅ Verification Checklist

- [ ] Maven build successful
- [ ] App starts on port 8080
- [ ] GET /api/db returns "UP"
- [ ] Onboarding endpoints work
- [ ] News endpoints return data
- [ ] Comments can be posted
- [ ] Scheduled task logs appear hourly

---

## 🎯 Important Remember

1. **Always send deviceId** with requests that need it
2. **No Authorization header** needed
3. **All endpoints are public**
4. **Comments show deviceId** as UUID
5. **News auto-deletes** after 5 days
6. **Scheduled cleanup** runs every hour

---

## 🆘 Quick Help

**API not responding?**
- Check if app is running: `http://localhost:8080/api/db`

**No data in responses?**
- Verify database is connected
- Check migrations ran: `SELECT * FROM flyway_schema_history;`

**Comments not posting?**
- Verify deviceId is sent as query parameter
- Check news exists: `GET /api/news/{newsId}`

**Scheduled task not running?**
- Check logs for "deleted X expired news articles"
- Verify @EnableScheduling on main class

---

**Last Updated:** February 21, 2026
**Status:** ✅ Ready to Use

