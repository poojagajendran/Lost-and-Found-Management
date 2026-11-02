package com.lostfound.lostfound.config;

import com.lostfound.lostfound.model.*;
import com.lostfound.lostfound.repository.ItemRepository;
import com.lostfound.lostfound.repository.PrivateClueRepository;
import com.lostfound.lostfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PrivateClueRepository privateClueRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@lostfound.edu}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    public DataInitializer(UserRepository userRepository,
                           ItemRepository itemRepository,
                           PrivateClueRepository privateClueRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.privateClueRepository = privateClueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Ensure Default Administrator
        String normalizedAdminEmail = adminEmail.trim().toLowerCase();
        User admin = userRepository.findByEmail(normalizedAdminEmail).orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setName(adminName);
            newAdmin.setEmail(normalizedAdminEmail);
            newAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            newAdmin.setRole("ROLE_ADMIN");
            return userRepository.save(newAdmin);
        });

        // 2. Ensure Sample Student User
        User student = userRepository.findByEmail("student@college.edu").orElseGet(() -> {
            User newStudent = new User();
            newStudent.setName("Pooja Sharma");
            newStudent.setEmail("student@college.edu");
            newStudent.setPasswordHash(passwordEncoder.encode("Student@123"));
            newStudent.setRole("ROLE_USER");
            return userRepository.save(newStudent);
        });

        // 3. Seed Sample Items and Private Clues if catalog is empty
        if (itemRepository.count() == 0) {
            // LOST ITEM 1
            Item lost1 = new Item(
                    "Apple AirPods Pro (2nd Gen)",
                    "Electronics",
                    "White",
                    "Science Block - Physics Lab 2",
                    LocalDate.now().minusDays(2),
                    "Left near desk #4 during the morning lab session. White charging case with silicone lanyard.",
                    ItemType.LOST,
                    student
            );
            itemRepository.save(lost1);

            // LOST ITEM 2
            Item lost2 = new Item(
                    "Brown Leather Wallet",
                    "Wallets/Bags",
                    "Dark Brown",
                    "Central Cafeteria",
                    LocalDate.now().minusDays(1),
                    "Contains university student ID, transit pass, and library card. Urgent!",
                    ItemType.LOST,
                    student
            );
            itemRepository.save(lost2);

            // FOUND ITEM 1 with Private Clue
            Item found1 = new Item(
                    "Dell 65W USB-C Laptop Charger",
                    "Electronics",
                    "Black",
                    "Main Auditorium - Row F",
                    LocalDate.now().minusDays(3),
                    "Found plugged into the wall outlet after the guest symposium.",
                    ItemType.FOUND,
                    admin
            );
            Item savedFound1 = itemRepository.save(found1);
            PrivateClue clue1 = new PrivateClue(savedFound1, "Has initials 'P.K.' engraved into the power brick and a neon green cable tie.");
            privateClueRepository.save(clue1);
            savedFound1.addPrivateClue(clue1);

            // FOUND ITEM 2 with Private Clue
            Item found2 = new Item(
                    "Silver Casio Stainless Steel Watch",
                    "Jewelry",
                    "Silver",
                    "Sports Complex - Indoor Court Bench",
                    LocalDate.now().minusDays(2),
                    "Water-resistant sports wrist watch found near the court locker area.",
                    ItemType.FOUND,
                    admin
            );
            Item savedFound2 = itemRepository.save(found2);
            PrivateClue clue2 = new PrivateClue(savedFound2, "Engraved on the case back: 'Class of 2024 - Never Stop Dreaming'.");
            privateClueRepository.save(clue2);
            savedFound2.addPrivateClue(clue2);

            // FOUND ITEM 3 with Private Clue
            Item found3 = new Item(
                    "Texas Instruments TI-84 Plus CE Calculator",
                    "Electronics",
                    "Black",
                    "Mathematics Hall - Room 204",
                    LocalDate.now().minusDays(1),
                    "Found on the front podium desk after the Calculus mid-term exam.",
                    ItemType.FOUND,
                    admin
            );
            Item savedFound3 = itemRepository.save(found3);
            PrivateClue clue3 = new PrivateClue(savedFound3, "Slide cover has a holographic astronaut sticker inside and battery compartment has name written in pencil.");
            privateClueRepository.save(clue3);
            savedFound3.addPrivateClue(clue3);

            // FOUND ITEM 4 with Private Clue
            Item found4 = new Item(
                    "Blue JanSport Backpack",
                    "Wallets/Bags",
                    "Navy Blue",
                    "Campus Transit Terminal",
                    LocalDate.now().minusDays(1),
                    "Found resting on the passenger shelter bench around 6 PM.",
                    ItemType.FOUND,
                    admin
            );
            Item savedFound4 = itemRepository.save(found4);
            PrivateClue clue4 = new PrivateClue(savedFound4, "Inside pouch contains a yellow spiral notebook and house keys with a copper Eiffel Tower keychain.");
            privateClueRepository.save(clue4);
            savedFound4.addPrivateClue(clue4);
        }
    }
}
