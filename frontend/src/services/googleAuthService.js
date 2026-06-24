import { signInWithPopup } from 'firebase/auth';
import { auth, googleProvider } from '../firebase';
import api from './api';

export const googleAuthService = {
  getGoogleIdToken: async () => {
    if (!auth || !googleProvider) {
      throw new Error('Google auth is not configured. Set VITE_FIREBASE_API_KEY and related env vars.');
    }

    const result = await signInWithPopup(auth, googleProvider);
    const token = await result.user.getIdToken();

    return token;
  },

  googleLogin: (idToken) => {

    return api.post('/api/auth/google',
        { idToken: idToken },
        {
          // Ép buộc Spring Boot phải hiểu đây là JSON
          headers: {
            'Content-Type': 'application/json'
          }
        }
    );
  },

  googleRegister: (payload) =>
      api.post('/api/auth/google/complete', payload, {
        headers: { 'Content-Type': 'application/json' }
      }),
};

export default googleAuthService;