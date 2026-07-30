package dev.algolens.analysis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AnalysisRepository extends JpaRepository<Analysis,Long>{List<Analysis> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);}
