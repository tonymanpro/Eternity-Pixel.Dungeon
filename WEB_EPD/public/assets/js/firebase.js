/**
 * firebase.js — Eternity Pixel Dungeon Website
 * Firebase initialization + Firestore + Analytics helpers.
 * Project: eternity-pixel-dungeon
 */

// ── Imports (must be at top in ESM) ─────────────────────────────────────────
import { initializeApp }    from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
import { getAnalytics }     from "https://www.gstatic.com/firebasejs/10.12.0/firebase-analytics.js";
import { getFirestore, collection, getDocs, getDoc, doc, addDoc, updateDoc, deleteDoc, query, orderBy, limit, Timestamp }
                            from "https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js";
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged }
                            from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";

// ── Firebase Config ──────────────────────────────────────────────────────────
const firebaseConfig = {
  apiKey:            "AIzaSyCYFgYuqYHNrw-IYVf1V_AkNQA3E3v79Hw",
  authDomain:        "eternity-pixel-dungeon.firebaseapp.com",
  projectId:         "eternity-pixel-dungeon",
  storageBucket:     "eternity-pixel-dungeon.firebasestorage.app",
  messagingSenderId: "362176379789",
  appId:             "1:362176379789:web:ab7115a32f9f7b3f0edbc4",
  measurementId:     "G-Y25SVBBP8L"
};

// ── Init ─────────────────────────────────────────────────────────────────────
const app       = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const db        = getFirestore(app);
const auth      = getAuth(app);

// Expose config so other scripts can detect real vs placeholder setup
window.firebaseConfig = firebaseConfig;

// ── Posts Helpers ─────────────────────────────────────────────────────────────

/**
 * Fetch latest N posts, ordered by date descending.
 * @param {number} maxResults
 * @returns {Promise<Array>}
 */
async function getLatestPosts(maxResults = 6) {
  try {
    const q = query(
      collection(db, 'posts'),
      orderBy('date', 'desc'),
      limit(maxResults)
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
  } catch (e) {
    console.error('getLatestPosts:', e);
    return [];
  }
}

/**
 * Fetch all posts (for blog list page).
 * @returns {Promise<Array>}
 */
async function getAllPosts() {
  try {
    const q = query(collection(db, 'posts'), orderBy('date', 'desc'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
  } catch (e) {
    console.error('getAllPosts:', e);
    return [];
  }
}

/**
 * Fetch a single post by Firestore ID.
 * @param {string} postId
 * @returns {Promise<Object|null>}
 */
async function getPost(postId) {
  try {
    const snap = await getDoc(doc(db, 'posts', postId));
    return snap.exists() ? { id: snap.id, ...snap.data() } : null;
  } catch (e) {
    console.error('getPost:', e);
    return null;
  }
}

/**
 * Save (create or update) a post.
 * @param {Object} data  — post fields
 * @param {string} [id] — if provided, updates existing post
 * @returns {Promise<string>} — post ID
 */
async function savePost(data, id) {
  const payload = { ...data, updatedAt: Timestamp.now() };
  if (id) {
    await updateDoc(doc(db, 'posts', id), payload);
    return id;
  } else {
    payload.date = Timestamp.now();
    const ref = await addDoc(collection(db, 'posts'), payload);
    return ref.id;
  }
}

/**
 * Delete a post by ID.
 * @param {string} id
 */
async function deletePost(id) {
  await deleteDoc(doc(db, 'posts', id));
}

// ── Auth Helpers ──────────────────────────────────────────────────────────────

async function signIn(email, password) {
  return signInWithEmailAndPassword(auth, email, password);
}

async function logOut() {
  return signOut(auth);
}

function onAuth(callback) {
  return onAuthStateChanged(auth, callback);
}

// ── Exports (global for non-module pages) ────────────────────────────────────
window.EPD = window.EPD || {};
Object.assign(window.EPD, {
  db, auth,
  getLatestPosts, getAllPosts, getPost, savePost, deletePost,
  signIn, logOut, onAuth,
  Timestamp
});
