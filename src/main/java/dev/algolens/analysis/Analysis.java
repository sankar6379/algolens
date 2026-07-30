package dev.algolens.analysis;

import dev.algolens.user.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="analyses")
public class Analysis {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) private User user;
 @Lob @Column(nullable=false,columnDefinition="LONGTEXT") private String sourceCode;
 @Column(nullable=false) private String timeComplexity;
 @Column(nullable=false) private String spaceComplexity;
 @Column(nullable=false) private String primaryPattern;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 protected Analysis(){}
 public Analysis(User u,String code,String time,String space,String pattern){user=u;sourceCode=code;timeComplexity=time;spaceComplexity=space;primaryPattern=pattern;}
 public Long getId(){return id;} public String getTimeComplexity(){return timeComplexity;} public String getSpaceComplexity(){return spaceComplexity;} public String getPrimaryPattern(){return primaryPattern;} public Instant getCreatedAt(){return createdAt;}
}
