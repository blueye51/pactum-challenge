package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.entity.OfferTerm;
import com.pareto.pactum_challenge.repository.NegotiatorTermPreferenceRepository;
import com.pareto.pactum_challenge.repository.OfferTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NegotiationDataService {
    private final NegotiatorTermPreferenceRepository preferenceRepository;
    private final OfferTermRepository offerTermRepository;

    public List<NegotiatorTermPreference> getAllPreferencesByNegotiator(Negotiator negotiator) {
        return preferenceRepository.findAllByNegotiatorId(negotiator.getId());
    }

    public List<OfferTerm> getAllOfferTermsByOffer(Offer offer) {
        return offerTermRepository.findAllByOfferId(offer.getId());
    }
}
