package com.example.english.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {
  @Id
  private String name;
  private String description;

  @ManyToMany
  Set<Permission> permissions;
}

