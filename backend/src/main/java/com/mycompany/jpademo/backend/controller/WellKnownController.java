package com.mycompany.jpademo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {

    @GetMapping("/.well-known/**")
    public ResponseEntity<?> handleWellKnown() {
        return ResponseEntity.ok().build();
    }
}