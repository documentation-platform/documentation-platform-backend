package com.org.project.service;

import com.org.project.exception.ParentFolderPermissionException;
import com.org.project.model.Folder;
import com.org.project.model.Organization;
import com.org.project.model.User;
import com.org.project.repository.FolderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FolderServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private FolderService folderService;

    private User testUser;
    private Organization testOrganization;
    private Folder rootFolder;
    private Folder parentFolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = Mockito.mock(User.class);
        when(testUser.getId()).thenReturn("test_user_id");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getProvider()).thenReturn(User.Provider.LOCAL);
        when(testUser.getAuthVersion()).thenReturn(1);

        testOrganization = Mockito.mock(Organization.class);
        when(testOrganization.getId()).thenReturn("test_organization_id");

        rootFolder = Mockito.mock(Folder.class);
        when(rootFolder.getName()).thenReturn("root");
        when(rootFolder.getId()).thenReturn("root_folder_id");

        parentFolder = Mockito.mock(Folder.class);
        when(parentFolder.getName()).thenReturn("parent_folder");
        when(parentFolder.getId()).thenReturn("root_folder_id");
        when(parentFolder.getUser()).thenReturn(testUser);
    }

    @Nested
    class CreateUserFolder {
        @Test
        void createUserFolder_WithParentFolder_ShouldCreateAndSaveFolder() {
            when(entityManager.getReference(User.class, "test_user_id")).thenReturn(testUser);
            when(entityManager.getReference(Folder.class, "parent_folder_id")).thenReturn(parentFolder);
            when(folderRepository.findById("parent_folder_id")).thenReturn(Optional.of(parentFolder));
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder createdFolder = folderService.createUserFolder("test_user_id", "Test Folder", "parent_folder_id");

            assertNotNull(createdFolder);
            assertEquals("Test Folder", createdFolder.getName());
            assertEquals(parentFolder, createdFolder.getParentFolder());
            assertEquals(testUser, createdFolder.getUser());
            verify(folderRepository, times(1)).save(createdFolder);
        }

        @Test
        void createUserFolder_WithParentFolderOwnedByAnotherUser_ShouldThrowException() {
            User anotherUser = Mockito.mock(User.class);
            when(anotherUser.getId()).thenReturn("another_user_id");

            Folder anotherUserFolder = Mockito.mock(Folder.class);
            when(anotherUserFolder.getId()).thenReturn("another_folder_id");
            when(anotherUserFolder.getUser()).thenReturn(anotherUser);

            when(folderRepository.findById("another_folder_id")).thenReturn(Optional.of(anotherUserFolder));

            assertThrows(ParentFolderPermissionException.class, () -> {
                folderService.createUserFolder("test_user_id", "Test Folder", "another_folder_id");
            });

            verify(folderRepository, never()).save(any(Folder.class));
        }

        @Test
        void createUserFolder_WithoutParentFolder_ShouldSetRootAsParent() {
            when(entityManager.getReference(User.class, "test_user_id")).thenReturn(testUser);
            when(folderRepository.findByUserIdAndParentFolderIsNull("test_user_id")).thenReturn(rootFolder);
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder createdFolder = folderService.createUserFolder("test_user_id", "Test Folder", null);

            assertNotNull(createdFolder);
            assertEquals("Test Folder", createdFolder.getName());
            assertEquals(rootFolder, createdFolder.getParentFolder());
            assertEquals(testUser, createdFolder.getUser());
            verify(folderRepository, times(1)).save(createdFolder);
        }
    }

    @Nested
    class CreateOrganizationFolder {

        @Test
        void createOrganizationFolder_WithParentFolder_ShouldCreateAndSaveFolder() {
            Folder parentOrganizationFolder = Mockito.mock(Folder.class);
            when(parentOrganizationFolder.getId()).thenReturn("parent_organization_folder_id");
            when(parentOrganizationFolder.getName()).thenReturn("root");
            when(parentOrganizationFolder.getOrganization()).thenReturn(testOrganization);

            when(entityManager.getReference(Organization.class, "test_organization_id")).thenReturn(testOrganization);
            when(entityManager.getReference(Folder.class, "parent_organization_folder_id")).thenReturn(parentOrganizationFolder);
            when(folderRepository.findById("parent_organization_folder_id")).thenReturn(Optional.of(parentOrganizationFolder));
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder createdFolder = folderService.createOrganizationFolder("test_organization_id", "Org Folder", "parent_organization_folder_id");

            assertNotNull(createdFolder);
            assertEquals("Org Folder", createdFolder.getName());
            assertEquals(parentOrganizationFolder, createdFolder.getParentFolder());
            assertEquals(testOrganization, createdFolder.getOrganization());
            verify(folderRepository, times(1)).save(createdFolder);
        }

        @Test
        void createOrganizationFolder_WithParentFolderOwnedByAnotherOrganization_ShouldThrowException() {
            Organization anotherOrganization = Mockito.mock(Organization.class);
            when(anotherOrganization.getId()).thenReturn("another_organization_id");

            Folder anotherUserFolder = Mockito.mock(Folder.class);
            when(anotherUserFolder.getId()).thenReturn("another_folder_id");
            when(anotherUserFolder.getOrganization()).thenReturn(anotherOrganization);

            when(folderRepository.findById("another_folder_id")).thenReturn(Optional.of(anotherUserFolder));

            assertThrows(ParentFolderPermissionException.class, () -> {
                folderService.createOrganizationFolder("test_organization_id", "Test Folder", "another_folder_id");
            });

            verify(folderRepository, never()).save(any(Folder.class));
        }

        @Test
        void createOrganizationFolder_WithoutParentFolder_ShouldSetRootAsParent() {
            Folder rootFolder = Mockito.mock(Folder.class);
            when(rootFolder.getId()).thenReturn("root_folder_id");
            when(rootFolder.getOrganization()).thenReturn(testOrganization);

            when(entityManager.getReference(Organization.class, "test_organization_id")).thenReturn(testOrganization);
            when(folderRepository.findByOrganizationIdAndParentFolderIsNull("test_organization_id")).thenReturn(rootFolder);
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder createdFolder = folderService.createOrganizationFolder("test_organization_id", "Org Folder", null);

            assertNotNull(createdFolder);
            assertEquals("Org Folder", createdFolder.getName());
            assertEquals(rootFolder, createdFolder.getParentFolder());
            assertEquals(testOrganization, createdFolder.getOrganization());
            verify(folderRepository, times(1)).save(createdFolder);
        }
    }

    @Nested
    class GetRootUserFolder {
        @Test
        void getRootUserFolder_WhenRootFolderExists_ShouldReturnExistingFolder() {
            when(folderRepository.findByUserIdAndParentFolderIsNull("test_user_id")).thenReturn(rootFolder);

            Folder result = folderService.getRootUserFolder("test_user_id");

            assertNotNull(result);
            assertEquals(rootFolder, result);
            verify(folderRepository, never()).save(any(Folder.class));
        }

        @Test
        void getRootUserFolder_WhenRootFolderDoesNotExist_ShouldCreateAndSaveFolder() {
            when(folderRepository.findByUserIdAndParentFolderIsNull("test_user_id")).thenReturn(null);
            when(entityManager.getReference(User.class, "test_user_id")).thenReturn(testUser);
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder result = folderService.getRootUserFolder("test_user_id");

            assertNotNull(result);
            assertEquals("root", result.getName());
            assertEquals(testUser, result.getUser());
            verify(folderRepository, times(1)).save(result);
        }
    }

    @Nested
    class GetRootOrganizationFolder {
        @Test
        void getRootOrganizationFolder_WhenRootFolderExists_ShouldReturnExistingFolder() {
            Folder rootFolder = Mockito.mock(Folder.class);
            when(folderRepository.findByOrganizationIdAndParentFolderIsNull("org_id")).thenReturn(rootFolder);

            Folder result = folderService.getRootOrganizationFolder("org_id");

            assertNotNull(result);
            assertEquals(rootFolder, result);
            verify(folderRepository, never()).save(any(Folder.class));
        }

        @Test
        void getRootOrganizationFolder_WhenRootFolderDoesNotExist_ShouldCreateAndSaveFolder() {
            Organization testOrganization = Mockito.mock(Organization.class);
            when(testOrganization.getId()).thenReturn("org_id");

            when(folderRepository.findByOrganizationIdAndParentFolderIsNull("org_id")).thenReturn(null);
            when(entityManager.getReference(Organization.class, "org_id")).thenReturn(testOrganization);
            when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Folder result = folderService.getRootOrganizationFolder("org_id");

            assertNotNull(result);
            assertEquals("root", result.getName());
            assertEquals(testOrganization, result.getOrganization());
            verify(folderRepository, times(1)).save(result);
        }
    }

    @Nested
    class CanUserAccessFolder {

        @Test
        void canUserAccessFolder_WhenFolderExistsAndBelongsToUser_ShouldReturnTrue() {
            Folder folder = Mockito.mock(Folder.class);
            when(folder.getUser()).thenReturn(testUser);
            when(folderRepository.findById("folder_id")).thenReturn(Optional.of(folder));

            boolean result = folderService.canUserAccessFolder("test_user_id", "folder_id");

            assertTrue(result);
        }

        @Test
        void canUserAccessFolder_WhenFolderDoesNotExist_ShouldReturnFalse() {
            when(folderRepository.findById("folder_id")).thenReturn(Optional.empty());

            boolean result = folderService.canUserAccessFolder("test_user_id", "folder_id");

            assertFalse(result);
        }

        @Test
        void canUserAccessFolder_WhenFolderBelongsToAnotherUser_ShouldReturnFalse() {
            User anotherUser = Mockito.mock(User.class);
            when(anotherUser.getId()).thenReturn("another_user_id");

            Folder folder = Mockito.mock(Folder.class);
            when(folder.getUser()).thenReturn(anotherUser);
            when(folderRepository.findById("folder_id")).thenReturn(Optional.of(folder));

            boolean result = folderService.canUserAccessFolder("test_user_id", "folder_id");

            assertFalse(result);
        }
    }

    @Nested
    class CanOrganizationAccessFolder {

        @Test
        void canOrganizationAccessFolder_WhenFolderExistsAndBelongsToOrganization_ShouldReturnTrue() {
            Organization testOrganization = Mockito.mock(Organization.class);
            when(testOrganization.getId()).thenReturn("org_id");

            Folder folder = Mockito.mock(Folder.class);
            when(folder.getOrganization()).thenReturn(testOrganization);
            when(folderRepository.findById("folder_id")).thenReturn(Optional.of(folder));

            boolean result = folderService.canOrganizationAccessFolder("org_id", "folder_id");

            assertTrue(result);
        }

        @Test
        void canOrganizationAccessFolder_WhenFolderDoesNotExist_ShouldReturnFalse() {
            when(folderRepository.findById("folder_id")).thenReturn(Optional.empty());

            boolean result = folderService.canOrganizationAccessFolder("org_id", "folder_id");

            assertFalse(result);
        }

        @Test
        void canOrganizationAccessFolder_WhenFolderBelongsToAnotherOrganization_ShouldReturnFalse() {
            Organization anotherOrganization = Mockito.mock(Organization.class);
            when(anotherOrganization.getId()).thenReturn("another_org_id");

            Folder folder = Mockito.mock(Folder.class);
            when(folder.getOrganization()).thenReturn(anotherOrganization);
            when(folderRepository.findById("folder_id")).thenReturn(Optional.of(folder));

            boolean result = folderService.canOrganizationAccessFolder("org_id", "folder_id");

            assertFalse(result);
        }
    }
}
