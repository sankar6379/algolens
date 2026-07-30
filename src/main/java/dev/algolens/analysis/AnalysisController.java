package dev.algolens.analysis;

import dev.algolens.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController @RequestMapping("/api")
public class AnalysisController {
 private static final Logger log=LoggerFactory.getLogger(AnalysisController.class);
  private final CodeAnalyzer analyzer; private final LlmAnalyzer ai; private final UserRepository users; private final AnalysisRepository analyses;
  public AnalysisController(CodeAnalyzer a,LlmAnalyzer ai,UserRepository u,AnalysisRepository r){analyzer=a;this.ai=ai;users=u;analyses=r;}
  public record Request(@NotNull Long userId,@NotBlank @Size(max=30000) String sourceCode){}
  public record PatternView(String id,String name,String complexity,String reason,String clue,String code){}
  public record Response(Long id,String timeComplexity,String spaceComplexity,String verdict,String primaryPattern,List<String> signals,List<PatternView> patterns,String analysisSource){}

  @PostMapping("/analyze") Response analyze(@Valid @RequestBody Request request){
   User u=users.findById(request.userId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
   try{
    LlmAnalyzer.Result r=ai.analyze(request.sourceCode(),u.getLanguage());
    List<PatternView> views=new ArrayList<>();
    for(var p:r.approaches()){
     views.add(new PatternView(p.id(),p.name(),p.complexity(),p.reason(),p.clue(),p.code()));
    }
    Analysis saved=analyses.save(new Analysis(u,request.sourceCode(),r.timeComplexity(),r.spaceComplexity(),r.primaryPattern()));
    return new Response(saved.getId(),r.timeComplexity(),r.spaceComplexity(),r.verdict(),r.primaryPattern(),r.signals(),List.copyOf(views),"AI");
   }catch(RuntimeException unavailable){
   log.warn("GitHub Models analysis failed; using rule-based fallback: {}",unavailable.getMessage());
   CodeAnalyzer.Result r=analyzer.analyze(request.sourceCode());
   Analysis saved=analyses.save(new Analysis(u,request.sourceCode(),r.timeComplexity(),r.spaceComplexity(),r.primaryPattern()));
   List<PatternView> views=r.patternIds().stream().distinct().map(id->{var p=PatternCatalog.byId(id);String shownCode=id.equals(r.primaryPattern())?request.sourceCode():p.code().get(u.getLanguage());return new PatternView(p.id(),p.name(),p.complexity(),p.reason(),p.clue(),shownCode);}).toList();
   return new Response(saved.getId(),r.timeComplexity(),r.spaceComplexity(),r.verdict(),r.primaryPattern(),r.signals(),views,"RULES");
  }
 }
 @GetMapping("/patterns") List<PatternView> patterns(@RequestParam(defaultValue="JAVA") Language language){return PatternCatalog.ALL.stream().map(p->new PatternView(p.id(),p.name(),p.complexity(),p.reason(),p.clue(),p.code().get(language))).toList();}
 public record HistoryView(Long id,String timeComplexity,String spaceComplexity,String primaryPattern,Instant createdAt){}
 @GetMapping("/history/{userId}") List<HistoryView> history(@PathVariable Long userId){return analyses.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream().map(a->new HistoryView(a.getId(),a.getTimeComplexity(),a.getSpaceComplexity(),a.getPrimaryPattern(),a.getCreatedAt())).toList();}
}
