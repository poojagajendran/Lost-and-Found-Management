package com.lostfound.lostfound.controller;

import com.lostfound.lostfound.dto.ClaimSubmissionDto;
import com.lostfound.lostfound.model.Claim;
import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.ItemType;
import com.lostfound.lostfound.model.User;
import com.lostfound.lostfound.service.ClaimService;
import com.lostfound.lostfound.service.ItemService;
import com.lostfound.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final ItemService itemService;
    private final UserService userService;

    public ClaimController(ClaimService claimService, ItemService itemService, UserService userService) {
        this.claimService = claimService;
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/items/{id}/claim")
    public String showClaimForm(
            @PathVariable("id") Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Item> itemOpt = itemService.getItemById(id);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/items";
        }

        Item item = itemOpt.get();
        if (item.getType() != ItemType.FOUND) {
            redirectAttributes.addFlashAttribute("errorMessage", "Claims can only be filed for FOUND items.");
            return "redirect:/items/" + id;
        }

        User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found in database."));

        if (item.getReporter() != null && item.getReporter().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot claim an item you reported finding.");
            return "redirect:/items/" + id;
        }

        if (!model.containsAttribute("claimDto")) {
            model.addAttribute("claimDto", new ClaimSubmissionDto());
        }

        model.addAttribute("item", item);
        return "claim";
    }

    @PostMapping("/items/{id}/claim")
    public String submitClaim(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("claimDto") ClaimSubmissionDto claimDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Item> itemOpt = itemService.getItemById(id);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/items";
        }
        Item item = itemOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "claim";
        }

        User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found."));

        try {
            claimService.submitClaim(id, claimDto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ownership claim submitted successfully! An administrator will privately inspect the clues and verify your evidence.");
            return "redirect:/my-claims";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("item", item);
            model.addAttribute("errorMessage", e.getMessage());
            return "claim";
        }
    }

    @GetMapping("/my-claims")
    public String viewMyClaims(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found."));

        List<Claim> myClaims = claimService.getClaimsByClaimant(currentUser);
        model.addAttribute("claims", myClaims);

        return "my-claims";
    }
}
