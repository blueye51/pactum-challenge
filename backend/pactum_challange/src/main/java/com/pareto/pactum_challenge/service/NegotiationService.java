package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.entity.OfferTerm;
import com.pareto.pactum_challenge.negotiation.NegotiationNode;
import com.pareto.pactum_challenge.negotiation.NegotiationTree;
import com.pareto.pactum_challenge.repository.NegotiatorTermPreferenceRepository;
import com.pareto.pactum_challenge.repository.OfferTermRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NegotiationService {
    private final NegotiatorTermPreferenceRepository preferenceRepository;
    private final NegotiationTree negotiationTree;
    private final List<NegotiationNode> nodeTree = List.of(
            progressCheckNode,
            scoringNode,
            maxRoundsNode,
            counterOfferNode
    );
    private final OfferTermRepository offerTermRepository;

    public NegotiationService(NegotiatorTermPreferenceRepository preferenceRepository, ScoringService scoringService, NegotiationTree negotiationTree, OfferTermRepository offerTermRepository) {
        this.preferenceRepository = preferenceRepository;
        this.negotiationTree = negotiationTree;
        this.offerTermRepository = offerTermRepository;
    }


    public NegotiationResult handleOffer(Offer offer, NegotiationContext context) {


        return negotiationTree.evaluate(offer, context);
    }

    public List<NegotiatorTermPreference> getAllPreferencesByNegotiator(Negotiator negotiator) {
        return preferenceRepository.findAllByNegotiatorId(negotiator.getId());
    }

    public List<OfferTerm> getAllOfferTermsByOffer(Offer offer) {
        return offerTermRepository.findAllByOfferId(offer.getId());
    }
}
