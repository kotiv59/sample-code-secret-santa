package org.example.repository;

import org.example.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByEmail(String email);

    List<Participant> findAllByOrderByRegisteredAtAsc();
}
