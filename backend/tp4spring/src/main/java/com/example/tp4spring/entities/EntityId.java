package com.example.tp4spring.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class EntityId {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
}
