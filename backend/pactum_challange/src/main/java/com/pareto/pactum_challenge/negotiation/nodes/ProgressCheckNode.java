package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.negotiation.NegotiationNode;

public class ProgressCheckNode implements NegotiationNode {
    // compare supplier's latest offer score to their previous offer score
    // if worse or same → REJECT ("you're moving in the wrong direction")
    // if better → CONTINUE (let other nodes decide)

}
