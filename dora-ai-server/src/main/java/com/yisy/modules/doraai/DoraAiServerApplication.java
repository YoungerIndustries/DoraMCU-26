package com.yisy.modules.doraai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DoraAI Server — Standalone LLM and Voice AI microservice.
 * <p>
 * This is a separate Spring Boot application that provides AI services
 * (LLM chat, voice synthesis, voice commands, code generation) via REST APIs.
 * It runs independently from the main DoraMCU game server to isolate
 * AI resource usage (memory, threads) from game processing.
 * </p>
 * <p>
 * Services exposed:
 * <ul>
 *   <li>LLM Chat — Ollama, Gemini, DeepSeek integration</li>
 *   <li>Voice TTS — Text-to-speech synthesis</li>
 *   <li>Voice STT — Speech-to-text transcription</li>
 *   <li>Voice Commands — Voice command interpretation</li>
 *   <li>Code Generation — AI-assisted code generation</li>
 *   <li>Code Analysis — AI-assisted code analysis</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class DoraAiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoraAiServerApplication.class, args);
    }
}
