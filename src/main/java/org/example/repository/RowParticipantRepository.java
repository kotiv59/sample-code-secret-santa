package org.example.repository;

import org.example.entity.RowParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RowParticipantRepository extends JpaRepository<RowParticipant, Long> {
    List<RowParticipant> findAllByMailSentFalse();
}
