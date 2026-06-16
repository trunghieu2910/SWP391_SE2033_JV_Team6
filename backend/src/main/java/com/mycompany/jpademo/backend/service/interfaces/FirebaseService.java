package com.mycompany.jpademo.backend.service.interfaces;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseService {
    public FirebaseToken verifyIdToken(String idToken);
}
