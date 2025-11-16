# .env File Setup Guide

## ✅ What's Been Configured

Your project now uses a `.env` file for environment variables instead of IntelliJ IDEA configurations.

### Security
- ✅ `.env` file is **git-ignored** (won't be committed)
- ✅ `.env.example` template is **tracked** (safe to commit)
- ✅ All sensitive data stays local

## 📋 Quick Setup

### Step 1: Create Your .env File

If you already have a `.env` file, you're good to go! ✅

If not:
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env` and fill in your actual values
3. Save the file

### Step 2: Required Variables

Make sure your `.env` contains:

```env
# Database
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# JWT Security
JWT_SECRET=your_base64_jwt_secret

# Email (NEW - for verification)
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_gmail_app_password

# Optional: Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Stripe (paiements)
STRIPE_SECRET_KEY=sk_test_your_secret
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable
STRIPE_WEBHOOK_SECRET=whsec_optional_if_configured
```

### Step 3: How It Works

The application automatically loads your `.env` file on startup:
- Spring Boot reads variables from `.env`
- No need to configure IntelliJ environment variables
- Works with `mvnw.cmd` commands too

### Step 4: Stripe Keys

1. Créez un compte Stripe (ou connectez-vous) : https://dashboard.stripe.com/
2. Activez le mode test
3. Récupérez les clés **Publishable** et **Secret** (onglet *Developers > API keys*)
4. Copiez-les dans le `.env` côté backend
5. Utilisez les cartes de test Stripe : https://stripe.com/docs/testing

## 🔐 Getting Gmail App Password

For email verification:

1. Enable **2-Step Verification**: https://myaccount.google.com/security
2. Create **App Password**: https://myaccount.google.com/apppasswords
   - Select "Mail" and your device
   - Click "Generate"
3. Copy the 16-character password (no spaces)
4. Use it as `SMTP_PASSWORD` in your `.env`

## 🧪 Testing

1. Start backend: Run `BackendApplication` in IntelliJ
2. Register a new user
3. Check email for verification link
4. Click link to verify account
5. Login with verified account

## ✅ Success Checklist

- [ ] `.env` file created in `homework-backend/`
- [ ] All required variables filled in
- [ ] Gmail App Password generated and added
- [ ] Backend starts without errors
- [ ] Can register and receive verification emails
- [ ] Email verification link works

## 🚨 Troubleshooting

**Backend won't start?**
- Check console for specific error
- Verify all variables in `.env` are correct
- Ensure MySQL is running

**No verification emails?**
- Check spam folder
- Verify Gmail App Password has no spaces
- Check SMTP settings in console logs
- Ensure 2FA is enabled on Google account

**Variables not loading?**
- Verify `.env` is in `homework-backend/` directory
- Check file has correct format (no spaces around `=`)
- Restart IntelliJ if needed

---

## 📝 Files Reference

- **`.env`** - Your local environment variables (NOT in git)
- **`.env.example`** - Template for other developers (in git)
- **`ENV_SETUP.md`** - Detailed setup instructions
- **`SETUP_CHECKLIST.md`** - Step-by-step checklist

