package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.exception.UnauthorizedException;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import org.springframework.stereotype.Service;

@Service
public class FirebaseServiceImpl implements FirebaseService {

    public FirebaseToken verifyIdToken(String idToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new UnauthorizedException("Firebase is not configured. Please set GOOGLE_APPLICATION_CREDENTIALS or add firebase-service-account.json to classpath.");
        }

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new UnauthorizedException("Invalid Firebase token");
        }
    }
}
