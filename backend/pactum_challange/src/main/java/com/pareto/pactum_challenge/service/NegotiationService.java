package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.entity.OfferTerm;
import com.pareto.pactum_challenge.repository.NegotiatorTermPreferenceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NegotiationService {
    private final NegotiatorTermPreferenceRepository preferenceRepository;
    private final ScoringService scoringService;

    public void handleOffer(Offer offer, NegotiationContext context) {
        double score = scoringService.score(offer, getAllPreferencesByNegotiator(context.negotiator().getId()));
        int currentRound = context.offers().size();
        double threshold = scoringService.adjustedThreshold(context.negotiator(), currentRound);
    }

    public List<NegotiatorTermPreference> getAllPreferencesByNegotiator(Long id) {
        return preferenceRepository.findAllByNegotiatorId(id);
    }
}
