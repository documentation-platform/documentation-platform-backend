package com.org.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.org.project.model.Folder;
import com.org.project.model.User;
import com.org.project.service.FolderService;
import com.org.project.test_configs.BaseControllerTest;
import com.org.project.test_configs.ControllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(FolderController.class)
public class FolderControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FolderService folderService;

    private User testUser;
    private Folder noParentUserFolder;

    @BeforeEach
    void setUp() {
        testUser = Mockito.mock(User.class);
        when(testUser.getId()).thenReturn("test_id");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getProvider()).thenReturn(User.Provider.LOCAL);
        when(testUser.getAuthVersion()).thenReturn(1);

        noParentUserFolder = Mockito.mock(Folder.class);
        when(noParentUserFolder.getName()).thenReturn("test_folder");
        when(noParentUserFolder.getId()).thenReturn("test_folder_id");
    }

    @Nested
    class CreateUserFolder {
        @Test
        void shouldReturn201AndFolderIdWhenFolderIsCreated() throws Exception {
            when(folderService.createUserFolder(any(), any(), any())).thenReturn(noParentUserFolder);

            mockMvc.perform(post("/folder/user/create")
                            .param("name", "test_folder")
                            .requestAttr("user_id", testUser.getId())
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.folder_id").value(noParentUserFolder.getId()));
        }

        @Test
        void shouldReturn500AndErrorMessageWhenFolderCreationFails() throws Exception {
            when(folderService.createUserFolder(any(), any(), any())).thenThrow(new RuntimeException("Failed to create folder"));

            mockMvc.perform(post("/folder/user/create")
                            .param("name", "test_folder")
                            .requestAttr("user_id", testUser.getId())
                    )
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("An error occurred while creating the folder"));
        }

        @Test
        void shouldReturn400WhenFolderNameIsMissing() throws Exception {
            mockMvc.perform(post("/folder/user/create")
                            .requestAttr("user_id", testUser.getId())
                    )
                    .andExpect(status().isBadRequest());
        }
    }
}