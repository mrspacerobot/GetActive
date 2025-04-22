package com.sportsync.user;

import com.sportsync.activity.Activity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @OneToMany(mappedBy = "creator")
  @Builder.Default
  private Set<Activity> createdActivities = new HashSet<>();

  @ManyToMany(mappedBy = "participants")
  @Builder.Default
  private Set<Activity> joinedActivities = new HashSet<>();
}
