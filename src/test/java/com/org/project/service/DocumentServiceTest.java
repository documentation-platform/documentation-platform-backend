package com.org.project.service;

import com.org.project.exception.ParentFolderPermissionException;
import com.org.project.model.*;
import com.org.project.repository.FileContentRelationRepository;
import com.org.project.repository.FileRepository;
import com.org.project.repository.FolderRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private FolderService folderService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FileContentRelationRepository fileContentRelationRepository;

    @InjectMocks
    private DocumentService documentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = Mockito.mock(User.class);
        when(testUser.getId()).thenReturn("test_user_id");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getProvider()).thenReturn(User.Provider.LOCAL);
        when(testUser.getAuthVersion()).thenReturn(1);
    }

    @Nested
    class UserDocumentTests {
        private File testRootFile;
        private File testParentFile;
        private Folder testParentFolder;
        private Folder testRootFolder;

        @BeforeEach
        void setUp() {
            testRootFile = Mockito.mock(File.class);
            when(testRootFile.getId()).thenReturn("test_root_file_id");
            when(testRootFile.getName()).thenReturn("test_root_file");
            when(testRootFile.getFolder()).thenReturn(testRootFolder);

            testParentFile = Mockito.mock(File.class);
            when(testParentFile.getId()).thenReturn("test_parent_file_id");
            when(testParentFile.getName()).thenReturn("test_parent_file");
            when(testParentFile.getFolder()).thenReturn(testParentFolder);

            testParentFolder = Mockito.mock(Folder.class);
            when(testParentFolder.getId()).thenReturn("test_parent_folder_id");
            when(testParentFolder.getName()).thenReturn("test_parent_folder");
            when(testParentFolder.getUser()).thenReturn(testUser);
            when(testParentFolder.getParentFolder()).thenReturn(testRootFolder);

            testRootFolder = Mockito.mock(Folder.class);
            when(testRootFolder.getId()).thenReturn("test_root_folder_id");
            when(testRootFolder.getName()).thenReturn("test_root_folder");
            when(testRootFolder.getUser()).thenReturn(testUser);
        }

        @Nested
        class CreateUserDocument {
            @Test
            void shouldCreateDocumentWithValidInputs() {
                when(folderService.canUserAccessFolder(any(), any())).thenReturn(true);
                when(entityManager.getReference(Folder.class, testParentFolder.getId())).thenReturn(testParentFolder);
                when(entityManager.getReference(User.class, testUser.getId())).thenReturn(testUser);

                File createdFile = Mockito.mock(File.class);
                when(createdFile.getId()).thenReturn("new_file_id");
                when(fileContentRelationRepository.save(any(FileContentRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(fileRepository.save(any(File.class))).thenReturn(createdFile);

                File result = documentService.createUserDocument(testUser.getId(), "New Document", testParentFolder.getId());

                assertNotNull(result);
                assertEquals("new_file_id", result.getId());
                verify(fileRepository, times(1)).save(any(File.class));
                verify(fileContentRelationRepository, times(1)).save(any(FileContentRelation.class));
            }

            @Test
            void shouldThrowPermissionExceptionIfUserCannotAccessFolder() {
                when(folderService.canUserAccessFolder(any(), any())).thenReturn(false);

                assertThrows(
                        ParentFolderPermissionException.class,
                        () -> documentService.createUserDocument(testUser.getId(), "New Document", testParentFolder.getId())
                );

                verify(fileRepository, never()).save(any(File.class));
                verify(fileContentRelationRepository, never()).save(any(FileContentRelation.class));
            }

            @Test
            void shouldCreateDocumentInRootFolderIfParentFolderIdIsNull() {
                when(folderService.getRootUserFolder(testUser.getId())).thenReturn(testRootFolder);
                when(entityManager.getReference(User.class, testUser.getId())).thenReturn(testUser);

                File createdFile = Mockito.mock(File.class);
                when(createdFile.getId()).thenReturn("new_file_id");

                when(fileRepository.save(any(File.class))).thenReturn(createdFile);
                when(fileContentRelationRepository.save(any(FileContentRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));

                File result = documentService.createUserDocument(testUser.getId(), "New Document", null);

                assertNotNull(result);
                assertEquals("new_file_id", result.getId());
                verify(folderService, times(1)).getRootUserFolder(testUser.getId());
                verify(fileRepository, times(1)).save(any(File.class));
                verify(fileContentRelationRepository, times(1)).save(any(FileContentRelation.class));
            }
        }
    }

    @Nested
    class OrganizationDocumentTests {
        private Organization testOrganization;
        private Folder rootOrganizationFolder;
        private Folder parentOrganizationFolder;

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
        }

        @Nested
        class CreateOrganizationDocument {
            @Test
            void shouldCreateDocumentWithValidInputs() {
                when(folderService.canOrganizationAccessFolder(any(), any())).thenReturn(true);
                when(entityManager.getReference(Folder.class, parentOrganizationFolder.getId())).thenReturn(parentOrganizationFolder);
                when(entityManager.getReference(User.class, testUser.getId())).thenReturn(testUser);

                File createdFile = Mockito.mock(File.class);
                when(createdFile.getId()).thenReturn("new_file_id");
                when(fileContentRelationRepository.save(any(FileContentRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(fileRepository.save(any(File.class))).thenReturn(createdFile);

                File result = documentService.createOrganizationDocument(testUser.getId(), testOrganization.getId(), "New Document", parentOrganizationFolder.getId());

                assertNotNull(result);
                assertEquals("new_file_id", result.getId());
                verify(fileRepository, times(1)).save(any(File.class));
                verify(folderService, times(0)).getRootOrganizationFolder(testOrganization.getId());
                verify(fileContentRelationRepository, times(1)).save(any(FileContentRelation.class));
            }

            @Test
            void shouldThrowPermissionExceptionIfUserCannotAccessFolder() {
                when(folderService.canOrganizationAccessFolder(any(), any())).thenReturn(false);

                assertThrows(
                        ParentFolderPermissionException.class,
                        () -> documentService.createOrganizationDocument(testUser.getId(), testOrganization.getId(), "New Document", parentOrganizationFolder.getId())
                );

                verify(fileRepository, never()).save(any(File.class));
                verify(fileContentRelationRepository, never()).save(any(FileContentRelation.class));
            }

            @Test
            void shouldCreateDocumentInRootFolderIfParentFolderIdIsNull() {
                when(folderService.getRootOrganizationFolder(testOrganization.getId())).thenReturn(rootOrganizationFolder);
                when(entityManager.getReference(User.class, testUser.getId())).thenReturn(testUser);

                File createdFile = Mockito.mock(File.class);
                when(createdFile.getId()).thenReturn("new_file_id");

                when(fileRepository.save(any(File.class))).thenReturn(createdFile);
                when(fileContentRelationRepository.save(any(FileContentRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));

                File result = documentService.createOrganizationDocument(testUser.getId(), testOrganization.getId(), "New Document", null);

                assertNotNull(result);
                assertEquals("new_file_id", result.getId());
                verify(folderService, times(1)).getRootOrganizationFolder(testOrganization.getId());
                verify(fileRepository, times(1)).save(any(File.class));
                verify(fileContentRelationRepository, times(1)).save(any(FileContentRelation.class));
            }
        }
    }
}
