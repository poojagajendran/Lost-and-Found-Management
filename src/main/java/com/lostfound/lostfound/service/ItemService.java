package com.lostfound.lostfound.service;

import com.lostfound.lostfound.dto.ItemReportDto;
import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.ItemStatus;
import com.lostfound.lostfound.model.ItemType;
import com.lostfound.lostfound.model.PrivateClue;
import com.lostfound.lostfound.model.User;
import com.lostfound.lostfound.repository.ItemRepository;
import com.lostfound.lostfound.repository.PrivateClueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final PrivateClueRepository privateClueRepository;

    public ItemService(ItemRepository itemRepository, PrivateClueRepository privateClueRepository) {
        this.itemRepository = itemRepository;
        this.privateClueRepository = privateClueRepository;
    }

    @Transactional
    public Item reportItem(ItemReportDto dto, User reporter) {
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter user must be authenticated.");
        }

        Item item = new Item();
        item.setItemName(dto.getItemName().trim());
        item.setCategory(dto.getCategory().trim());
        item.setColor(dto.getColor() != null ? dto.getColor().trim() : null);
        item.setLocation(dto.getLocation().trim());
        item.setItemDate(dto.getItemDate());
        item.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        item.setType(dto.getType());
        item.setStatus(ItemStatus.OPEN);
        item.setReporter(reporter);

        // Save item first
        Item savedItem = itemRepository.save(item);

        // If the item is FOUND and a private identifying clue was entered by finder:
        if (dto.getType() == ItemType.FOUND && dto.getPrivateClue() != null && !dto.getPrivateClue().trim().isEmpty()) {
            PrivateClue clue = new PrivateClue(savedItem, dto.getPrivateClue().trim());
            privateClueRepository.save(clue);
            savedItem.addPrivateClue(clue);
        }

        return savedItem;
    }

    @Transactional(readOnly = true)
    public List<Item> searchItems(ItemType type, String category, ItemStatus status, String keyword) {
        String trimmedCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return itemRepository.searchItems(type, trimmedCategory, status, trimmedKeyword);
    }

    @Transactional(readOnly = true)
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Item> getRecentItems() {
        return itemRepository.findTop6ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsByReporter(User reporter) {
        return itemRepository.findByReporterOrderByCreatedAtDesc(reporter);
    }

    @Transactional
    public Item markItemAsReturned(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + itemId + " not found."));
        item.setStatus(ItemStatus.RETURNED);
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<PrivateClue> getPrivateCluesForItem(Long itemId) {
        return privateClueRepository.findByItemId(itemId);
    }

    @Transactional(readOnly = true)
    public long countByType(ItemType type) {
        return itemRepository.countByType(type);
    }

    @Transactional(readOnly = true)
    public long countByStatus(ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countTotalItems() {
        return itemRepository.count();
    }
}
