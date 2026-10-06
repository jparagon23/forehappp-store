package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.userModule.domain.model.StoreProfile;

public interface IGuestOrderLinkService {

    /**
     * Attaches every guest order placed with the profile's email to the profile, and fills its phone
     * and default address from the latest one when missing. Only call it once the email is proven to
     * belong to the user (verified code, Google, or an already active account). Returns orders linked.
     */
    int linkGuestOrders(StoreProfile profile);
}
