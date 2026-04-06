// Enhanced Loading state management with better UX
class LoadingManager {
    static show(button, text = 'Processing...') {
        if (!button || button.disabled) return;
        
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.innerHTML = `<span class="spinner" aria-hidden="true"></span> ${text}`;
        button.classList.add('loading');
        button.setAttribute('aria-busy', 'true');
    }

    static hide(button) {
        if (!button) return;
        
        button.disabled = false;
        button.textContent = button.dataset.originalText || 'Submit';
        button.classList.remove('loading');
        button.removeAttribute('aria-busy');
    }

    static showGlobal(message = 'Loading...') {
        const existing = document.querySelector('.global-loading');
        if (existing) return;

        const loader = document.createElement('div');
        loader.className = 'global-loading';
        loader.innerHTML = `
            <div class="loading-backdrop">
                <div class="loading-content">
                    <span class="spinner" aria-hidden="true"></span>
                    <span>${message}</span>
                </div>
            </div>
        `;
        loader.setAttribute('role', 'status');
        loader.setAttribute('aria-live', 'polite');
        document.body.appendChild(loader);
    }

    static hideGlobal() {
        const loader = document.querySelector('.global-loading');
        if (loader) {
            loader.remove();
        }
    }
}

// Enhanced form handling with better error management and CSRF protection
class FormHandler {
    static init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.attachFormListeners();
            this.setupKeyboardNavigation();
            this.setupCSRFToken();
        });
    }

    static setupCSRFToken() {
        // Get CSRF token from meta tag or cookie
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ||
                     this.getCookie('XSRF-TOKEN');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-XSRF-TOKEN';
        
        if (token) {
            // Set default CSRF header for all AJAX requests
            const originalFetch = window.fetch;
            window.fetch = function(url, options = {}) {
                options.headers = options.headers || {};
                options.headers[header] = token;
                return originalFetch(url, options);
            };
            
            // Set CSRF header for jQuery AJAX if available
            if (window.$ && $.ajaxSetup) {
                $.ajaxSetup({
                    beforeSend: function(xhr) {
                        xhr.setRequestHeader(header, token);
                    }
                });
            }
        }
    }

    static getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    static attachFormListeners() {
        const forms = document.querySelectorAll('form');
        
        forms.forEach(form => {
            form.addEventListener('submit', this.handleFormSubmit.bind(this));
            
            // Add real-time validation
            const inputs = form.querySelectorAll('input, select, textarea');
            inputs.forEach(input => {
                input.addEventListener('blur', () => this.validateField(input));
                input.addEventListener('input', () => this.clearFieldError(input));
            });
        });
    }

    static handleFormSubmit(event) {
        const form = event.target;
        const submitBtn = form.querySelector('button[type="submit"]');
        
        // CSRF Protection: Verify CSRF token exists
        const csrfToken = form.querySelector('input[name="_token"]') || 
                         form.querySelector('meta[name="_token"]')?.content ||
                         document.querySelector('meta[name="_token"]')?.content;
        
        if (!csrfToken && form.method?.toLowerCase() === 'post') {
            event.preventDefault();
            this.showError('Security token missing. Please refresh the page.');
            return false;
        }
        
        // Validate form origin
        if (!this.validateFormOrigin(form)) {
            event.preventDefault();
            this.showError('Invalid form submission detected.');
            return false;
        }
        
        if (submitBtn && !form.dataset.skipLoading) {
            LoadingManager.show(submitBtn);
        }

        // Add timeout protection
        const timeout = setTimeout(() => {
            LoadingManager.hide(submitBtn);
            this.showError('Request timed out. Please try again.');
        }, 30000);

        form.dataset.timeoutId = timeout;
    }
    
    static validateFormOrigin(form) {
        // Validate that form action is same-origin or explicitly allowed
        const action = form.action || window.location.href;
        const actionUrl = new URL(action, window.location.origin);
        
        // Allow same-origin requests
        if (actionUrl.origin === window.location.origin) {
            return true;
        }
        
        // Check if external URL is in allowed list (if any)
        const allowedOrigins = form.dataset.allowedOrigins?.split(',') || [];
        return allowedOrigins.includes(actionUrl.origin);
    }

    static validateField(field) {
        if (!field.checkValidity()) {
            this.showFieldError(field, field.validationMessage);
            return false;
        }
        this.clearFieldError(field);
        return true;
    }

    static showFieldError(field, message) {
        this.clearFieldError(field);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        errorDiv.setAttribute('role', 'alert');
        
        field.parentNode.appendChild(errorDiv);
        field.classList.add('error');
        field.setAttribute('aria-invalid', 'true');
    }

    static clearFieldError(field) {
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        field.classList.remove('error');
        field.removeAttribute('aria-invalid');
    }

    static showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error global-error';
        errorDiv.textContent = message;
        errorDiv.setAttribute('role', 'alert');
        errorDiv.setAttribute('aria-live', 'assertive');
        
        const container = document.querySelector('.container') || document.body;
        container.insertBefore(errorDiv, container.firstChild);
        
        // Auto-remove after 5 seconds
        setTimeout(() => errorDiv.remove(), 5000);
    }

    static setupKeyboardNavigation() {
        // Enhanced keyboard navigation for forms
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.tagName === 'INPUT' && e.target.type !== 'submit') {
                const form = e.target.closest('form');
                if (form) {
                    const inputs = Array.from(form.querySelectorAll('input, select, textarea'));
                    const currentIndex = inputs.indexOf(e.target);
                    const nextInput = inputs[currentIndex + 1];
                    
                    if (nextInput) {
                        nextInput.focus();
                        e.preventDefault();
                    }
                }
            }
        });
    }
}

// Initialize form handling
FormHandler.init();