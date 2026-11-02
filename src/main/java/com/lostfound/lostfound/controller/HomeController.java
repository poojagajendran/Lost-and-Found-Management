package com.lostfound.lostfound.controller;

import com.lostfound.lostfound.model.ItemStatus;
import com.lostfound.lostfound.model.ItemType;
import com.lostfound.lostfound.service.ClaimService;
import com.lostfound.lostfound.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ItemService itemService;
    private final ClaimService claimService;

    public HomeController(ItemService itemService, ClaimService claimService) {
        this.itemService = itemService;
        this.claimService = claimService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentItems", itemService.getRecentItems());
        model.addAttribute("totalLost", itemService.countByType(ItemType.LOST));
        model.addAttribute("totalFound", itemService.countByType(ItemType.FOUND));
        model.addAttribute("totalReturned", itemService.countByStatus(ItemStatus.RETURNED));
        return "home";
    }
}
