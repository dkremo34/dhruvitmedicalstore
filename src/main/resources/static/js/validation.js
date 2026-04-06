// Enhanced Form validation with better UX and accessibility
class FormValidator {
    static init() {
        this.setupValidationRules();
        this.attachEventListeners();
    }

    static setupValidationRules() {
        this.rules = {
            username: {
                minLength: 3,
                maxLength: 50,
                pattern: /^[a-zA-Z0-9_]+$/,
                message: 'Username must be 3-50 characters and contain only letters, numbers, and underscores'
            },
            password: {
                minLength: 6,
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
                message: 'Password must be at least 6 characters with uppercase, lowercase, and number'
            },
            email: {
                pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message: 'Please enter a valid email address'
            },
            fullName: {
                minLength: 2,
                maxLength: 100,
                pattern: /^[a-zA-Z\s]+$/,
                message: 'Full name must be 2-100 characters and contain only letters and spaces'
            }
        };
    }

    static attachEventListeners() {
        document.addEventListener('DOMContentLoaded', () => {
            const forms = document.querySelectorAll('form');
            
            forms.forEach(form => {
                if (form.action.includes('login')) {
                    form.addEventListener('submit', (e) => this.handleLogin(e, form));
                } else if (form.action.includes('register')) {
                    form.addEventListener('submit', (e) => this.handleRegister(e, form));
                }
                
                // Real-time validation
                const inputs = form.querySelectorAll('input');
                inputs.forEach(input => {
                    input.addEventListener('blur', () => this.validateField(input));
                    input.addEventListener('input', () => this.clearFieldError(input));
                });
            });
        });
    }

    static handleLogin(event, form) {
        event.preventDefault();
        
        if (this.validateLogin(form)) {
            this.submitForm(form);
        } else {
            const submitBtn = form.querySelector('button[type="submit"]');
            LoadingManager.hide(submitBtn);
        }
    }

    static handleRegister(event, form) {
        event.preventDefault();
        
        if (this.validateRegister(form)) {
            this.submitForm(form);
        } else {
            const submitBtn = form.querySelector('button[type="submit"]');
            LoadingManager.hide(submitBtn);
        }
    }

    static validateLogin(form) {
        const username = form.querySelector('input[name="username"]');
        const password = form.querySelector('input[name="password"]');
        
        let isValid = true;
        
        if (!this.validateField(username)) isValid = false;
        if (!this.validateField(password)) isValid = false;
        
        return isValid;
    }

    static validateRegister(form) {
        const fields = ['fullName', 'email', 'username', 'password'];
        const confirmPassword = form.querySelector('input[name="confirmPassword"]');
        const password = form.querySelector('input[name="password"]');
        
        let isValid = true;
        
        // Validate all fields
        fields.forEach(fieldName => {
            const field = form.querySelector(`input[name="${fieldName}"]`);
            if (!this.validateField(field)) isValid = false;
        });
        
        // Check password confirmation
        if (confirmPassword && password.value !== confirmPassword.value) {
            this.showFieldError(confirmPassword, 'Passwords do not match');
            isValid = false;
        }
        
        return isValid;
    }

    static validateField(field) {
        if (!field) return true;
        
        const fieldName = field.name;
        const value = field.value.trim();
        const rules = this.rules[fieldName];
        
        // Clear previous errors
        this.clearFieldError(field);
        
        // Required field check
        if (field.required && !value) {
            this.showFieldError(field, `${this.getFieldLabel(field)} is required`);
            return false;
        }
        
        // Skip validation if field is empty and not required
        if (!value && !field.required) return true;
        
        // Apply specific rules
        if (rules) {
            if (rules.minLength && value.length < rules.minLength) {
                this.showFieldError(field, rules.message);
                return false;
            }
            
            if (rules.maxLength && value.length > rules.maxLength) {
                this.showFieldError(field, rules.message);
                return false;
            }
            
            if (rules.pattern && !rules.pattern.test(value)) {
                this.showFieldError(field, rules.message);
                return false;
            }
        }
        
        return true;
    }

    static showFieldError(field, message) {
        this.clearFieldError(field);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        errorDiv.setAttribute('role', 'alert');
        errorDiv.setAttribute('aria-live', 'polite');
        
        field.parentNode.appendChild(errorDiv);
        field.classList.add('error');
        field.setAttribute('aria-invalid', 'true');
        field.setAttribute('aria-describedby', `${field.name}-error`);
        errorDiv.id = `${field.name}-error`;
    }

    static clearFieldError(field) {
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        field.classList.remove('error');
        field.removeAttribute('aria-invalid');
        field.removeAttribute('aria-describedby');
    }

    static getFieldLabel(field) {
        const label = field.parentNode.querySelector('label');
        return label ? label.textContent : field.placeholder || field.name;
    }

    static submitForm(form) {
        // Create a new form submission to avoid event loop issues
        const formData = new FormData(form);
        
        fetch(form.action, {
            method: form.method,
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else {
                return response.text();
            }
        })
        .then(html => {
            if (html) {
                // Handle validation errors from server
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const errorElement = doc.querySelector('.error');
                
                if (errorElement) {
                    this.showGlobalError(errorElement.textContent);
                }
            }
        })
        .catch(error => {
            console.error('Form submission error:', error);
            this.showGlobalError('An error occurred. Please try again.');
        })
        .finally(() => {
            const submitBtn = form.querySelector('button[type="submit"]');
            LoadingManager.hide(submitBtn);
        });
    }

    static showGlobalError(message) {
        const existingError = document.querySelector('.global-error');
        if (existingError) existingError.remove();
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error global-error';
        errorDiv.textContent = message;
        errorDiv.setAttribute('role', 'alert');
        errorDiv.setAttribute('aria-live', 'assertive');
        
        const form = document.querySelector('form');
        form.parentNode.insertBefore(errorDiv, form);
        
        // Auto-remove after 5 seconds
        setTimeout(() => errorDiv.remove(), 5000);
    }
}

// Initialize validation
FormValidator.init();