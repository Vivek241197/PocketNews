# PocketNews - Frontend Implementation Checklist

## 🎯 Overview
The backend is now configured for a **device-based, authentication-free** news app with automatic news expiration and user preference management.

---

## 📱 Frontend Implementation Steps

### Phase 1: Device ID Management

- [ ] **Generate Device ID on First Launch**
  - Use UUID library to generate a unique 36-character ID
  - Store in local storage / shared preferences: `deviceId`
  - Generate only once on first app launch
  - Persist across app sessions

### Phase 2: Onboarding Flow (3 Steps)

#### Step 1: Language Selection
- [ ] Display list of 12 Indian languages
- [ ] Call: `GET /api/onboarding/languages` to get available languages
- [ ] User selects preferred language
- [ ] Call: `POST /api/onboarding/step1` with deviceId and language
- [ ] Store selected language locally

#### Step 2: Age Input
- [ ] Display age input form
- [ ] Show validation: Age must be >= 13
- [ ] Call: `POST /api/onboarding/step2` with deviceId and age
- [ ] Handle error if age < 13

#### Step 3: Category Selection
- [ ] Call: `GET /api/categories` to fetch all categories
- [ ] Display categories as checkboxes/chips
- [ ] Allow user to select at least 1 category (show validation error if none selected)
- [ ] Call: `POST /api/onboarding/step3` with deviceId and selectedCategoryIds
- [ ] Mark onboarding as complete

### Phase 3: News Feed

#### News Display
- [ ] Fetch news: `GET /api/news?page=0&size=10`
- [ ] Display news cards with:
  - Title
  - Description
  - Image (imageUrl)
  - Category name
  - Published date
  - Like count
  - Comment count
  - Bookmark status

#### Featured Section
- [ ] Show featured news: `GET /api/news/featured?page=0&size=10`
- [ ] Display separately or in a carousel

#### Category-Based Feed
- [ ] Add category filter buttons
- [ ] When category selected: `GET /api/news/category/{categoryId}?page=0&size=10`
- [ ] Load news filtered by category

#### Pagination
- [ ] Implement infinite scroll or pagination
- [ ] Load more news as user scrolls down
- [ ] Increment `page` parameter: page=0, page=1, page=2, etc.

### Phase 4: Interactions

#### Likes
- [ ] Add like button on each news card
- [ ] On click: Check current like status `GET /api/news/{newsId}/likes?deviceId=XXX`
- [ ] Toggle like: `POST /api/news/{newsId}/likes?deviceId=XXX`
- [ ] Update like count UI
- [ ] Visual feedback (change icon color/fill)

#### Bookmarks
- [ ] Add bookmark button on news card
- [ ] On click: Check status `GET /api/news/{newsId}/bookmarks?deviceId=XXX`
- [ ] Toggle bookmark: `POST /api/news/{newsId}/bookmarks?deviceId=XXX`
- [ ] Update visual state
- [ ] Display "Saved" or similar indicator

#### Comments
- [ ] Add "View Comments" button/section on news detail
- [ ] Fetch comments: `GET /api/news/{newsId}/comments?page=0&size=10`
- [ ] Display comments with:
  - Comment text
  - User UUID (device ID) - can be masked as "Anonymous User #ABC123"
  - Created date
  - Like count for comment

- [ ] Add comment input box
- [ ] Text validation: Not empty, reasonable length limit
- [ ] Post comment: `POST /api/news/{newsId}/comments?deviceId=XXX` with comment text
- [ ] Add new comment to list after post
- [ ] Clear input field after successful post

### Phase 5: User Profile & Settings

#### View Profile
- [ ] Display user information: `GET /api/onboarding/profile/{deviceId}`
- [ ] Show: Device ID, Age, Preferred Language, Selected Categories

#### View Bookmarks
- [ ] Fetch user's bookmarks: `GET /api/user/bookmarks?deviceId=XXX&page=0&size=10`
- [ ] Display list of bookmarked news
- [ ] Allow user to view or unbookmark

#### Update Settings
- [ ] Allow language change: `PUT /api/onboarding/{deviceId}/language?languageCode=en`
- [ ] Update language throughout app UI

#### View Preferences
- [ ] Fetch preferences: `GET /api/onboarding/preferences/{deviceId}`
- [ ] Display selected categories

### Phase 6: Error Handling

- [ ] **Network Error Handling**
  - Show "Unable to connect to server" message
  - Retry button
  - Offline mode (cache data if possible)

- [ ] **Validation Errors**
  - Display error messages from API
  - Highlight invalid fields
  - Show "At least 1 category required" when needed
  - Show "Age must be 13+" when needed

- [ ] **User-Friendly Messages**
  - "No news available" when feed is empty
  - "Loading..." state during API calls
  - "News has expired" for old articles
  - "Comment posted successfully" notifications

### Phase 7: Performance Optimization

- [ ] **Caching**
  - Cache language list (rarely changes)
  - Cache categories
  - Cache downloaded news to support offline viewing

- [ ] **Lazy Loading**
  - Load images only when visible
  - Implement pagination/infinite scroll efficiently
  - Don't load all news at once

- [ ] **State Management**
  - Store deviceId in persistent storage
  - Cache onboarding completion status
  - Store user preferences locally
  - Minimize API calls

---

## 🔄 API Integration Pattern

### Example Flow: Fetching and Liking a News Article

```javascript
// 1. Get deviceId from local storage
const deviceId = localStorage.getItem('deviceId');

// 2. Fetch news
async function getNews() {
  const response = await fetch('/api/news?page=0&size=10');
  const data = await response.json();
  // Display news...
}

// 3. Toggle like
async function toggleLike(newsId) {
  const response = await fetch(
    `/api/news/${newsId}/likes?deviceId=${deviceId}`,
    { method: 'POST' }
  );
  const data = await response.json();
  // Update UI: isLiked and totalLikes
}

// 4. Post comment
async function postComment(newsId, commentText) {
  const response = await fetch(
    `/api/news/${newsId}/comments?deviceId=${deviceId}`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: commentText })
    }
  );
  const data = await response.json();
  // Add comment to UI
}
```

---

## 📊 Data Flow Diagram

```
Mobile App Startup
       │
       ▼
┌─────────────────────┐
│ Generate/Load       │
│ Device ID           │
│ (UUID format)       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Show Onboarding?    │─── NO ──→ Show News Feed
└──────────┬──────────┘
           │ YES
           ▼
┌─────────────────────┐
│ Step 1: Language    │
│ POST /step1         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Step 2: Age         │
│ POST /step2         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Step 3: Categories  │
│ POST /step3         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Show News Feed      │
│ GET /news           │
└─────────────────────┘
           │
    ┌──────┼──────┐
    ▼      ▼      ▼
 Like  Comment Bookmark
```

---

## 🛡️ Important Notes for Frontend

1. **Always Include DeviceID**
   - Every API call that needs user identification should include `?deviceId=XXX`
   - Store deviceId securely in local/device storage

2. **No Authentication Headers**
   - Do NOT send Authorization headers
   - Do NOT send JWT tokens
   - All endpoints are public

3. **News Expiration**
   - News articles automatically delete after 5 days from creation
   - Don't worry about removing old news from UI - backend handles it
   - You might see articles disappear from the feed

4. **Comments Show Device UUID**
   - Comments don't show user names
   - Instead, they show a device UUID
   - You can format as "Anonymous User #xyz" in UI

5. **Device ID Persistence**
   - Generate once and save in local/device storage
   - Use same deviceId across entire app lifetime
   - Don't generate a new one each time app opens

6. **Error Handling**
   - All errors return a status code (400, 404, 500) with error message
   - Always check response.ok or status code
   - Handle network timeouts gracefully

---

## 📚 API Response Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | OK - Request successful | Continue |
| 201 | Created - Resource created | Show success |
| 204 | No Content - Action completed | No response body |
| 400 | Bad Request - Invalid input | Show error message |
| 404 | Not Found - Resource doesn't exist | Show "Not found" |
| 500 | Server Error | Show "Server error" |

---

## 🧪 Testing Checklist

- [ ] Test onboarding flow end-to-end
- [ ] Test news feed loading and pagination
- [ ] Test like/unlike functionality
- [ ] Test comment posting
- [ ] Test bookmark toggle
- [ ] Test category filtering
- [ ] Test language switching
- [ ] Test error scenarios (network timeout, invalid input, etc.)
- [ ] Test offline mode (cache)
- [ ] Test on slow network (throttle connection)
- [ ] Test with deviceId persistence across app restarts

---

## 🚀 Deployment Readiness

- [ ] Backend database migrations applied (V1, V2, V3)
- [ ] Scheduled news deletion task running every hour
- [ ] API endpoints tested with Postman/Insomnia
- [ ] Error messages are user-friendly
- [ ] Frontend can handle all API responses
- [ ] DeviceId generation works correctly
- [ ] All endpoints return proper CORS headers

---

## 📞 Backend Support Info

If frontend encounters issues:

1. **Check API_DOCUMENTATION.md** for endpoint details
2. **Check BACKEND_SETUP_COMPLETE.md** for architecture
3. **Verify deviceId format** is 36-character UUID
4. **Check response status codes** and error messages
5. **Monitor server logs** for scheduled task execution

---

**Last Updated:** February 21, 2026
**Status:** ✅ Ready for Frontend Development
**Estimated Frontend Dev Time:** 2-3 weeks depending on UI complexity

