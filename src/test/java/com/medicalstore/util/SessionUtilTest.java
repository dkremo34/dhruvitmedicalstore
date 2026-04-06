package com.medicalstore.util;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionUtilTest {

    @Mock
    private HttpSession session;
    
    @Mock
    private Model model;

    public SessionUtilTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsLoggedIn_WithUser() {
        when(session.getAttribute("user")).thenReturn(new Object());
        assertTrue(SessionUtil.isLoggedIn(session));
    }

    @Test
    void testIsLoggedIn_WithoutUser() {
        when(session.getAttribute("user")).thenReturn(null);
        assertFalse(SessionUtil.isLoggedIn(session));
    }

    @Test
    void testRedirectIfNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);
        assertEquals("redirect:/login", SessionUtil.redirectIfNotLoggedIn(session));
        
        when(session.getAttribute("user")).thenReturn(new Object());
        assertNull(SessionUtil.redirectIfNotLoggedIn(session));
    }
}