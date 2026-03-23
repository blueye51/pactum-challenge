package com.pareto.pactum_challenge.config;

import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.repository.NegotiationTermRepository;
import com.pareto.pactum_challenge.repository.NegotiatorRepository;
import com.pareto.pactum_challenge.repository.NegotiatorTermPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {

    private final NegotiationTermRepository termRepository;
    private final NegotiatorRepository negotiatorRepository;
    private final NegotiatorTermPreferenceRepository preferenceRepository;

    @Override
    public void run(String... args) {
        if (termRepository.count() > 0) return;

        NegotiationTerm unitPrice = termRepository.save(term("Unit Price", "USD", 0, 1_000_000_000,
                "Price per unit of product or service"));

        NegotiationTerm contractLength = termRepository.save(term("Contract Length", "months", 1, 60,
                "Duration of the contract in months"));

        NegotiationTerm deliveryTime = termRepository.save(term("Delivery Time", "days", 1, 180,
                "Time to deliver the goods or services"));

        NegotiationTerm paymentTerms = termRepository.save(term("Payment Terms", "days", 1, 120,
                "Number of days until payment is due (e.g. Net 30, Net 60, Net 90)"));

        termRepository.save(term("Early Payment Discount", "%", 0, 10,
                "Discount percentage for paying before the due date"));

        termRepository.save(term("Late Payment Penalty", "%", 0, 10,
                "Penalty percentage applied per month for payments past the due date"));

        // Demo negotiator
        Negotiator demo = new Negotiator();
        demo.setName("BudgetBuyer");
        demo.setAcceptanceThreshold(0.7);
        demo.setWalkawayThreshold(0.3);
        demo.setConcessionRate(0.05);
        demo.setMaxOffersCount(10);
        demo.setStrategy(Strategy.BALANCED);
        demo = negotiatorRepository.save(demo);

        // Wants low price — high weight, low strictness (willing to negotiate but it matters a lot)
        preferenceRepository.save(pref(demo, unitPrice, Direction.MINIMIZE, 50, 200, 0.4, 0.3,
                "We need to keep unit costs under budget"));

        // Wants short contract — moderate weight
        preferenceRepository.save(pref(demo, contractLength, Direction.MINIMIZE, 6, 24, 0.2, 0.5,
                "Shorter contracts reduce long-term risk"));

        // Wants fast delivery — moderate weight, fairly strict
        preferenceRepository.save(pref(demo, deliveryTime, Direction.MINIMIZE, 7, 60, 0.2, 0.7,
                "Operations need stock within 2 months"));

        // Wants late payment — low weight, flexible
        preferenceRepository.save(pref(demo, paymentTerms, Direction.MAXIMIZE, 90, 30, 0.2, 0.3,
                "Cash flow is better with longer payment windows"));
    }

    private NegotiationTerm term(String name, String unit, double min, double max, String description) {
        NegotiationTerm t = new NegotiationTerm();
        t.setName(name);
        t.setUnit(unit);
        t.setMin(min);
        t.setMax(max);
        t.setDescription(description);
        return t;
    }

    private NegotiatorTermPreference pref(Negotiator negotiator, NegotiationTerm term,
                                          Direction direction, double ideal, double limit,
                                          double weight, double strictness, String reasoning) {
        NegotiatorTermPreference p = new NegotiatorTermPreference();
        p.setNegotiator(negotiator);
        p.setNegotiationTerm(term);
        p.setDirection(direction);
        p.setIdealValue(ideal);
        p.setLimitValue(limit);
        p.setWeight(weight);
        p.setStrictness(strictness);
        p.setReasoning(reasoning);
        return p;
    }
}
