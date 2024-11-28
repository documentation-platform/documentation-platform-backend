package com.org.project.service;

import com.org.project.model.Folder;
import com.org.project.model.User;
import com.org.project.repository.FolderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
    private Folder rootFolder;
    private Folder parentFolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = Mockito.mock(User.class);
        when(testUser.getId()).thenReturn("test_id");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getProvider()).thenReturn(User.Provider.LOCAL);
        when(testUser.getAuthVersion()).thenReturn(1);

        rootFolder = Mockito.mock(Folder.class);
        when(rootFolder.getName()).thenReturn("root");
        when(rootFolder.getId()).thenReturn("root_folder_id");

        parentFolder = Mockito.mock(Folder.class);
        when(parentFolder.getName()).thenReturn("parent_folder");
        when(parentFolder.getId()).thenReturn("root_folder_id");
    }

    @Test
    void createUserFolder_WithParentFolder_ShouldCreateAndSaveFolder() {
        when(entityManager.getReference(User.class, "test_user_id")).thenReturn(testUser);
        when(entityManager.getReference(Folder.class, "parent_folder_id")).thenReturn(parentFolder);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Folder createdFolder = folderService.createUserFolder("test_user_id", "Test Folder", "parent_folder_id");

        assertNotNull(createdFolder);
        assertEquals("Test Folder", createdFolder.getName());
        assertEquals(parentFolder, createdFolder.getParentFolder());
        assertEquals(testUser, createdFolder.getUser());
        verify(folderRepository, times(1)).save(createdFolder);
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