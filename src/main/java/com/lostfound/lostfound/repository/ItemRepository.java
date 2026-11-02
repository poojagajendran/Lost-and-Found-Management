package com.lostfound.lostfound.repository;

import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.ItemStatus;
import com.lostfound.lostfound.model.ItemType;
import com.lostfound.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByTypeOrderByCreatedAtDesc(ItemType type);

    List<Item> findByReporterOrderByCreatedAtDesc(User reporter);

    List<Item> findTop6ByOrderByCreatedAtDesc();

    long countByType(ItemType type);

    long countByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i WHERE " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(i.category) = LOWER(:category)) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(i.color) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY i.createdAt DESC")
    List<Item> searchItems(
            @Param("type") ItemType type,
            @Param("category") String category,
            @Param("status") ItemStatus status,
            @Param("keyword") String keyword
    );
}
