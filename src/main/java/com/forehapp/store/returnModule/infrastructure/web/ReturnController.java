package com.forehapp.store.returnModule.infrastructure.web;

import com.forehapp.store.returnModule.application.dto.CreateReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;
import com.forehapp.store.returnModule.domain.ports.in.IReturnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final IReturnService returnService;

    public ReturnController(IReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse createReturn(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateReturnRequestDto dto) {
        return returnService.createReturn(Long.parseLong(userId), dto);
    }

    @GetMapping("/my")
    public List<ReturnResponse> getMyReturns(@AuthenticationPrincipal String userId) {
        return returnService.getMyReturns(Long.parseLong(userId));
    }

    @GetMapping("/{returnId}")
    public ReturnResponse getReturn(
            @AuthenticationPrincipal String userId,
            @PathVariable Long returnId) {
        return returnService.getReturn(Long.parseLong(userId), returnId);
    }
}
