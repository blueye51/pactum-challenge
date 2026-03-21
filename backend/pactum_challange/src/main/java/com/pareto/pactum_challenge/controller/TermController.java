package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.entity.NegotiationTerm;
import com.pareto.pactum_challenge.repository.NegotiationTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final NegotiationTermRepository termRepository;

    @GetMapping
    public List<NegotiationTerm> listAll() {
        return termRepository.findAll();
    }
}
