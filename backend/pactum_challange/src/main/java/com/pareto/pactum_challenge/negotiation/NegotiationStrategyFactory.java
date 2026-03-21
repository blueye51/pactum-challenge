package com.pareto.pactum_challenge.negotiation;

import com.pareto.pactum_challenge.entity.Strategy;
import com.pareto.pactum_challenge.negotiation.nodes.CounterOfferNode;
import com.pareto.pactum_challenge.negotiation.nodes.MaxRoundsNode;
import com.pareto.pactum_challenge.negotiation.nodes.ProgressCheckNode;
import com.pareto.pactum_challenge.negotiation.nodes.ScoringNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NegotiationStrategyFactory {

    private final ProgressCheckNode progressCheckNode;
    private final ScoringNode scoringNode;
    private final MaxRoundsNode maxRoundsNode;
    private final CounterOfferNode counterOfferNode;

    public NegotiationTree create(Strategy strategy) {
        return switch (strategy) {
            case INFINITE -> new NegotiationTree(List.of(
                    progressCheckNode,
                    scoringNode,
                    counterOfferNode
            ));
            case BALANCED -> new NegotiationTree(List.of(
                    maxRoundsNode,
                    scoringNode,
                    progressCheckNode,
                    counterOfferNode
            ));
            case CONCEDING -> new NegotiationTree(List.of(
                    maxRoundsNode,
                    scoringNode,
                    counterOfferNode
            ));
        };
    }
}
