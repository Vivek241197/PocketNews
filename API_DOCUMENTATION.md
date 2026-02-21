# PocketNews API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
**NO AUTHENTICATION REQUIRED** - All endpoints are public. Use `deviceId` parameter for user identification.

---

## 📱 Onboarding Endpoints

### 1. Get Available Languages
```
GET /onboarding/languages
```

**Response:**
```json
{
  "languages": [
    {
      "code": "en",
      "name": "English",
      "nativeName": "English"
    },
    {
      "code": "hi",
      "name": "Hindi",
      "nativeName": "हिंदी"
    }
    // ... more languages
  ]
}
```

---

### 2. Step 1: Set Language Preference
```
POST /onboarding/step1
Content-Type: application/json

{
  "deviceId": "device-uuid-12345",
  "preferredLanguage": "hi"
}
```

**Response:**
```json
{
  "id": 1,
  "deviceId": "device-uuid-12345",
  "age": 0,
  "preferredLanguage": "hi"
}
```

---

### 3. Step 2: Set Age
```
POST /onboarding/step2
Content-Type: application/json

{
  "deviceId": "device-uuid-12345",
  "age": 25
}
```

**Response:**
```json
{
  "id": 1,
  "deviceId": "device-uuid-12345",
  "age": 25,
  "preferredLanguage": "hi"
}
```

**Validation:** Age must be >= 13

---

### 4. Step 3: Set Category Preferences
```
POST /onboarding/step3
Content-Type: application/json

{
  "deviceId": "device-uuid-12345",
  "preferredCategoryIds": [1, 2, 3]
}
```

**Response:**
```json
{
  "id": 1,
  "deviceId": "device-uuid-12345",
  "age": 25,
  "preferredLanguage": "hi"
}
```

**Validation:** At least 1 category must be selected

---

### 5. Get User Profile
```
GET /onboarding/profile/{deviceId}
```

**Response:**
```json
{
  "id": 1,
  "deviceId": "device-uuid-12345",
  "age": 25,
  "preferredLanguage": "hi"
}
```

---

### 6. Get User Preferences
```
GET /onboarding/preferences/{deviceId}
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "preferredCategories": "[1,2,3]",
  "language": "",
  "theme": "",
  "notificationsEnabled": false,
  "createdAt": "2026-02-21T10:30:00",
  "updatedAt": "2026-02-21T10:30:00"
}
```

---

### 7. Update Language Preference
```
PUT /onboarding/{deviceId}/language?languageCode=en
```

**Response:**
```json
{
  "id": 1,
  "deviceId": "device-uuid-12345",
  "age": 25,
  "preferredLanguage": "en"
}
```

---

## 📰 News Endpoints

### 1. Get All News
```
GET /news?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "Technology",
      "title": "Breaking News Title",
      "description": "Short description",
      "content": "Full content here",
      "imageUrl": "https://...",
      "sourceUrl": "https://...",
      "sourceName": "Source Name",
      "author": "Author Name",
      "isFeatured": true,
      "viewsCount": 100,
      "likesCount": 50,
      "commentsCount": 25,
      "publishedAt": "2026-02-21T10:30:00",
      "createdAt": "2026-02-21T10:30:00",
      "updatedAt": "2026-02-21T10:30:00"
    }
  ],
  "pageable": { /* pagination info */ },
  "totalElements": 100,
  "totalPages": 10
}
```

---

### 2. Get Featured News
```
GET /news/featured?page=0&size=10
```

**Response:** Same as above

---

### 3. Get News by Category
```
GET /news/category/{categoryId}?page=0&size=10
```

**Response:** Same as above

---

### 4. Get Specific News
```
GET /news/{newsId}
```

**Response:** Single news object (same structure)

---

### 5. Create News (Admin)
```
POST /news
Content-Type: application/json

{
  "categoryId": 1,
  "title": "News Title",
  "description": "Short description",
  "content": "Full content",
  "imageUrl": "https://...",
  "sourceUrl": "https://...",
  "sourceName": "Source",
  "author": "Author",
  "isFeatured": true
}
```

---

### 6. Update News (Admin)
```
PUT /news/{newsId}
Content-Type: application/json

{
  "categoryId": 1,
  "title": "Updated Title",
  // ... other fields
}
```

---

### 7. Delete News (Admin)
```
DELETE /news/{newsId}
```

---

## 💬 Comment Endpoints

### 1. Get Comments for News
```
GET /news/{newsId}/comments?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "newsId": 1,
      "userUUID": "device-uuid-12345",  // Device identifier
      "content": "Great news!",
      "likesCount": 5,
      "isActive": true,
      "createdAt": "2026-02-21T10:30:00",
      "updatedAt": "2026-02-21T10:30:00"
    }
  ],
  "pageable": { /* pagination info */ },
  "totalElements": 25,
  "totalPages": 3
}
```

---

### 2. Create Comment
```
POST /news/{newsId}/comments?deviceId=device-uuid-12345
Content-Type: application/json

{
  "content": "This is a great news article!"
}
```

**Response:**
```json
{
  "id": 1,
  "newsId": 1,
  "userUUID": "device-uuid-12345",
  "content": "This is a great news article!",
  "likesCount": 0,
  "isActive": true,
  "createdAt": "2026-02-21T10:30:00",
  "updatedAt": "2026-02-21T10:30:00"
}
```

---

### 3. Update Comment
```
PUT /news/{newsId}/comments/{commentId}
Content-Type: application/json

{
  "content": "Updated comment text"
}
```

---

### 4. Delete Comment
```
DELETE /news/{newsId}/comments/{commentId}
```

---

## ❤️ Like Endpoints

### 1. Get Like Status
```
GET /news/{newsId}/likes?deviceId=device-uuid-12345
```

**Response:**
```json
{
  "isLiked": true,
  "totalLikes": 150
}
```

---

### 2. Toggle Like
```
POST /news/{newsId}/likes?deviceId=device-uuid-12345
```

**Response:**
```json
{
  "isLiked": true,
  "totalLikes": 151
}
```

---

## 🔖 Bookmark Endpoints

### 1. Get Bookmark Status
```
GET /news/{newsId}/bookmarks?deviceId=device-uuid-12345
```

**Response:**
```json
{
  "isBookmarked": true,
  "totalBookmarks": 45
}
```

---

### 2. Toggle Bookmark
```
POST /news/{newsId}/bookmarks?deviceId=device-uuid-12345
```

**Response:**
```json
{
  "isBookmarked": true,
  "totalBookmarks": 46
}
```

---

### 3. Get User's Bookmarks
```
GET /user/bookmarks?deviceId=device-uuid-12345&page=0&size=10
```

**Response:**
```json
{
  "content": [1, 2, 3, 4, 5],  // News IDs
  "pageable": { /* pagination info */ },
  "totalElements": 15,
  "totalPages": 2
}
```

---

## 📂 Category Endpoints

### 1. Get All Categories
```
GET /categories?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Technology",
      "slug": "technology",
      "description": "Tech news",
      "iconUrl": "https://...",
      "displayOrder": 1,
      "isActive": true,
      "createdAt": "2026-02-21T10:30:00",
      "updatedAt": "2026-02-21T10:30:00"
    }
  ],
  "pageable": { /* pagination info */ },
  "totalElements": 15,
  "totalPages": 2
}
```

---

### 2. Get Specific Category
```
GET /categories/{categoryId}
```

---

### 3. Create Category (Admin)
```
POST /categories
Content-Type: application/json

{
  "name": "Technology",
  "slug": "technology",
  "description": "Tech news",
  "iconUrl": "https://...",
  "displayOrder": 1
}
```

---

## ✅ Health Check
```
GET /db
```

**Response:**
```json
{
  "status": "UP"
}
```

---

## 🔴 Error Responses

### Bad Request (400)
```json
{
  "error": "BadRequestException",
  "message": "Invalid language code: xyz"
}
```

### Not Found (404)
```json
{
  "error": "ResourceNotFoundException",
  "message": "News not found with id: 999"
}
```

### Server Error (500)
```json
{
  "error": "Exception",
  "message": "Internal server error"
}
```

---

## 🔐 Security Notes

- ✅ No authentication needed
- ✅ All endpoints are public
- ✅ Use `deviceId` to track user-specific data
- ✅ News automatically expires after 5 days
- ✅ Comments stored with device UUID (not user ID)

---

## 📍 Supported Languages

| Code | Language | Native |
|------|----------|--------|
| en | English | English |
| hi | Hindi | हिंदी |
| ta | Tamil | தமிழ் |
| te | Telugu | తెలుగు |
| kn | Kannada | ಕನ್ನಡ |
| ml | Malayalam | മലയാളം |
| bn | Bengali | বাংলা |
| pa | Punjabi | ਪੰਜਾਬੀ |
| mr | Marathi | मराठी |
| gu | Gujarati | ગુજરાતી |
| ur | Urdu | اردو |
| od | Odia | ଓଡ଼ିଆ |

---

**Last Updated:** February 21, 2026
**Status:** ✅ Ready for Frontend Integration

