package dev.algolens.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder;
  public AuthController(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
  public record RegisterRequest(@NotBlank String name,@Email String email,@Size(min=8) String password,@NotNull Language language){}
  public record LoginRequest(@Email String email,@NotBlank String password){}
  public record UserView(Long id,String name,String email,Language language){}
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
  UserView register(@Valid @RequestBody RegisterRequest r){
    if(users.findByEmailIgnoreCase(r.email()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
    User u=users.save(new User(r.name().trim(),r.email().toLowerCase(),encoder.encode(r.password()),r.language()));
    return new UserView(u.getId(),u.getName(),u.getEmail(),u.getLanguage());
  }
  @PostMapping("/login") UserView login(@Valid @RequestBody LoginRequest r){
    User u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
    if(!encoder.matches(r.password(),u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
    return new UserView(u.getId(),u.getName(),u.getEmail(),u.getLanguage());
  }
}
