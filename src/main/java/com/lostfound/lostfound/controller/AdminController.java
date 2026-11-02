package com.lostfound.lostfound.controller;

import com.lostfound.lostfound.dto.ClaimDecisionDto;
import com.lostfound.lostfound.model.*;
import com.lostfound.lostfound.service.ClaimService;
import com.lostfound.lostfound.service.ItemService;
import com.lostfound.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ClaimService claimService;
    private final ItemService itemService;
    private final UserService userService;

    public AdminController(ClaimService claimService, ItemService itemService, UserService userService) {
        this.claimService = claimService;
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalLost", itemService.countByType(ItemType.LOST));
        model.addAttribute("totalFound", itemService.countByType(ItemType.FOUND));
        model.addAttribute("pendingClaimsCount", claimService.countByStatus(ClaimStatus.PENDING));
        model.addAttribute("approvedClaimsCount", claimService.countByStatus(ClaimStatus.APPROVED));
        model.addAttribute("rejectedClaimsCount", claimService.countByStatus(ClaimStatus.REJECTED));
        model.addAttribute("totalReturned", itemService.countByStatus(ItemStatus.RETURNED));

        model.addAttribute("pendingClaims", claimService.getClaimsByStatus(ClaimStatus.PENDING));
        model.addAttribute("allClaims", claimService.getAllClaims());

        return "admin/dashboard";
    }

    @GetMapping("/claims/{id}")
    public String reviewClaim(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Claim> claimOpt = claimService.getClaimById(id);
        if (claimOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Claim not found.");
            return "redirect:/admin/dashboard";
        }

        Claim claim = claimOpt.get();
        List<PrivateClue> privateClues = itemService.getPrivateCluesForItem(claim.getItem().getId());

        model.addAttribute("claim", claim);
        model.addAttribute("item", claim.getItem());
        model.addAttribute("privateClues", privateClues);
        if (!model.containsAttribute("decisionDto")) {
            model.addAttribute("decisionDto", new ClaimDecisionDto());
        }

        return "admin/claim-review";
    }

    @PostMapping("/claims/{id}/approve")
    public String approveClaim(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("decisionDto") ClaimDecisionDto decisionDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Optional<Claim> claimOpt = claimService.getClaimById(id);
            if (claimOpt.isPresent()) {
                Claim claim = claimOpt.get();
                model.addAttribute("claim", claim);
                model.addAttribute("item", claim.getItem());
                model.addAttribute("privateClues", itemService.getPrivateCluesForItem(claim.getItem().getId()));
                return "admin/claim-review";
            }
            return "redirect:/admin/dashboard";
        }

        User currentAdmin = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin user not found."));

        try {
            claimService.approveClaim(id, currentAdmin, decisionDto.getDecisionReason());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Claim #" + id + " has been approved! The item has been marked as RETURNED to the owner.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/claims/{id}/reject")
    public String rejectClaim(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("decisionDto") ClaimDecisionDto decisionDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Optional<Claim> claimOpt = claimService.getClaimById(id);
            if (claimOpt.isPresent()) {
                Claim claim = claimOpt.get();
                model.addAttribute("claim", claim);
                model.addAttribute("item", claim.getItem());
                model.addAttribute("privateClues", itemService.getPrivateCluesForItem(claim.getItem().getId()));
                return "admin/claim-review";
            }
            return "redirect:/admin/dashboard";
        }

        User currentAdmin = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin user not found."));

        try {
            claimService.rejectClaim(id, currentAdmin, decisionDto.getDecisionReason());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Claim #" + id + " has been rejected with decision reason recorded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/items/{id}/mark-returned")
    public String markReturned(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            itemService.markItemAsReturned(id);
            redirectAttributes.addFlashAttribute("successMessage", "Item #" + id + " has been marked as RETURNED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}
