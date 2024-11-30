package com.org.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.org.project.model.Folder;
import com.org.project.model.Organization;
import com.org.project.model.User;
import com.org.project.service.FolderService;
import com.org.project.test_configs.BaseControllerTest;
import com.org.project.test_configs.ControllerTest;

import jakarta.persistence.EntityManager;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(FolderController.class)
public class FolderControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FolderService folderService;

    @MockBean
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = Mockito.mock(User.class);
        when(testUser.getId()).thenReturn("test_id");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getProvider()).thenReturn(User.Provider.LOCAL);
        when(testUser.getAuthVersion()).thenReturn(1);

    }

    @Nested
    class UserTests {
        private Folder rootUserFolder;
        private Folder parentUserFolder;

        @BeforeEach
        void setUp() {
            rootUserFolder = Mockito.mock(Folder.class);
            when(rootUserFolder.getName()).thenReturn("test_folder");
            when(rootUserFolder.getId()).thenReturn("test_folder_id");
            when(rootUserFolder.getParentFolder()).thenReturn(null);
            when(rootUserFolder.getUser()).thenReturn(testUser);

            parentUserFolder = Mockito.mock(Folder.class);
            when(parentUserFolder.getName()).thenReturn("parent_folder");
            when(parentUserFolder.getId()).thenReturn("parent_folder_id");
            when(parentUserFolder.getParentFolder()).thenReturn(rootUserFolder);
            when(parentUserFolder.getUser()).thenReturn(testUser);
        }

        @Nested
        class CreateUserFolder {
            @Test
            void shouldReturn201AndFolderIdWhenFolderIsCreated() throws Exception {
                when(folderService.createUserFolder(any(), any(), any())).thenReturn(rootUserFolder);

                mockMvc.perform(post("/folder/user/create")
                                .param("name", "test_folder")
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.folder_id").value(rootUserFolder.getId()));
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

        @Nested
        class MoveUserFolder {
            private Folder newParentUserFolder;

            @BeforeEach
            void setUp() {
                newParentUserFolder = Mockito.mock(Folder.class);
                when(newParentUserFolder.getId()).thenReturn("new_parent_folder_id");
                when(newParentUserFolder.getUser()).thenReturn(testUser);
                when(newParentUserFolder.getParentFolder()).thenReturn(parentUserFolder);
            }

            @Test
            void shouldReturn200AndFolderIdWhenFolderIsMoved() throws Exception {
                when(folderService.moveUserFolder(any(), any(), any())).thenReturn(newParentUserFolder);

                mockMvc.perform(patch("/folder/{folder_id}/user/move", newParentUserFolder.getId())
                                .param("parent_folder_id", parentUserFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.folder_id").value(newParentUserFolder.getId()))
                        .andExpect(jsonPath("$.parent_folder_id").value(parentUserFolder.getId()));
            }

            @Test
            void shouldReturn500AndErrorMessageWhenFolderMoveFails() throws Exception {
                when(folderService.moveUserFolder(any(), any(), any())).thenThrow(new RuntimeException("Failed to move folder"));

                mockMvc.perform(patch("/folder/{folder_id}/user/move", newParentUserFolder.getId())
                                .param("parent_folder_id", parentUserFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("An error occurred while moving the folder"));
            }
        }
    }

    @Nested
    class OrganizationTests {
        private Organization testOrganizaton;
        private Folder rootOrganizationFolder;
        private Folder parentOrganizationFolder;

        @BeforeEach
        void setUp() {
            testOrganizaton = Mockito.mock(Organization.class);
            when(testOrganizaton.getId()).thenReturn("test_organization_id");
            when(testOrganizaton.getName()).thenReturn("test_organization");

            rootOrganizationFolder = Mockito.mock(Folder.class);
            when(rootOrganizationFolder.getName()).thenReturn("root_folder");
            when(rootOrganizationFolder.getId()).thenReturn("root_folder_id");
            when(rootOrganizationFolder.getOrganization()).thenReturn(testOrganizaton);

            parentOrganizationFolder = Mockito.mock(Folder.class);
            when(parentOrganizationFolder.getName()).thenReturn("parent_folder");
            when(parentOrganizationFolder.getId()).thenReturn("parent_folder_id");
            when(parentOrganizationFolder.getOrganization()).thenReturn(testOrganizaton);
        }

        @Nested
        class CreateOrganizationFolder {
            @Test
            void shouldReturn201AndFolderIdWhenFolderIsCreated() throws Exception {
                when(folderService.createOrganizationFolder(any(), any(), any())).thenReturn(rootOrganizationFolder);

                mockMvc.perform(post("/folder/organization/{organization_id}/create", testOrganizaton.getId())
                                .param("name", "root_folder")
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.folder_id").value(rootOrganizationFolder.getId()));
            }

            @Test
            void shouldReturn500AndErrorMessageWhenFolderCreationFails() throws Exception {
                when(folderService.createOrganizationFolder(any(), any(), any())).thenThrow(new RuntimeException("Failed to create folder"));

                mockMvc.perform(post("/folder/organization/{organization_id}/create", testOrganizaton.getId())
                                .param("name", "root_folder")
                        )
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("An error occurred while creating the folder"));
            }

            @Test
            void shouldReturn400WhenFolderNameIsMissing() throws Exception {
                mockMvc.perform(post("/folder/organization/{organization_id}/create", testOrganizaton.getId()))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        class MoveOrganizationFolder {
            private Folder newParentOrganizationFolder;

            @BeforeEach
            void setUp() {
                newParentOrganizationFolder = Mockito.mock(Folder.class);
                when(newParentOrganizationFolder.getId()).thenReturn("new_parent_folder_id");
                when(newParentOrganizationFolder.getOrganization()).thenReturn(testOrganizaton);
                when(newParentOrganizationFolder.getParentFolder()).thenReturn(parentOrganizationFolder);
            }

            @Test
            void shouldReturn200AndFolderIdWhenFolderIsMoved() throws Exception {
                when(folderService.moveOrganizationFolder(any(), any(), any())).thenReturn(newParentOrganizationFolder);

                mockMvc.perform(patch("/folder/{folder_id}/organization/{organization_id}/move", newParentOrganizationFolder.getId(), testOrganizaton.getId())
                                .param("parent_folder_id", parentOrganizationFolder.getId())
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.folder_id").value(newParentOrganizationFolder.getId()))
                        .andExpect(jsonPath("$.parent_folder_id").value(parentOrganizationFolder.getId()));
            }

            @Test
            void shouldReturn500AndErrorMessageWhenFolderMoveFails() throws Exception {
                when(folderService.moveOrganizationFolder(any(), any(), any())).thenThrow(new RuntimeException("Failed to move folder"));

                mockMvc.perform(patch("/folder/{folder_id}/organization/{organization_id}/move", newParentOrganizationFolder.getId(), testOrganizaton.getId())
                                .param("parent_folder_id", parentOrganizationFolder.getId())
                        )
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("An error occurred while moving the folder"));
            }
        }
    }
}
