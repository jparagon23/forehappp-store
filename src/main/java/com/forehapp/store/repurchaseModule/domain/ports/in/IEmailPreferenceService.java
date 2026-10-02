package com.forehapp.store.repurchaseModule.domain.ports.in;

import com.forehapp.store.repurchaseModule.application.dto.EmailPreferenceResponse;

public interface IEmailPreferenceService {
    EmailPreferenceResponse unsubscribe(String token);
    EmailPreferenceResponse resubscribe(String token);
}
