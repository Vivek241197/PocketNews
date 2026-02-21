# PocketNews Backend Setup - Completion Guide

## ✅ Changes Implemented

### 1. **Removed Authentication & Security Configuration**
   - **Removed from `pom.xml`:**
     - Spring Security dependency
     - JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson)
   
   - **Updated `SecurityConfig.java`:**
     - Completely deprecated - all endpoints are now public
     - No authentication or authorization checks needed
   
   - **Removed from `application-default.properties`:**
     - JWT configuration (app.jwt.secret, app.jwt.expiration)
     - Security configuration (spring.security.user.name, spring.security.user.password)
     - Security-related logging

### 2. **Deprecated Unused DTOs**
   - **UserLoginRequest.java** - No longer needed (no login)
   - **UserRegistrationRequest.java** - No longer needed (no registration)
   - **AuthResponse.java** - No longer needed (no authentication)

### 3. **Updated Data Transfer Objects**

   **CommentDTO:**
   - Removed `userId` field
   - Updated `deviceId` to `userUUID` to store device identifier
   - Comments now show UUID of the device that posted them

   **UserDTO:**
   - Removed: email, username, firstName, lastName, profileImageUrl, bio, isActive, isEmailVerified
   - Added: deviceId, age, preferredLanguage
   - Now represents anonymous users identified by device

### 4. **Fixed Service Classes**

   **CommentService:**
   - Updated `mapToDTO()` to use deviceId as the UUID identifier
   - Comments now track the user by device identifier instead of user ID
   - All comment operations work with deviceId

   **CommentRepository:**
   - Removed `findByUserId()` method (no longer tracking by user)
   - Kept: `findByNewsId()`, `countByNewsId()`

### 5. **Added News Expiration Feature**

   **News Entity:**
   - Added `expiresAt` field (LocalDateTime)
   - Updated `@PrePersist` to set expiration date to 5 days from creation
   - Automatically sets news expiration when created

   **NewsRepository:**
   - Added `deleteByExpiresAtBefore()` method with @Modifying and @Transactional
   - Query: `DELETE FROM News n WHERE n.expiresAt < :currentTime`

   **NewsSchedulerService (NEW):**
   - Created new scheduled service class
   - Runs every hour via @Scheduled(fixedRate = 3600000)
   - Automatically deletes expired news (older than 5 days)
   - Logs the number of deleted articles

   **PocketNewsApplication:**
   - Added `@EnableScheduling` annotation to enable scheduled tasks

   **Database Migration V3:**
   - Adds `expires_at` column to news table
   - Sets expiration date to created_at + 5 days for existing records
   - Creates index on `expires_at` for efficient queries

### 6. **Onboarding Flow (Already Complete)**
   - ✅ Step 1: Language selection (supports all Indian languages)
   - ✅ Step 2: Age input
   - ✅ Step 3: Category preferences selection
   - ✅ User identified by deviceId instead of authentication

## 📊 Architecture Summary

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Mobile App)                      │
│  - Generates deviceId on first launch                           │
│  - Completes 3-step onboarding process                          │
│  - Sends deviceId with every API request                        │
└──────────────────────┬──────────────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
┌────────────────────┐      ┌─────────────────────┐
│   OnboardingAPI    │      │    NewsAPI          │
│  - /onboarding/*   │      │  - /news/*          │
│  - Language        │      │  - Comments         │
│  - Age             │      │  - Bookmarks        │
│  - Categories      │      │  - Likes            │
└────────────┬───────┘      └────────────┬────────┘
             │                           │
             └───────────┬───────────────┘
                         │
                         ▼
        ┌─────────────────────────────────┐
        │   Services Layer                │
        │ - OnboardingService             │
        │ - NewsService                   │
        │ - CommentService                │
        │ - NewsSchedulerService (NEW)    │
        └────────────┬────────────────────┘
                     │
                     ▼
        ┌─────────────────────────────────┐
        │   Repository Layer              │
        │ - UserRepository                │
        │ - NewsRepository (updated)      │
        │ - CommentRepository (updated)   │
        │ - Other repositories            │
        └────────────┬────────────────────┘
                     │
                     ▼
        ┌─────────────────────────────────┐
        │   PostgreSQL Database           │
        │ - Users (device-based)          │
        │ - News (with expiration)        │
        │ - Comments (with deviceId)      │
        │ - Bookmarks, Likes, Preferences │
        └─────────────────────────────────┘
```

## 🔄 Data Flow

### User Onboarding
1. Mobile app generates deviceId (UUID)
2. POST /onboarding/step1 → Select Language
3. POST /onboarding/step2 → Enter Age
4. POST /onboarding/step3 → Select Categories
5. User profile stored with deviceId

### News & Comments
1. GET /news → Fetch news (only non-expired)
2. POST /news/{newsId}/comments → Add comment with deviceId
   - Comment stored with user's UUID (deviceId)
   - Comment shown with device identifier
3. Scheduled task runs hourly to delete news > 5 days old

## 📅 Database Migrations

- **V1__Initial_Database_Schema.sql** - Original schema
- **V2__Update_For_Anonymous_Users.sql** - Remove auth fields, add deviceId
- **V3__Add_News_Expiration.sql** - Add expires_at column and index

## 🚀 How to Deploy

### 1. Build the project
```bash
mvn clean install
```

### 2. Run migrations
Flyway will automatically run migrations on startup

### 3. Start the application
```bash
java -jar PocketNews-1.0-SNAPSHOT.jar
```

### 4. Verify
- Check database has new expires_at column
- Monitor logs for scheduled news deletion task
- Test onboarding flow with deviceId

## 🔍 Key Endpoints

### Onboarding
- `GET /api/onboarding/languages` - Available languages
- `POST /api/onboarding/step1` - Set language
- `POST /api/onboarding/step2` - Set age
- `POST /api/onboarding/step3` - Set categories
- `GET /api/onboarding/profile/{deviceId}` - Get user profile
- `GET /api/onboarding/preferences/{deviceId}` - Get preferences

### News
- `GET /api/news` - Get all non-expired news
- `GET /api/news/featured` - Get featured news
- `GET /api/news/{id}` - Get specific news

### Comments
- `GET /api/news/{newsId}/comments` - Get comments
- `POST /api/news/{newsId}/comments?deviceId=xxx` - Add comment

## ⏰ Scheduled Tasks

**NewsSchedulerService:**
- Runs every hour automatically
- Deletes news where expiresAt < current timestamp
- Logs deleted count to application logs
- No manual intervention needed

## 📝 Notes

- All users are identified by `deviceId` parameter
- No database queries needed for user authentication
- News automatically expires after 5 days
- Comments are associated with device UUID, not user ID
- Onboarding is the first step before viewing news

## ✨ Next Steps

1. **Frontend:** Generate deviceId on first app launch
2. **Frontend:** Complete onboarding before showing news feed
3. **Backend:** Set up cron job monitoring for scheduled task (optional)
4. **Testing:** Test news expiration with test data
5. **Deployment:** Deploy to production with database migrations

---

**Status:** ✅ Backend structure is ready for development
**Last Updated:** February 21, 2026

