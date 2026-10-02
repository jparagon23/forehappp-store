package com.forehapp.store.repurchaseModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.repurchaseModule.application.dto.EmailPreferenceResponse;
import com.forehapp.store.repurchaseModule.domain.ports.in.IEmailPreferenceService;
import com.forehapp.store.repurchaseModule.domain.ports.out.IEmailUnsubscribeDao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EmailPreferenceServiceImpl implements IEmailPreferenceService {

    private final IEmailUnsubscribeDao unsubscribeDao;
    private final UnsubscribeTokenService tokenService;

    public EmailPreferenceServiceImpl(IEmailUnsubscribeDao unsubscribeDao, UnsubscribeTokenService tokenService) {
        this.unsubscribeDao = unsubscribeDao;
        this.tokenService = tokenService;
    }

    @Override
    public EmailPreferenceResponse unsubscribe(String token) {
        String email = resolveEmail(token);
        if (!unsubscribeDao.exists(email)) {
            try {
                unsubscribeDao.save(email);
            } catch (DataIntegrityViolationException ignored) {
                // Unsubscribed concurrently (e.g. double click) — already in the desired state
            }
        }
        return new EmailPreferenceResponse(maskEmail(email), false);
    }

    @Override
    public EmailPreferenceResponse resubscribe(String token) {
        String email = resolveEmail(token);
        unsubscribeDao.delete(email);
        return new EmailPreferenceResponse(maskEmail(email), true);
    }

    private String resolveEmail(String token) {
        return tokenService.parseEmail(token)
                .orElseThrow(() -> new BadRequestException(ErrorCode.EMAIL_PREFERENCE_TOKEN_INVALID,
                        "Invalid email preference token"));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
