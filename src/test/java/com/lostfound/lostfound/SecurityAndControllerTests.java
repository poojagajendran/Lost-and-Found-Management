package com.lostfound.lostfound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SecurityAndControllerTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Public routes are accessible to anonymous visitors")
    void testPublicRoutesAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Unauthenticated requests to /report redirect to /login")
    void testReportRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/report"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Unauthenticated requests to /my-claims redirect to /login")
    void testMyClaimsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/my-claims"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Ordinary USER cannot access /admin/dashboard (Forbidden 403)")
    @WithMockUser(username = "student@college.edu", roles = {"USER"})
    void testOrdinaryUserDeniedAdminAccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN user successfully accesses /admin/dashboard (200 OK)")
    @WithMockUser(username = "admin@lostfound.edu", roles = {"ADMIN"})
    void testAdminAccessPermitted() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalLost", "totalFound", "pendingClaims"));
    }

    @Test
    @DisplayName("Authenticated user can view /report form")
    @WithMockUser(username = "student@college.edu", roles = {"USER"})
    void testAuthenticatedUserCanViewReport() throws Exception {
        mockMvc.perform(get("/report"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("itemDto", "categories"));
    }
}
