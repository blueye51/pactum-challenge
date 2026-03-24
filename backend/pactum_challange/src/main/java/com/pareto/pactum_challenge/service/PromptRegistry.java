package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.entity.NegotiationSession;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.NegotiationTerm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromptRegistry {

    private final NegotiationDataService dataService;

    private String baseTemplate;
    private final Map<PromptMode, String> modeTemplates = new EnumMap<>(PromptMode.class);

    @PostConstruct
    void init() throws IOException {
        baseTemplate = loadResource("prompts/base.txt");
        for (PromptMode mode : PromptMode.values()) {
            modeTemplates.put(mode, loadResource(mode.getResourcePath()));
        }
    }

    public String buildSystemPrompt(NegotiationSession session, PromptMode mode) {
        return buildSystemPrompt(session, mode, Map.of());
    }

    public String buildSystemPrompt(NegotiationSession session, PromptMode mode, Map<String, String> extraVars) {
        Negotiator bot = session.getNegotiator();
        List<NegotiatorTermPreference> preferences = dataService.getAllPreferencesByNegotiator(bot);

        String marketCtx = bot.getMarketContext() != null ? bot.getMarketContext() : "No specific market context provided.";

        String filled = baseTemplate
                .replace("{{botName}}", bot.getName())
                .replace("{{marketContext}}", marketCtx)
                .replace("{{strategy}}", bot.getStrategy().name())
                .replace("{{maxOffers}}", String.valueOf(bot.getMaxOffersCount()))
                .replace("{{termBlock}}", buildTermBlock(preferences));

        String modeSection = modeTemplates.get(mode);
        for (var entry : extraVars.entrySet()) {
            modeSection = modeSection.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return filled + "\n\n" + modeSection;
    }

    private String buildTermBlock(List<NegotiatorTermPreference> preferences) {
        StringBuilder sb = new StringBuilder();
        for (NegotiatorTermPreference pref : preferences) {
            NegotiationTerm term = pref.getNegotiationTerm();
            sb.append("### %s (%s)\n".formatted(term.getName(), term.getUnit()));
            sb.append("- Range: %s to %s\n".formatted(fmt(term.getMin()), fmt(term.getMax())));
            sb.append("- You want to %s this value\n".formatted(pref.getDirection().name().toLowerCase()));

            double flexibility = (1 - pref.getWeight()) * (1 - pref.getStrictness());
            String flexLabel;
            if (flexibility < 0.15) flexLabel = "very low";
            else if (flexibility < 0.35) flexLabel = "low";
            else if (flexibility < 0.55) flexLabel = "moderate";
            else if (flexibility < 0.75) flexLabel = "high";
            else flexLabel = "very high";
            sb.append("- Your flexibility on this term: %s\n".formatted(flexLabel));

            String importanceLabel;
            if (pref.getWeight() >= 0.7) importanceLabel = "critical";
            else if (pref.getWeight() >= 0.4) importanceLabel = "important";
            else importanceLabel = "nice-to-have";
            sb.append("- Importance: %s\n".formatted(importanceLabel));

            if (pref.getReasoning() != null && !pref.getReasoning().isBlank()) {
                sb.append("- Why this matters to your company: %s\n".formatted(pref.getReasoning()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String fmt(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private static String loadResource(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
