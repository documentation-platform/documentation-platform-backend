package com.org.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.org.project.model.File;
import com.org.project.model.Folder;
import com.org.project.model.Organization;
import com.org.project.model.User;
import com.org.project.service.DocumentService;
import com.org.project.service.OrganizationService;
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

@ControllerTest(DocumentController.class)
public class DocumentControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private OrganizationService organizationService;

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
    class OrganizationTests {
        private Organization testOrganization;
        private Folder rootOrganizationFolder;
        private Folder parentOrganizationFolder;
        private File testRootFile;
        private File testParentFile;

        @BeforeEach
        void setUp() {
            testOrganization = Mockito.mock(Organization.class);
            when(testOrganization.getId()).thenReturn("test_org_id");
            when(testOrganization.getName()).thenReturn("test_org");

            rootOrganizationFolder = Mockito.mock(Folder.class);
            when(rootOrganizationFolder.getName()).thenReturn("root");
            when(rootOrganizationFolder.getId()).thenReturn("root_folder_id");
            when(rootOrganizationFolder.getOrganization()).thenReturn(testOrganization);

            parentOrganizationFolder = Mockito.mock(Folder.class);
            when(parentOrganizationFolder.getName()).thenReturn("parent");
            when(parentOrganizationFolder.getId()).thenReturn("parent_folder_id");
            when(parentOrganizationFolder.getOrganization()).thenReturn(testOrganization);
            when(parentOrganizationFolder.getParentFolder()).thenReturn(rootOrganizationFolder);

            testRootFile = Mockito.mock(File.class);
            when(testRootFile.getId()).thenReturn("root_file_id");
            when(testRootFile.getName()).thenReturn("root_file");
            when(testRootFile.getFolder()).thenReturn(rootOrganizationFolder);

            testParentFile = Mockito.mock(File.class);
            when(testParentFile.getId()).thenReturn("parent_file_id");
            when(testParentFile.getName()).thenReturn("parent_file");
            when(testParentFile.getFolder()).thenReturn(parentOrganizationFolder);
        }

        @Nested
        class CreateOrganizationDocumentTests {
            @Test
            void shouldReturn201AndDocumentIdWhenDocumentIsCreated() throws Exception {
                File testFile = Mockito.mock(File.class);
                when(testFile.getId()).thenReturn("test_file_id");
                when(documentService.createOrganizationDocument(any(), any(), any(), any())).thenReturn(testFile);

                mockMvc.perform(post("/document/organization/{org_id}/create", testOrganization.getId())
                                .param("name", "test_document")
                                .param("parent_folder_id", parentOrganizationFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.document_id").value(testFile.getId()));
            }

            @Test
            void shouldReturn500WhenDocumentCreationFails() throws Exception {
                when(documentService.createOrganizationDocument(any(), any(), any(), any())).thenThrow(new RuntimeException("Creation failed"));

                mockMvc.perform(post("/document/organization/{org_id}/create", testOrganization.getId())
                                .param("name", "test_document")
                                .param("parent_folder_id", parentOrganizationFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("An error occurred while creating the document"));
            }
        }
    }

    @Nested
    class UserTests {
        private Folder testFolder;
        private File testFile;

        @BeforeEach
        void setUp() {
            testFolder = Mockito.mock(Folder.class);
            when(testFolder.getName()).thenReturn("test_folder");
            when(testFolder.getId()).thenReturn("test_folder_id");
            when(testFolder.getUser()).thenReturn(testUser);

            testFile = Mockito.mock(File.class);
            when(testFile.getId()).thenReturn("test_file_id");
            when(testFile.getName()).thenReturn("test_file");
            when(testFile.getFolder()).thenReturn(testFolder);
        }

        @Nested
        class CreateUserDocumentTests {
            @Test
            void shouldReturn201AndDocumentIdWhenDocumentIsCreated() throws Exception {
                when(documentService.createUserDocument(any(), any(), any())).thenReturn(testFile);

                mockMvc.perform(post("/document/user/create")
                                .param("name", "test_document")
                                .param("parent_folder_id", testFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.document_id").value(testFile.getId()));
            }

            @Test
            void shouldReturn500WhenDocumentCreationFails() throws Exception {
                when(documentService.createUserDocument(any(), any(), any())).thenThrow(new RuntimeException("Creation failed"));

                mockMvc.perform(post("/document/user/create")
                                .param("name", "test_document")
                                .param("parent_folder_id", testFolder.getId())
                                .requestAttr("user_id", testUser.getId())
                        )
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("An error occurred while creating the document"));
            }
        }
    }
}