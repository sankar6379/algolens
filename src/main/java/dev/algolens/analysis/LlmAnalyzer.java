package dev.algolens.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.algolens.user.Language;
import java.time.Duration;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LlmAnalyzer {
  private static final Logger log = LoggerFactory.getLogger(LlmAnalyzer.class);
  
  public record Approach(String id,String name,String complexity,String reason,String clue,String code){}
  public record Result(String timeComplexity,String spaceComplexity,String verdict,String primaryPattern,List<String> signals,List<Approach> approaches){}
  
  private final String provider;
  private final String apiUrl;
  private final String token;
  private final String model;
  private final RestClient.Builder restClientBuilder;
  private final ObjectMapper json;

  public LlmAnalyzer(
      @Value("${algolens.ai.provider:github}") String provider,
      @Value("${algolens.ai.api-url:https://models.github.ai/inference/chat/completions}") String apiUrl,
      @Value("${algolens.ai.token:}") String token,
      @Value("${algolens.ai.model:meta/Llama-4-Scout-17B-16E-Instruct}") String model,
      RestClient.Builder restClientBuilder,
      ObjectMapper json) {
    this.provider = provider.trim().toLowerCase(Locale.ROOT);
    this.apiUrl = apiUrl.trim();
    this.token = token.trim();
    this.model = model.trim();
    this.restClientBuilder = restClientBuilder;
    this.json = json;
  }

  public boolean isEnabled() {
    return !token.isBlank() || "ollama".equals(provider) ||
           (!apiUrl.contains("github.ai") && !apiUrl.contains("generativelanguage.googleapis.com") &&
            !apiUrl.contains("groq.com") && !apiUrl.contains("openrouter.ai"));
  }

  public Result analyze(String source, Language language) {
    if ("github".equals(provider) && token.isBlank()) {
      throw new IllegalStateException("GitHub Models token is not configured on the server. Please check application.properties");
    }
    if ("gemini".equals(provider) && token.isBlank()) {
      throw new IllegalStateException("Gemini API key is not configured on the server. Please check application.properties");
    }
    if ("groq".equals(provider) && token.isBlank()) {
      throw new IllegalStateException("Groq API key is not configured. Please check application.properties");
    }
    if ("openrouter".equals(provider) && token.isBlank()) {
      throw new IllegalStateException("OpenRouter API key is not configured. Please check application.properties");
    }
    if (!"ollama".equals(provider) && !"github".equals(provider) && !"gemini".equals(provider) && !"groq".equals(provider) && !"openrouter".equals(provider) && token.isBlank()) {
      log.warn("Running LLM analysis on a custom provider without an API token.");
    }

    Map<String, Object> body = Map.of(
        "model", model,
        "temperature", 0.1,
        "max_tokens", 3000,
        "messages", List.of(
            Map.of("role", "system", "content", systemPrompt(language)),
            Map.of("role", "user", "content", "Analyze only this submitted code:\n\n" + source)
        )
    );

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(6));
    requestFactory.setReadTimeout(Duration.ofSeconds(30));

    // Parse API URL into base URL and URI path to build RestClient
    String baseUrl;
    String uriPath;
    try {
      java.net.URI uri = new java.net.URI(apiUrl);
      String scheme = uri.getScheme();
      String authority = uri.getAuthority();
      baseUrl = scheme + "://" + authority;
      uriPath = uri.getPath();
      if (uri.getQuery() != null) {
        uriPath += "?" + uri.getQuery();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid LLM Endpoint URL: " + apiUrl, e);
    }

    RestClient client = restClientBuilder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    RestClient.RequestBodySpec requestSpec = client.post().uri(uriPath)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON);

    // Add authorization header if token is present
    if (!token.isBlank()) {
      requestSpec.header("Authorization", "Bearer " + token);
    }

    // Inject version header for GitHub Models API
    if (apiUrl.contains("github.ai") || apiUrl.contains("github.com")) {
      requestSpec.header("X-GitHub-Api-Version", "2026-03-10");
    }

    // Inject referer & title headers for OpenRouter rankings
    if (apiUrl.contains("openrouter.ai")) {
      requestSpec.header("HTTP-Referer", "https://algolens.app");
      requestSpec.header("X-Title", "AlgoLens");
    }

    JsonNode response = requestSpec.body(body).retrieve().body(JsonNode.class);
    if (response == null) {
      throw new IllegalStateException("LLM endpoint returned an empty response");
    }

    String content = response.path("choices").path(0).path("message").path("content").asText();
    if (content.isBlank()) {
      throw new IllegalStateException("LLM endpoint returned empty content");
    }

    return parse(stripFence(content), source);
  }

  private Result parse(String content, String source) {
    try {
      JsonNode root = json.readTree(content);
      String time = required(root, "timeComplexity", 30);
      String space = required(root, "spaceComplexity", 40);
      String verdict = required(root, "verdict", 500);

      List<String> signals = new ArrayList<>();
      root.path("signals").forEach(n -> {
        if (signals.size() < 5 && !n.asText().isBlank()) {
          signals.add(limit(n.asText(), 180));
        }
      });

      List<Approach> approaches = new ArrayList<>();
      JsonNode list = root.path("approaches");
      if (!list.isArray() || list.isEmpty()) {
        throw new IllegalArgumentException("No approaches returned");
      }

      for (JsonNode n : list) {
        if (approaches.size() >= 4 || !n.path("applicable").asBoolean(true)) {
          continue;
        }
        String name = required(n, "name", 80);
        String code = n.path("code").asText("").trim();
        if (code.length() > 12000) {
          code = code.substring(0, 12000);
        }
        approaches.add(new Approach(
            slug(name),
            name,
            required(n, "complexity", 100),
            required(n, "reason", 500),
            limit(n.path("clue").asText("AI-supported match"), 180),
            code
        ));
      }

      if (approaches.isEmpty()) {
        throw new IllegalArgumentException("No applicable approaches returned");
      }

      Approach first = approaches.get(0);
      approaches.set(0, new Approach(first.id(), first.name(), first.complexity(), first.reason(), first.clue(), source));
      return new Result(time, space, verdict, first.id(), List.copyOf(signals), List.copyOf(approaches));
    } catch (Exception e) {
      log.error("Failed to parse LLM response: {}", content, e);
      throw new IllegalStateException("Invalid structured analysis from LLM endpoint", e);
    }
  }

  private String systemPrompt(Language language) {
    return """
      You are AlgoLens, a precise DSA code reviewer. Analyze the submitted %s code, not a guessed problem.
      Return ONLY one JSON object with this exact shape:
      {"timeComplexity":"O(...) or Theta(...)","spaceComplexity":"O(...)","verdict":"short explanation","signals":["concrete evidence from code"],"approaches":[{"name":"detected approach","complexity":"O(...) time - O(...) space","reason":"why it works here","clue":"evidence","code":"complete %s code","applicable":true}]}
      Rules:
      - approaches[0] must describe the submitted implementation and its actual complexity.
      - When the task is inferable from method names, parameters, return type and code, include the sensible brute-force/baseline approach and every genuinely distinct common DSA approach, up to 4 total.
      - Put alternatives after the detected approach, ordered from most efficient to least efficient. State trade-offs in reason.
      - Start the optimal approach clue with "RECOMMENDED: ", the submitted approach clue with "DETECTED: ", and a baseline clue with "BRUTE FORCE: ".
      - Never add an alternative merely because it is a known pattern. It must solve the same inferred task.
      - Never invent a problem statement, return value, constraints, or unrelated pattern. Do not call every loop two-pointers or every array problem sliding-window.
      - Account for recursion, allocations, copied arrays, collections, graph vertices/edges, and average versus worst case.
      - Return at most 4 approaches. All code must be complete and in %s. If intent truly cannot be inferred, return only the detected approach.
      """.formatted(language, language, language);
  }

  private String required(JsonNode node, String field, int max) {
    String value = node.path(field).asText("").trim();
    if (value.isBlank()) throw new IllegalArgumentException("Missing " + field);
    return limit(value, max);
  }

  private String limit(String value, int max) {
    return value.length() <= max ? value : value.substring(0, max);
  }

  private String slug(String value) {
    String s = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    return s.isBlank() ? "ai-pattern" : s;
  }

  private String stripFence(String value) {
    String s = value.trim();
    if (s.startsWith("```")) {
      s = s.replaceFirst("^```(?:json)?\\s*", "");
      s = s.replaceFirst("\\s*```$", "");
    }
    return s;
  }
}
