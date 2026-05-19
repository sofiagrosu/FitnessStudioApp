package com.fitness.fitness_app.repository;

import com.fitness.fitness_app.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptsRepository extends JpaRepository<Receipt, Long> {
    Receipt findByPayment_Id(Long paymentId);
}