package com.lostfound.lostfound;

import com.lostfound.lostfound.dto.ClaimDecisionDto;
import com.lostfound.lostfound.dto.ClaimSubmissionDto;
import com.lostfound.lostfound.dto.ItemReportDto;
import com.lostfound.lostfound.dto.UserRegistrationDto;
import com.lostfound.lostfound.model.*;
import com.lostfound.lostfound.repository.ClaimRepository;
import com.lostfound.lostfound.repository.ItemRepository;
import com.lostfound.lostfound.repository.PrivateClueRepository;
import com.lostfound.lostfound.repository.UserRepository;
import com.lostfound.lostfound.service.ClaimService;
import com.lostfound.lostfound.service.ItemService;
import com.lostfound.lostfound.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LostFoundSystemTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PrivateClueRepository privateClueRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("User registration hashes password using BCrypt and assigns ROLE_USER")
    void testUserRegistrationAndBCrypt() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Alice Wonderland");
        dto.setEmail("alice@college.edu");
        dto.setPassword("SecretPass123");
        dto.setConfirmPassword("SecretPass123");

        User registered = userService.registerUser(dto);
        assertNotNull(registered.getId());
        assertEquals("alice@college.edu", registered.getEmail());
        assertEquals("ROLE_USER", registered.getRole());

        // Ensure password is not stored in plaintext and matches BCrypt hash
        assertNotEquals("SecretPass123", registered.getPasswordHash());
        assertTrue(passwordEncoder.matches("SecretPass123", registered.getPasswordHash()));

        // Ensure duplicate email is rejected
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    @DisplayName("Item reporting saves item and associates private clues when found")
    void testReportItemWithPrivateClues() {
        User finder = userRepository.findByEmail("admin@lostfound.edu")
                .orElseThrow(() -> new IllegalStateException("Default admin must exist"));

        ItemReportDto reportDto = new ItemReportDto();
        reportDto.setItemName("Sony Wireless Headphones");
        reportDto.setCategory("Electronics");
        reportDto.setColor("Matte Black");
        reportDto.setLocation("Central Library Desk 12");
        reportDto.setItemDate(LocalDate.now());
        reportDto.setDescription("Black over-ear headphones found on the second floor study cubicle.");
        reportDto.setType(ItemType.FOUND);
        reportDto.setPrivateClue("Right ear cup has a tiny white scratch near the volume dial.");

        Item savedItem = itemService.reportItem(reportDto, finder);
        assertNotNull(savedItem.getId());
        assertEquals(ItemType.FOUND, savedItem.getType());
        assertEquals(ItemStatus.OPEN, savedItem.getStatus());

        // Verify private clue was saved and linked
        List<PrivateClue> clues = itemService.getPrivateCluesForItem(savedItem.getId());
        assertEquals(1, clues.size());
        assertEquals("Right ear cup has a tiny white scratch near the volume dial.", clues.get(0).getClueDescription());
    }

    @Test
    @DisplayName("Search items filter by keyword, category, and report type")
    void testSearchAndFilterItems() {
        List<Item> searchResults = itemService.searchItems(ItemType.FOUND, "Electronics", ItemStatus.OPEN, "Dell");
        assertNotNull(searchResults);
        assertTrue(searchResults.stream().anyMatch(i -> i.getItemName().contains("Dell")));
    }

    @Test
    @DisplayName("Claim submission: claimant cannot claim own reported item")
    void testCannotClaimOwnReportedItem() {
        User student = userRepository.findByEmail("student@college.edu")
                .orElseThrow(() -> new IllegalStateException("Student user must exist"));

        // Student reports a found item
        ItemReportDto reportDto = new ItemReportDto();
        reportDto.setItemName("Casio Scientific Calculator");
        reportDto.setCategory("Electronics");
        reportDto.setLocation("Room 101");
        reportDto.setItemDate(LocalDate.now());
        reportDto.setType(ItemType.FOUND);

        Item item = itemService.reportItem(reportDto, student);

        ClaimSubmissionDto claimDto = new ClaimSubmissionDto();
        claimDto.setExplanation("I want to claim this item");
        claimDto.setEvidenceType("IDENTIFYING_MARKS");
        claimDto.setEvidenceDetails("It has my name on it");

        // Attempting to claim one's own reported item must throw IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                claimService.submitClaim(item.getId(), claimDto, student)
        );
        assertTrue(ex.getMessage().contains("cannot submit an ownership claim for an item you reported"));
    }

    @Test
    @DisplayName("Admin approval workflow: verifies ownership, marks item RETURNED, and rejects conflicting claims")
    void testAdminClaimVerificationWorkflow() {
        User admin = userRepository.findByEmail("admin@lostfound.edu").get();

        // Create student 1 (Claimant 1)
        UserRegistrationDto reg1 = new UserRegistrationDto();
        reg1.setName("Claimant One");
        reg1.setEmail("claimant1@college.edu");
        reg1.setPassword("Pass12345");
        User claimant1 = userService.registerUser(reg1);

        // Create student 2 (Claimant 2)
        UserRegistrationDto reg2 = new UserRegistrationDto();
        reg2.setName("Claimant Two");
        reg2.setEmail("claimant2@college.edu");
        reg2.setPassword("Pass12345");
        User claimant2 = userService.registerUser(reg2);

        // Create a Found Item
        ItemReportDto reportDto = new ItemReportDto();
        reportDto.setItemName("MacBook Air M2");
        reportDto.setCategory("Electronics");
        reportDto.setColor("Space Gray");
        reportDto.setLocation("Auditorium Hall");
        reportDto.setItemDate(LocalDate.now());
        reportDto.setType(ItemType.FOUND);
        reportDto.setPrivateClue("Serial ending with 4321 and small dent on bottom left corner.");

        Item item = itemService.reportItem(reportDto, admin);

        // Claimant 1 submits valid claim
        ClaimSubmissionDto claimDto1 = new ClaimSubmissionDto();
        claimDto1.setExplanation("I forgot my MacBook after the symposium yesterday.");
        claimDto1.setEvidenceType("SERIAL_NUMBER");
        claimDto1.setEvidenceDetails("Serial ending in 4321 and dent on lower left casing.");
        Claim claim1 = claimService.submitClaim(item.getId(), claimDto1, claimant1);

        // Claimant 2 submits rival claim
        ClaimSubmissionDto claimDto2 = new ClaimSubmissionDto();
        claimDto2.setExplanation("I also lost a Space Gray MacBook.");
        claimDto2.setEvidenceType("IDENTIFYING_MARKS");
        claimDto2.setEvidenceDetails("It was space gray.");
        Claim claim2 = claimService.submitClaim(item.getId(), claimDto2, claimant2);

        assertEquals(ClaimStatus.PENDING, claim1.getStatus());
        assertEquals(ClaimStatus.PENDING, claim2.getStatus());

        // Admin approves Claimant 1
        Claim approved = claimService.approveClaim(claim1.getId(), admin, "Evidence perfectly matches finder's confidential serial and dent description.");

        assertEquals(ClaimStatus.APPROVED, approved.getStatus());
        assertEquals(admin.getId(), approved.getReviewedBy().getId());

        // Verify the item is now marked as RETURNED
        Item updatedItem = itemService.getItemById(item.getId()).get();
        assertEquals(ItemStatus.RETURNED, updatedItem.getStatus());

        // Verify rival claim 2 was automatically REJECTED
        Claim rivalClaim = claimService.getClaimById(claim2.getId()).get();
        assertEquals(ClaimStatus.REJECTED, rivalClaim.getStatus());
    }

    @Test
    @DisplayName("Admin rejection workflow: rejects false claim and restores item status")
    void testAdminClaimRejection() {
        User admin = userRepository.findByEmail("admin@lostfound.edu").get();

        UserRegistrationDto reg = new UserRegistrationDto();
        reg.setName("Fraud Suspect");
        reg.setEmail("suspect@college.edu");
        reg.setPassword("Pass12345");
        User suspect = userService.registerUser(reg);

        ItemReportDto reportDto = new ItemReportDto();
        reportDto.setItemName("Titan Gold Watch");
        reportDto.setCategory("Jewelry");
        reportDto.setLocation("Gymnasium");
        reportDto.setItemDate(LocalDate.now());
        reportDto.setType(ItemType.FOUND);
        reportDto.setPrivateClue("Engraved 'To Dad 2021'");

        Item item = itemService.reportItem(reportDto, admin);

        ClaimSubmissionDto claimDto = new ClaimSubmissionDto();
        claimDto.setExplanation("I lost my watch.");
        claimDto.setEvidenceType("OTHER_PROOF");
        claimDto.setEvidenceDetails("I think it is mine, no receipt.");

        Claim claim = claimService.submitClaim(item.getId(), claimDto, suspect);
        assertEquals(ItemStatus.CLAIM_PENDING, itemService.getItemById(item.getId()).get().getStatus());

        // Admin rejects the claim
        Claim rejected = claimService.rejectClaim(claim.getId(), admin, "Failed to provide matching identifying clues.");
        assertEquals(ClaimStatus.REJECTED, rejected.getStatus());

        // Item status should revert to OPEN because no other claims are pending
        assertEquals(ItemStatus.OPEN, itemService.getItemById(item.getId()).get().getStatus());
    }
}
