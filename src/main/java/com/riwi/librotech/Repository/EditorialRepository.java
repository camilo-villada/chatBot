package com.riwi.librotech.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.riwi.librotech.model.Editorial;

@Repository
public interface EditorialRepository extends JpaRepository<Editorial, Long> {
}
