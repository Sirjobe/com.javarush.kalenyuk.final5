package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private ProfileMapper profileMapper;

    private final long userId = 1L;
    private final String userEmail = "user@example.com";

    // Helper to create AuthUser with id=1L
    private AuthUser createAuthUser() {
        User user = new User(userId, userEmail, "password", "First", null, null, Role.DEV);
        return new AuthUser(user);
    }

    @Test
    void getAuthenticatedUserShouldReturnProfile() throws Exception {
        Profile profile = new Profile(userId);
        ProfileTo profileTo = new ProfileTo(userId, Collections.emptySet(), Collections.emptySet());

        when(profileRepository.getOrCreate(userId)).thenReturn(profile);
        when(profileMapper.toTo(profile)).thenReturn(profileTo);

        mockMvc.perform(get(ProfileRestController.REST_URL)
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId));

        verify(profileRepository).getOrCreate(userId);
        verify(profileMapper).toTo(profile);
    }

    @Test
    void getUnauthenticatedShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(ProfileRestController.REST_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(profileRepository, profileMapper);
    }

    @Test
    void updateValidDataShouldSucceed() throws Exception {
        ProfileTo profileTo = new ProfileTo(userId, Collections.emptySet(), Collections.emptySet());
        Profile profile = new Profile(userId);

        when(profileRepository.getOrCreate(userId)).thenReturn(profile);
        when(profileMapper.updateFromTo(any(Profile.class), any(ProfileTo.class))).thenReturn(profile);

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileTo)))
                .andExpect(status().isNoContent());

        verify(profileRepository).getOrCreate(userId);
        verify(profileMapper).updateFromTo(profile, profileTo);  // More specific verification
        verify(profileRepository).save(profile);
    }

    @Test
    void updateUnauthenticatedShouldReturnUnauthorized() throws Exception {
        ProfileTo profileTo = new ProfileTo(userId, Collections.emptySet(), Collections.emptySet());

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileTo)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(profileRepository, profileMapper);
    }

    @Test
    void updateWithInvalidContactShouldReturnUnprocessableEntity() throws Exception {
        ContactTo invalidContact = new ContactTo("", "");
        ProfileTo invalidProfileTo = new ProfileTo(userId,
                Collections.emptySet(),
                Set.of(invalidContact));

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfileTo)))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(profileRepository, profileMapper);
    }

    @Test
    void updateWithNullFieldsShouldSucceed() throws Exception {
        String invalidJson = "{\"mailNotifications\": null, \"contacts\": null}\"";

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isNoContent());

        verify(profileRepository).getOrCreate(anyLong());
        verify(profileMapper).updateFromTo(any(), any());
        verify(profileRepository).save(any());
    }

    @Test
    void updateWithInvalidMailNotificationShouldReturnUnprocessableEntity() throws Exception {
        ProfileTo invalidProfileTo = new ProfileTo(userId,
                Set.of(""),
                Collections.emptySet());

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfileTo)))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(profileRepository, profileMapper);
    }

    // Additional test: unsuccess path - ID mismatch
    @Test
    void updateWithIdMismatchShouldThrowException() throws Exception {
        ProfileTo profileToWithWrongId = new ProfileTo(2L, Collections.emptySet(), Collections.emptySet());  // Wrong ID

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(createAuthUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileToWithWrongId)))
                .andExpect(status().isUnprocessableEntity())  // Adjusted to 422
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ProfileTo must has id=1")));  // Adjust message if needed

        verifyNoInteractions(profileRepository, profileMapper);
    }
}