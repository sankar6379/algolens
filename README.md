# AlgoLens

AlgoLens is a portfolio-ready DSA learning platform that turns a submitted solution into an explainable optimization report. It identifies likely time/space complexity, recommends relevant problem-solving patterns, and compares brute-force and optimized approaches in Java, C++, or Python.

## Highlights

- Premium responsive dashboard with an interactive complexity chart
- Register/login flow with one immutable coding language per account
- Heuristic code analyzer for loops, sorting, recursion, hash structures, and common patterns
- Pattern library with rationale, complexity, and language-specific templates
- MySQL-backed users and analysis history
- Safe architecture: source analysis is local; untrusted code execution is deliberately separated for a future sandbox/Judge0 integration

## Run locally

1. Create MySQL database `algolens`.
2. Set `DB_USERNAME` and `DB_PASSWORD` (defaults are `root` and `root`).
3. Run `mvn spring-boot:run` and open `http://localhost:8080`.

The UI also works as a polished demo when opening `src/main/resources/static/index.html`; sample analysis is shown without requiring an account.

## Resume-ready description

> Built AlgoLens, a full-stack DSA optimization coach using Java, Spring Boot, MySQL, HTML, CSS, and JavaScript. Designed a heuristic static-analysis engine that detects algorithmic patterns and estimates time/space complexity, with explainable comparisons across brute-force and optimized approaches in Java, C++, and Python.

## Production roadmap

- Run code in isolated containers or Judge0—not inside the Spring Boot process
- Add an LLM provider behind a server-side interface for richer explanations
- Add test-case execution, rate limiting, email verification, and OAuth
- Add community DSA sheets and shareable analysis reports
