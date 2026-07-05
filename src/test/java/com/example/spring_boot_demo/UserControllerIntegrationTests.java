package com.example.spring_boot_demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.spring_boot_demo.model.AppUser;
import com.example.spring_boot_demo.repository.AppUserRepository;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void register_ShouldCreateUser_WhenRequestIsValid() throws Exception {
                String request = """
                                {
                                    "username": "alice",
                                    "email": "alice@example.com",
                                    "password": "123456"
                                }
                                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_ShouldReturn400_WhenUsernameAlreadyExists() throws Exception {
        appUserRepository.save(new AppUser("alice", "other@example.com", passwordEncoder.encode("123456")));

                String request = """
                                {
                                    "username": "alice",
                                    "email": "alice@example.com",
                                    "password": "123456"
                                }
                                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void login_ShouldReturn200_WhenCredentialsAreCorrect() throws Exception {
        appUserRepository.save(new AppUser("bob", "bob@example.com", passwordEncoder.encode("123456")));

                String request = """
                                {
                                    "username": "bob",
                                    "password": "123456"
                                }
                                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void login_ShouldReturn400_WhenPasswordIsWrong() throws Exception {
        appUserRepository.save(new AppUser("bob", "bob@example.com", passwordEncoder.encode("123456")));

                String request = """
                                {
                                    "username": "bob",
                                    "password": "wrong-password"
                                }
                                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void delete_ShouldReturn200_WhenUserExists() throws Exception {
        AppUser saved = appUserRepository.save(new AppUser("carol", "carol@example.com", passwordEncoder.encode("123456")));

        mockMvc.perform(delete("/api/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void delete_ShouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }

    @Test
    void list_ShouldReturnUsers_WhenUsersExist() throws Exception {
        appUserRepository.save(new AppUser("u1", "u1@example.com", passwordEncoder.encode("123456")));
        appUserRepository.save(new AppUser("u2", "u2@example.com", passwordEncoder.encode("123456")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getById_ShouldReturnUser_WhenUserExists() throws Exception {
        AppUser saved = appUserRepository.save(new AppUser("dave", "dave@example.com", passwordEncoder.encode("123456")));

        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.username").value("dave"));
    }

    @Test
    void getById_ShouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }

    @Test
    void update_ShouldReturnUpdatedUser_WhenRequestIsValid() throws Exception {
        AppUser saved = appUserRepository.save(new AppUser("erin", "erin@example.com", passwordEncoder.encode("123456")));

                String request = """
                                {
                                    "username": "erin-new",
                                    "email": "erin-new@example.com",
                                    "password": "abcdef"
                                }
                                """;

        mockMvc.perform(put("/api/users/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.username").value("erin-new"))
                .andExpect(jsonPath("$.email").value("erin-new@example.com"));
    }

    @Test
    void update_ShouldReturn400_WhenUsernameAlreadyExists() throws Exception {
        AppUser target = appUserRepository.save(new AppUser("frank", "frank@example.com", passwordEncoder.encode("123456")));
        appUserRepository.save(new AppUser("taken", "taken@example.com", passwordEncoder.encode("123456")));

                String request = """
                                {
                                    "username": "taken",
                                    "email": "frank-new@example.com",
                                    "password": "abcdef"
                                }
                                """;

        mockMvc.perform(put("/api/users/{id}", target.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

}
