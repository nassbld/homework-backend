# Email & Payment Setup Checklist ✅

Follow these steps to configure email verification **and** Stripe payments:

## 🔧 Configuration Steps

### Step 1: Get Gmail App Password
- [ ] Enable 2-Step Verification on Google account
- [ ] Generate App Password at https://myaccount.google.com/apppasswords
- [ ] Copy the 16-character password (no spaces)

### Step 2: Create .env File
- [ ] Create a file named `.env` in the `homework-backend` directory
- [ ] Copy the format below and fill in your values:
  ```env
  DB_USERNAME=root
  DB_PASSWORD=your_password
  JWT_SECRET=your_jwt_secret
  SMTP_USERNAME=your_email@gmail.com
  SMTP_PASSWORD=your_16_char_password
  STRIPE_SECRET_KEY=sk_test_your_secret
  STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable
  STRIPE_WEBHOOK_SECRET=whsec_optional_if_configured
  ```
- [ ] Save the file

### Step 3: Retrieve Stripe Keys
- [ ] Log in to https://dashboard.stripe.com/
- [ ] Switch to Test mode
- [ ] Copy the **Publishable key** and **Secret key** (Developers &gt; API keys)
- [ ] Paste them into your `.env`
- [ ] (Optionnel) Créez un webhook secret si vous testez les webhooks locaux

## 🧪 Testing Steps

### Step 4: Start Backend
- [ ] Start MySQL server
- [ ] Run `BackendApplication` in IntelliJ
- [ ] Check console for startup success (no errors)

### Step 5: Start Frontend
- [ ] Run `cd homework-frontend && npm run dev`
- [ ] Open http://localhost:5173/

### Step 6: Test Registration & Verification
- [ ] Register a new account (use une vraie adresse email)
- [ ] Vérifiez que l'email de confirmation est reçu
- [ ] Cliquez sur le lien de vérification
- [ ] Connectez-vous avec le compte vérifié

### Step 7: Test Payment Flow
- [ ] Ouvrez la page d'un cours et cliquez sur "S'inscrire"
- [ ] Procédez au paiement avec une carte de test Stripe (ex: `4242 4242 4242 4242`)
- [ ] Vérifiez que l'inscription apparaît dans "Mes Cours"
- [ ] Essayez d'annuler (remboursement) si le cours est à plus de 48h

## ❌ Troubleshooting

**If emails don't arrive:**
1. Check spam folder
2. Verify App Password has no spaces
3. Check console logs for SMTP errors
4. Ensure 2FA is enabled on Google account

**If payments fail:**
1. Vérifiez les clés Stripe dans `.env`
2. Consultez les logs Stripe dans la console backend
3. Utilisez une carte de test valide

**If backend won't start:**
1. Check all environment variables are set
2. Verify MySQL is running
3. Check console for specific error messages

## ✅ Success Indicators

You'll know it's working when:
- ✅ Backend starts without SMTP/Stripe errors
- ✅ Registration shows success page
- ✅ Vous recevez l'email de vérification
- ✅ Le lien confirme le compte et permet la connexion
- ✅ Paiement Stripe validé et inscription créée
- ✅ Annulation possible (>48h) entraîne un remboursement Stripe

---

**Une fois toutes les étapes validées, votre plateforme est prête pour les inscriptions payantes !** 🎉

