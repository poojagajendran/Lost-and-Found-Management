package com.lostfound.lostfound.dto;

import com.lostfound.lostfound.model.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ItemReportDto {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Category is required")
    private String category;

    private String color;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate itemDate;

    private String description;

    @NotNull(message = "Report type is required")
    private ItemType type = ItemType.LOST;

    // Optional private clue provided by the reporter when reporting a FOUND item
    private String privateClue;

    public ItemReportDto() {
        this.itemDate = LocalDate.now();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getPrivateClue() {
        return privateClue;
    }

    public void setPrivateClue(String privateClue) {
        this.privateClue = privateClue;
    }
}
