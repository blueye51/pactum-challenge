package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.dto.AddTermPreferenceRequest;
import com.pareto.pactum_challenge.dto.CreateNegotiatorRequest;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.service.NegotiatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negotiators")
@RequiredArgsConstructor
public class NegotiatorController {

    private final NegotiatorService negotiatorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Negotiator create(@Valid @RequestBody CreateNegotiatorRequest request) {
        return negotiatorService.create(request);
    }

    @GetMapping
    public List<Negotiator> listAll() {
        return negotiatorService.findAll();
    }

    @GetMapping("/{id}")
    public Negotiator getById(@PathVariable Long id) {
        return negotiatorService.findById(id);
    }

    @PostMapping("/{id}/preferences")
    @ResponseStatus(HttpStatus.CREATED)
    public NegotiatorTermPreference addPreference(
            @PathVariable Long id,
            @Valid @RequestBody AddTermPreferenceRequest request) {
        return negotiatorService.addPreference(id, request);
    }

    @GetMapping("/{id}/preferences")
    public List<NegotiatorTermPreference> getPreferences(@PathVariable Long id) {
        return negotiatorService.getPreferences(id);
    }
}
