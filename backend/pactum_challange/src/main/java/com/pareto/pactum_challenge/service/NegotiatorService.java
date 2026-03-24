package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.dto.AddTermPreferenceRequest;
import com.pareto.pactum_challenge.dto.CreateNegotiatorRequest;
import com.pareto.pactum_challenge.entity.NegotiationTerm;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.repository.NegotiationTermRepository;
import com.pareto.pactum_challenge.repository.NegotiatorRepository;
import com.pareto.pactum_challenge.repository.NegotiatorTermPreferenceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NegotiatorService {

    private final NegotiatorRepository negotiatorRepository;
    private final NegotiationTermRepository termRepository;
    private final NegotiatorTermPreferenceRepository preferenceRepository;

    @Transactional
    public Negotiator create(CreateNegotiatorRequest request) {
        Negotiator negotiator = new Negotiator();
        negotiator.setName(request.name());
        negotiator.setAcceptanceThreshold(request.acceptanceThreshold());
        negotiator.setWalkawayThreshold(request.walkawayThreshold());
        negotiator.setConcessionRate(request.concessionRate());
        negotiator.setMaxOffersCount(request.maxOffersCount());
        negotiator.setStrategy(request.strategy());
        negotiator.setMarketContext(request.marketContext());
        return negotiatorRepository.save(negotiator);
    }

    @Transactional
    public NegotiatorTermPreference addPreference(Long negotiatorId, AddTermPreferenceRequest request) {
        Negotiator negotiator = negotiatorRepository.findById(negotiatorId)
                .orElseThrow(() -> new EntityNotFoundException("Negotiator not found"));

        NegotiationTerm term;
        if (request.termId() != null) {
            term = termRepository.findById(request.termId())
                    .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        } else {
            term = new NegotiationTerm();
            term.setName(request.termName());
            term.setUnit(request.termUnit());
            term.setDescription(request.termDescription());
            term.setMin(request.termMin());
            term.setMax(request.termMax());
            term.setWholeNumber(request.termWholeNumber() != null && request.termWholeNumber());
            term = termRepository.save(term);
        }

        NegotiatorTermPreference preference = new NegotiatorTermPreference();
        preference.setNegotiator(negotiator);
        preference.setNegotiationTerm(term);
        preference.setDirection(request.direction());
        preference.setIdealValue(request.idealValue());
        preference.setLimitValue(request.limitValue());
        preference.setWeight(request.weight());
        preference.setStrictness(request.strictness());
        preference.setReasoning(request.reasoning());
        return preferenceRepository.save(preference);
    }

    public Negotiator findById(Long id) {
        return negotiatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Negotiator not found"));
    }

    public List<Negotiator> findAll() {
        return negotiatorRepository.findAll();
    }

    public List<NegotiatorTermPreference> getPreferences(Long negotiatorId) {
        return preferenceRepository.findAllByNegotiatorId(negotiatorId);
    }
}
