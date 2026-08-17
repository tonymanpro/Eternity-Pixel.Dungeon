# Eternity Pixel Dungeon — Official Website

Official multilingual website (English/Spanish) for **Eternity Pixel Dungeon**, hosted on Firebase.

## Structure

```
WEB_EPD/
├── public/                   ← Firebase Hosting root
│   ├── index.html            ← Homepage (Hero + Features + Blog Preview)
│   ├── game.html             ← The Game (Classes, Screenshots, Download)
│   ├── blog.html             ← Blog listing (Firestore-powered)
│   ├── blog-post.html        ← Single post template (Markdown rendering)
│   ├── changelog.html        ← Version history
│   ├── about.html            ← About the project & Credits
│   ├── 404.html              ← Custom error page
│   ├── admin/index.html      ← Admin panel (Firebase Auth protected)
│   ├── assets/
│   │   ├── css/style.css     ← Complete design system
│   │   ├── js/
│   │   │   ├── i18n.js       ← i18n engine (EN/ES auto-detection)
│   │   │   ├── firebase.js   ← Firebase init + Firestore/Auth helpers
│   │   │   ├── blog.js       ← Post rendering from Firestore
│   │   │   └── main.js       ← UI: navbar, lightbox, toasts
│   │   └── images/           ← Game icon, hero background, screenshots
│   └── translations/
│       ├── en.json           ← All English strings
│       └── es.json           ← All Spanish strings
├── firebase.json             ← Hosting config, rewrites, caching headers
├── firestore.rules           ← Security rules (public read, auth write)
└── firestore.indexes.json
```

## Setup Instructions

### 1. Create Firebase Project
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create a new project (e.g., `eternity-pixel-dungeon`)
3. Enable **Firestore Database** (start in production mode)
4. Enable **Authentication** → Email/Password
5. Add a **Web App** and copy the config

### 2. Configure Firebase in Code
Edit `public/assets/js/firebase.js` and replace the placeholder config:
```js
const firebaseConfig = {
  apiKey:            "YOUR_API_KEY",
  authDomain:        "YOUR_PROJECT.firebaseapp.com",
  projectId:         "YOUR_PROJECT_ID",
  ...
};
```

### 3. Create `.firebaserc`
```json
{
  "projects": {
    "default": "YOUR_PROJECT_ID"
  }
}
```

### 4. Install Firebase CLI & Deploy
```bash
npm install -g firebase-tools
firebase login
cd WEB_EPD
firebase init hosting   # select existing project, public dir = "public"
firebase deploy
```

### 5. Create Admin User
In Firebase Console → Authentication → Users → Add User

### 6. Access Admin Panel
Navigate to `https://YOUR_SITE.web.app/admin/` and log in.

## i18n — Adding a New Language

1. Copy `public/translations/en.json` → `public/translations/fr.json` (or any locale)
2. Translate all values
3. Add the locale to `SUPPORTED_LANGS` array in `public/assets/js/i18n.js`
4. Add a button to the language toggle in each HTML file:
   ```html
   <button class="lang-btn" data-lang="fr">FR</button>
   ```

## Publishing Blog Posts (Admin Panel)

1. Go to `/admin/`
2. Sign in with your Firebase Auth credentials
3. Click "New Post"
4. Fill in titles, summaries, and Markdown body in both English and Spanish
5. Add a cover image URL and tags
6. Click "Publish Post"

Posts are immediately live on the website without any redeployment.
