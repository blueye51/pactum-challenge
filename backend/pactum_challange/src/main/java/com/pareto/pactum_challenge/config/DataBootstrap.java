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
                "Price per unit of product or service", false));

        NegotiationTerm contractLength = termRepository.save(term("Contract Length", "months", 1, 60,
                "Duration of the contract in months", true));

        NegotiationTerm deliveryTime = termRepository.save(term("Delivery Time", "days", 1, 180,
                "Time to deliver the goods or services", true));

        NegotiationTerm paymentTerms = termRepository.save(term("Payment Terms", "days", 1, 120,
                "Number of days until payment is due (e.g. Net 30, Net 60, Net 90)", true));

        termRepository.save(term("Early Payment Discount", "%", 0, 10,
                "Discount percentage for paying before the due date", false));

        termRepository.save(term("Late Payment Penalty", "%", 0, 10,
                "Penalty percentage applied per month for payments past the due date", false));

        // Demo negotiator — phone case buyer
        Negotiator demo = new Negotiator();
        demo.setName("CaseDeal");
        demo.setAcceptanceThreshold(0.65);
        demo.setWalkawayThreshold(0.25);
        demo.setConcessionRate(0.05);
        demo.setMaxOffersCount(10);
        demo.setStrategy(Strategy.BALANCED);
        demo.setMarketContext(
                "We're a mid-size electronics retail chain and we're looking to buy silicone phone cases " +
                "in bulk — around 10,000 units per order. " +
                "We've been sourcing from manufacturers in Shenzhen and typically pay around $3–$5 per case " +
                "for this quality tier. Some premium suppliers charge up to $8, but anything above $12 is a non-starter. " +
                "Delivery is usually 30–45 days by sea from China. We can do air freight for urgent restocks " +
                "but that eats into margins, so we prefer sea. " +
                "Payment is usually Net 30, but we've done Net 60 with trusted partners. " +
                "We'd like a 6-month initial contract to test the relationship, with the option to extend to 12+ months " +
                "if quality holds up. We order frequently so there's good repeat business potential here."
        );
        demo = negotiatorRepository.save(demo);

        // Wants low price — $3 ideal, $12 limit. This is the biggest deal-breaker.
        preferenceRepository.save(pref(demo, unitPrice, Direction.MINIMIZE, 3, 12, 0.4, 0.3,
                "We typically source phone cases at $3–$5 per unit. Market rate for this quality is around $4. " +
                "Anything over $10 makes it hard to hit our retail margin targets."));

        // Wants shorter contract — 6 months ideal, 24 months max
        preferenceRepository.save(pref(demo, contractLength, Direction.MINIMIZE, 6, 24, 0.15, 0.4,
                "We prefer a shorter trial period first. If quality and reliability are good, " +
                "we're happy to extend. We've been burned before locking into long contracts with new suppliers."));

        // Wants fast delivery — 14 days ideal, 60 days limit. Fairly strict.
        preferenceRepository.save(pref(demo, deliveryTime, Direction.MINIMIZE, 14, 60, 0.25, 0.6,
                "Our warehouses need restocking within 2 months max. Ideally 2 weeks for air freight. " +
                "We've lost sales before waiting on slow shipments."));

        // Wants late payment — Net 60 ideal, Net 30 limit. Flexible.
        preferenceRepository.save(pref(demo, paymentTerms, Direction.MAXIMIZE, 60, 30, 0.2, 0.3,
                "We prefer Net 60 to keep cash flow healthy, but Net 30 is standard and we can work with that."));

        // Second bot — used car buyer
        Negotiator carBot = new Negotiator();
        carBot.setName("AutoFleet");
        carBot.setAcceptanceThreshold(0.6);
        carBot.setWalkawayThreshold(0.2);
        carBot.setConcessionRate(0.04);
        carBot.setMaxOffersCount(8);
        carBot.setStrategy(Strategy.CONCEDING);
        carBot.setMarketContext(
                "We buy used sedans and SUVs for our corporate fleet. " +
                "Budget is $8,000–$15,000 per vehicle depending on mileage and condition. " +
                "We need delivery within 3 weeks and pay on Net 30."
        );
        carBot = negotiatorRepository.save(carBot);

        // Price per vehicle — wants low
        preferenceRepository.save(pref(carBot, unitPrice, Direction.MINIMIZE, 8000, 15000, 0.45, 0.5,
                "Market rate for fleet-grade used sedans is $10k–$12k. We buy in volume so expect a discount."));

        // Delivery time — wants fast
        preferenceRepository.save(pref(carBot, deliveryTime, Direction.MINIMIZE, 7, 21, 0.3, 0.7,
                "We need cars on the lot quickly to fill fleet gaps. Over 3 weeks causes operational issues."));

        // Payment terms — wants longer
        preferenceRepository.save(pref(carBot, paymentTerms, Direction.MAXIMIZE, 45, 14, 0.25, 0.3,
                "Net 45 is ideal for our accounting cycle but we can do Net 14 if the price is right."));
    }

    private NegotiationTerm term(String name, String unit, double min, double max, String description, boolean wholeNumber) {
        NegotiationTerm t = new NegotiationTerm();
        t.setName(name);
        t.setUnit(unit);
        t.setMin(min);
        t.setMax(max);
        t.setDescription(description);
        t.setWholeNumber(wholeNumber);
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
