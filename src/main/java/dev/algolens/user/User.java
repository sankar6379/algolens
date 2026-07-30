package dev.algolens.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="app_users", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, length=80) private String name;
  @Column(nullable=false) private String email;
  @Column(nullable=false) private String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false, updatable=false) private Language language;
  @Column(nullable=false, updatable=false) private Instant createdAt = Instant.now();
  protected User() {}
  public User(String name,String email,String passwordHash,Language language){this.name=name;this.email=email;this.passwordHash=passwordHash;this.language=language;}
  public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
  public String getPasswordHash(){return passwordHash;} public Language getLanguage(){return language;}
}
