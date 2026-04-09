package com.jahnavi.api.repository;

import com.jahnavi.api.model.Item;
import com.jahnavi.api.model.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByStatus(ItemStatus status, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Item> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
