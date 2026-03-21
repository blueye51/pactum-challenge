package com.pareto.pactum_challenge.config;

import com.pareto.pactum_challenge.entity.NegotiationTerm;
import com.pareto.pactum_challenge.repository.NegotiationTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {

    private final NegotiationTermRepository termRepository;

    @Override
    public void run(String... args) {
        if (termRepository.count() > 0) return;

        termRepository.save(term("Unit Price", "USD", 0, 1_000_000_000,
                "Price per unit of product or service"));

        termRepository.save(term("Contract Length", "months", 1, 60,
                "Duration of the contract in months"));

        termRepository.save(term("Delivery Time", "days", 1, 180,
                "Time to deliver the goods or services"));

        termRepository.save(term("Payment Terms", "days", 1, 120,
                "Number of days until payment is due (e.g. Net 30, Net 60, Net 90)"));

        termRepository.save(term("Early Payment Discount", "%", 0, 10,
                "Discount percentage for paying before the due date"));

        termRepository.save(term("Late Payment Penalty", "%", 0, 10,
                "Penalty percentage applied per month for payments past the due date"));
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
}
