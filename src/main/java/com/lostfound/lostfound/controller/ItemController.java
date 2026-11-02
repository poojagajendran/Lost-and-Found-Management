package com.lostfound.lostfound.controller;

import com.lostfound.lostfound.dto.ItemReportDto;
import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.ItemStatus;
import com.lostfound.lostfound.model.ItemType;
import com.lostfound.lostfound.model.User;
import com.lostfound.lostfound.repository.ClaimRepository;
import com.lostfound.lostfound.service.ItemService;
import com.lostfound.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final ClaimRepository claimRepository;

    private static final List<String> CATEGORIES = Arrays.asList(
            "Electronics",
            "Documents & IDs",
            "Wallets & Bags",
            "Keys & Access Cards",
            "Clothing & Apparel",
            "Jewelry & Watches",
            "Books & Stationery",
            "Other"
    );

    public ItemController(ItemService itemService, UserService userService, ClaimRepository claimRepository) {
        this.itemService = itemService;
        this.userService = userService;
        this.claimRepository = claimRepository;
    }

    @GetMapping("/items")
    public String browseItems(
            @RequestParam(name = "type", required = false) ItemType type,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) ItemStatus status,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        List<Item> items = itemService.searchItems(type, category, status, keyword);

        model.addAttribute("items", items);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", CATEGORIES);

        return "items";
    }

    @GetMapping("/items/{id}")
    public String viewItemDetails(
            @PathVariable("id") Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Item> itemOpt = itemService.getItemById(id);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "The requested item was not found.");
            return "redirect:/items";
        }

        Item item = itemOpt.get();
        model.addAttribute("item", item);

        boolean isReporter = false;
        boolean hasActiveClaim = false;

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> currentUserOpt = userService.findByEmail(authentication.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                if (item.getReporter() != null && item.getReporter().getId().equals(currentUser.getId())) {
                    isReporter = true;
                }
                hasActiveClaim = claimRepository.existsByItemIdAndClaimantIdAndStatus(
                        item.getId(), currentUser.getId(), com.lostfound.lostfound.model.ClaimStatus.PENDING
                ) || claimRepository.existsByItemIdAndClaimantIdAndStatus(
                        item.getId(), currentUser.getId(), com.lostfound.lostfound.model.ClaimStatus.APPROVED
                );
            }
        }

        model.addAttribute("isReporter", isReporter);
        model.addAttribute("hasActiveClaim", hasActiveClaim);

        return "item-details";
    }

    @GetMapping({"/report", "/items/report"})
    public String showReportForm(
            @RequestParam(name = "type", required = false) ItemType initialType,
            Model model) {

        if (!model.containsAttribute("itemDto")) {
            ItemReportDto dto = new ItemReportDto();
            if (initialType != null) {
                dto.setType(initialType);
            }
            model.addAttribute("itemDto", dto);
        }

        model.addAttribute("categories", CATEGORIES);
        return "report";
    }

    @PostMapping({"/report", "/items/report"})
    public String submitReport(
            @Valid @ModelAttribute("itemDto") ItemReportDto itemDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            return "report";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        Item savedItem = itemService.reportItem(itemDto, currentUser);

        redirectAttributes.addFlashAttribute("successMessage",
                "Your " + savedItem.getType() + " item report has been published successfully!");
        return "redirect:/items/" + savedItem.getId();
    }
}
