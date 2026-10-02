package com.forehapp.store.repurchaseModule.infrastructure.web;

import com.forehapp.store.repurchaseModule.application.dto.EmailPreferenceResponse;
import com.forehapp.store.repurchaseModule.application.dto.EmailPreferenceTokenDto;
import com.forehapp.store.repurchaseModule.domain.ports.in.IEmailPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public: the signed token in the email link is the only credential (guests have no account)
@RestController
@RequestMapping("/api/v1/email-preferences")
public class EmailPreferenceController {

    private final IEmailPreferenceService emailPreferenceService;

    public EmailPreferenceController(IEmailPreferenceService emailPreferenceService) {
        this.emailPreferenceService = emailPreferenceService;
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<EmailPreferenceResponse> unsubscribe(@Valid @RequestBody EmailPreferenceTokenDto dto) {
        return ResponseEntity.ok(emailPreferenceService.unsubscribe(dto.getToken()));
    }

    @PostMapping("/resubscribe")
    public ResponseEntity<EmailPreferenceResponse> resubscribe(@Valid @RequestBody EmailPreferenceTokenDto dto) {
        return ResponseEntity.ok(emailPreferenceService.resubscribe(dto.getToken()));
    }
}
