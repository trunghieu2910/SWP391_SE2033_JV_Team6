import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';

// Read config from Vite env. In development the VITE_* vars may be missing.
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

let app = null;
let auth = null;
let googleProvider = null;

// Initialize Firebase only when a key is present to avoid runtime errors
// in environments where the developer has not set up Firebase.
if (firebaseConfig.apiKey) {
  try {
    app = initializeApp(firebaseConfig);
    auth = getAuth(app);
    googleProvider = new GoogleAuthProvider();
  } catch (err) {
    // Don't crash the entire app — log and export nulls so callers can handle absence.
    // eslint-disable-next-line no-console
    console.warn('Firebase initialization failed:', err);
    app = null;
    auth = null;
    googleProvider = null;
  }
} else {
  // eslint-disable-next-line no-console
  console.info('Firebase not configured (VITE_FIREBASE_API_KEY missing). Skipping initialization.');
}

export { auth, googleProvider };

