package com.lostfound.lostfound.repository;

import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.PrivateClue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateClueRepository extends JpaRepository<PrivateClue, Long> {
    List<PrivateClue> findByItemId(Long itemId);
    List<PrivateClue> findByItem(Item item);
}
