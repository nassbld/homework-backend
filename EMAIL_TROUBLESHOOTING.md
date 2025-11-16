# Email Troubleshooting Guide

## 🔍 What to Check First

After registering a user, check your **backend console logs** for email-related messages. You should see:

### ✅ Success Logs:
```
Attempting to send verification email to: user@example.com
Verification email sent successfully to: user@example.com
```

### ❌ Error Logs:
If you see errors like:
```
❌ FAILED to send verification email to user@example.com: ...
```

## 📋 Common Issues & Solutions

### 1. Check Your .env File

Verify your `.env` file in `homework-backend/` contains:
```env
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_16_character_app_password
```

**Important:** 
- `SMTP_USERNAME` must be your full Gmail address
- `SMTP_PASSWORD` must be a Gmail App Password (NOT your regular Gmail password)
- No spaces around the `=` sign
- No quotes around values

### 2. Gmail App Password Issues

**Generate a new App Password:**
1. Go to: https://myaccount.google.com/apppasswords
2. Make sure 2-Step Verification is enabled
3. Select "Mail" and your device
4. Generate and copy the 16-character password
5. Update your `.env` file with the new password
6. Restart your backend

**Common mistakes:**
- Using regular Gmail password instead of App Password
- Copying password with spaces (remove all spaces)
- 2-Step Verification not enabled

### 3. Check Console Logs

Look for these specific error messages:

**"Authentication failed" or "Invalid credentials":**
- Wrong App Password
- SMTP_USERNAME incorrect
- App Password expired/revoked

**"Connection refused" or "Connection timeout":**
- Check internet connection
- Firewall blocking port 587
- Gmail SMTP temporarily unavailable

**"Could not authenticate" or "Username and Password not accepted":**
- App Password is incorrect
- Need to regenerate App Password
- 2-Step Verification not enabled

### 4. Verify Environment Variables Loaded

Check your backend startup logs. You should see:
- No errors about missing `${SMTP_USERNAME}` or `${SMTP_PASSWORD}`
- Application starts successfully

If you see errors like `Could not resolve placeholder 'SMTP_USERNAME'`:
- Check `.env` file exists in `homework-backend/` directory
- Verify variable names match exactly (case-sensitive)
- Restart backend after changing `.env`

### 5. Test Email Configuration

Add this temporary test endpoint to verify SMTP:

```java
@GetMapping("/test-email")
public ResponseEntity<String> testEmail() {
    try {
        emailService.sendVerificationEmail(
            "your-test-email@gmail.com", 
            "test-token", 
            "Test"
        );
        return ResponseEntity.ok("Test email sent!");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}
```

### 6. Check Spam Folder

Sometimes emails end up in spam:
- Check spam/junk folder
- Mark as "Not Spam" if found
- Add sender to contacts

### 7. Alternative: Use a Different Email Provider

If Gmail doesn't work, you can use other providers:

**Outlook/Hotmail:**
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
```

**Yahoo:**
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
```

## 🧪 Testing Steps

1. **Restart backend** after changing `.env`
2. **Register a new user** (or use existing one)
3. **Check console logs** immediately after registration
4. **Look for email logs** - should see "Attempting to send..." and either success or error
5. **Check email inbox** (and spam folder)

## 📝 Debug Logging

Enhanced logging is now enabled. You'll see:
- SMTP connection attempts
- Email sending status
- Detailed error messages with root causes

## 🔧 Quick Fixes

**If emails still don't send:**

1. **Double-check App Password:**
   - Regenerate at https://myaccount.google.com/apppasswords
   - Copy without spaces
   - Update `.env` file
   - Restart backend

2. **Verify .env file location:**
   - Must be in `homework-backend/` directory
   - Not in `homework-backend/src/`
   - Same level as `pom.xml`

3. **Check console for specific error:**
   - Copy the full error message
   - Look for authentication/connection errors
   - Share error with development team

## ✅ Success Checklist

- [ ] `.env` file has correct `SMTP_USERNAME` and `SMTP_PASSWORD`
- [ ] Gmail App Password is 16 characters (no spaces)
- [ ] 2-Step Verification enabled on Google account
- [ ] Backend starts without errors
- [ ] Console shows "Attempting to send verification email"
- [ ] Console shows success OR detailed error message
- [ ] Checked email inbox AND spam folder

---

**Need more help?** Share the console error logs for specific assistance.

