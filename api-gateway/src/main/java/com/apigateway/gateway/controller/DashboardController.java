package com.apigateway.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @GetMapping("/")
    public Mono<String> index(Model model) {
        model.addAttribute("activeTab", "overview");
        model.addAttribute("pageTitle", "API Gateway & Microservices Platform");
        return Mono.just("index");
    }

    @GetMapping("/sandbox")
    public Mono<String> sandbox(Model model) {
        model.addAttribute("activeTab", "sandbox");
        model.addAttribute("pageTitle", "Interactive API Sandbox");
        return Mono.just("sandbox");
    }

    @GetMapping("/resilience")
    public Mono<String> resilience(Model model) {
        model.addAttribute("activeTab", "resilience");
        model.addAttribute("pageTitle", "Resilience & Rate Limiter Lab");
        return Mono.just("resilience");
    }

    @GetMapping("/security")
    public Mono<String> security(Model model) {
        model.addAttribute("activeTab", "security");
        model.addAttribute("pageTitle", "AI Threat & Anomaly Radar");
        return Mono.just("security");
    }

    @GetMapping("/docs")
    public Mono<String> docs(Model model) {
        model.addAttribute("activeTab", "docs");
        model.addAttribute("pageTitle", "API Documentation & Endpoints");
        return Mono.just("docs");
    }
}
