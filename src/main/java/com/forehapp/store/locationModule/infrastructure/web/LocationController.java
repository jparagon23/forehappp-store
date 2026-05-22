package com.forehapp.store.locationModule.infrastructure.web;

import com.forehapp.store.locationModule.domain.ports.in.ILocationService;
import com.forehapp.store.locationModule.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    // ── Public GETs (dropdowns) ────────────────────────────────────────────

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        return ResponseEntity.ok(locationService.getCountries());
    }

    @GetMapping("/countries/{countryId}/states")
    public ResponseEntity<List<StateResponse>> getStates(@PathVariable Long countryId) {
        return ResponseEntity.ok(locationService.getStatesByCountry(countryId));
    }

    @GetMapping("/states/{stateId}/cities")
    public ResponseEntity<List<CityResponse>> getCities(@PathVariable Long stateId) {
        return ResponseEntity.ok(locationService.getCitiesByState(stateId));
    }

    // ── Admin CRUD ─────────────────────────────────────────────────────────

    @PostMapping("/countries")
    public ResponseEntity<CountryResponse> createCountry(
            @Valid @RequestBody CreateCountryDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createCountry(dto, Long.parseLong(userId)));
    }

    @PostMapping("/states")
    public ResponseEntity<StateResponse> createState(
            @Valid @RequestBody CreateStateDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createState(dto, Long.parseLong(userId)));
    }

    @PostMapping("/cities")
    public ResponseEntity<CityResponse> createCity(
            @Valid @RequestBody CreateCityDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createCity(dto, Long.parseLong(userId)));
    }
}
