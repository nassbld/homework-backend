# Environment Variables Setup Guide

## Required Environment Variables

Create a `.env` file in the `homework-backend` directory with these variables:

### Email Verification (NEW)
- **SMTP_USERNAME**: Your Gmail address (e.g., `youremail@gmail.com`)
- **SMTP_PASSWORD**: Gmail App Password (16-character password from Google)

### Database
- **DB_USERNAME**: Your MySQL username
- **DB_PASSWORD**: Your MySQL password

### JWT Security
- **JWT_SECRET**: Base64-encoded secret key for JWT tokens

### Google OAuth2 (if using)
- **GOOGLE_CLIENT_ID**: Your Google OAuth client ID
- **GOOGLE_CLIENT_SECRET**: Your Google OAuth client secret

### Stripe Payments
- **STRIPE_SECRET_KEY**: Secret key from your Stripe dashboard (starts with `sk_test_...` in test mode)
- **STRIPE_PUBLISHABLE_KEY**: Publishable key for the frontend (starts with `pk_test_...`)
- **STRIPE_WEBHOOK_SECRET** *(optionnel)*: Secret used to verify Stripe webhooks, si vous configurez les webhooks localement

## How to Set Up .env File

1. Create a file named `.env` in the `homework-backend` directory
2. Copy the format from below
3. Fill in your actual values
4. Save the file

Example `.env` file:
```env
# Database
DB_USERNAME=root
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_base64_jwt_secret

# Email
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_16_char_app_password

# Google OAuth (optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Stripe
STRIPE_SECRET_KEY=sk_test_your_secret
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable
STRIPE_WEBHOOK_SECRET=whsec_optional_if_configured
```

**Note:** The `.env` file is already git-ignored for security.

## Getting a Gmail App Password

1. Enable **2-Step Verification** on your Google account: https://myaccount.google.com/security
2. Go to **App Passwords**: https://myaccount.google.com/apppasswords
3. Select **Mail** and your device
4. Click **Generate**
5. Copy the 16-character password (remove spaces)
6. Use this as your `SMTP_PASSWORD`

## Testing Email Setup

1. Start your backend application
2. Register a new user account with a real email
3. Check the email inbox for verification link
4. Click the link to verify your account

## Important Notes

- **Never commit** environment variables to git
- The `.gitignore` file already excludes `.idea` and workspace settings
- Gmail App Passwords work immediately after generation
- If emails don't send, check:
  - App Password is correct (no spaces)
  - Less Secure Apps is NOT needed with App Passwords
  - SMTP settings are correct in `application.properties`

