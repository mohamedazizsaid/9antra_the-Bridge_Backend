package com._antra.the_bridge.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public MailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Async
  public void sendVerificationEmail(String to, String firstName, String code) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("The Bridge | Code de vérification email");
      helper.setText(buildEmailHtml(firstName, code), true);

      mailSender.send(message);
    } catch (MessagingException | java.io.UnsupportedEncodingException e) {
      log.error("Verification email build failed for {}", to, e);
    } catch (Exception e) {
      log.error("Verification email send failed for {}", to, e);
    }
  }

  @Async
  public void sendReferralEmail(String to, String referrerName) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("The Bridge | Vous avez été parrainé(e) — 10% de réduction !");
      helper.setText(buildReferralEmailHtml(referrerName), true);

      mailSender.send(message);
    } catch (MessagingException | java.io.UnsupportedEncodingException e) {
      log.error("Referral email build failed for {}", to, e);
    } catch (Exception e) {
      log.error("Referral email send failed for {}", to, e);
    }
  }

  private String buildReferralEmailHtml(String referrerName) {
    return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
        <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#08081A;padding:40px 20px;">
            <tr><td align="center">
              <table width="580" cellpadding="0" cellspacing="0"
                     style="background-color:#10102A;border-radius:20px;overflow:hidden;border:1px solid #2A2A4E;box-shadow:0 10px 40px rgba(0,0,0,0.6);">
                <!-- Header Gradient Bar -->
                <tr><td style="height:6px;background:linear-gradient(90deg,#C62761,#F5A623);"></td></tr>
                <tr><td style="padding:40px 40px 20px;text-align:center;">
                  <h1 style="color:#FFFFFF;font-size:26px;font-weight:800;margin:0 0 10px;">
                    🎉 Vous avez été parrainé(e) !
                  </h1>
                  <p style="color:#E2E8F0;font-size:15px;margin:0;line-height:1.6;">
                    <strong style="color:#F5A623;">%s</strong> vous invite à rejoindre <strong style="color:#FFFFFF;">The Bridge — 9antra</strong>
                  </p>
                </td></tr>
                <!-- Discount badge -->
                <tr><td style="padding:20px 40px;text-align:center;">
                  <div style="display:inline-block;background:linear-gradient(135deg,#C62761,#F5A623);
                              border-radius:16px;padding:24px 48px;box-shadow:0 8px 30px rgba(198,39,97,0.35);">
                    <p style="color:#FFFFFF;font-size:48px;font-weight:900;margin:0;line-height:1;">10%%</p>
                    <p style="color:#FFFFFF;font-size:14px;font-weight:800;margin:6px 0 0;letter-spacing:2px;text-transform:uppercase;">
                      DE RÉDUCTION
                    </p>
                  </div>
                </td></tr>
                <!-- CTA -->
                <tr><td style="padding:20px 40px 40px;text-align:center;">
                  <p style="color:#CBD5E1;font-size:14px;line-height:1.7;margin:0 0 24px;">
                    Inscrivez-vous sur The Bridge et bénéficiez automatiquement de 10%% de réduction immédiate sur vos formations et votre stage.
                  </p>
                  <a href="http://localhost:4200/auth/register"
                     style="display:inline-block;background:linear-gradient(90deg,#C62761,#F5A623);
                            color:#FFFFFF;text-decoration:none;padding:14px 34px;border-radius:12px;
                            font-size:14px;font-weight:800;box-shadow:0 4px 20px rgba(198,39,97,0.4);">
                    Créer mon compte →
                  </a>
                </td></tr>
                <!-- Footer -->
                <tr><td style="padding:20px 40px;border-top:1px solid #222244;text-align:center;background-color:#0A0A1C;">
                  <p style="color:#94A3B8;font-size:12px;margin:0;">© 2026 The Bridge — 9antra • Plateforme d'apprentissage</p>
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(referrerName);
  }

  private String buildEmailHtml(String firstName, String code) {
    String[] digits = code.split("");
    StringBuilder codeBoxes = new StringBuilder();
    for (String d : digits) {
      codeBoxes.append("""
              <td align="center" style="padding: 0 6px; text-align: center;">
                <div style="
                  width: 52px; height: 64px;
                  background-color: #16163D;
                  border: 2px solid #C62761;
                  border-radius: 12px;
                  display: inline-flex; align-items: center; justify-content: center;
                  font-size: 28px; font-weight: 800;
                  color: #FFFFFF !important;
                  font-family: 'Courier New', monospace;
                  text-align: center;
                  line-height: 64px;
                  box-shadow: 0 0 20px rgba(198,39,97,0.3);
                ">
                  %s
                </div>
              </td>
          """.formatted(d));
    }

    return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Vérification Email — The Bridge</title>
        </head>
        <body style="margin:0; padding:0; background-color:#08081A; font-family: 'Segoe UI', Arial, sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #08081A; padding: 40px 0;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%; background-color: #0F0F2E; border-radius: 24px; overflow: hidden; border: 1px solid rgba(198,39,97,0.3); box-shadow: 0 10px 40px rgba(0,0,0,0.6);">

                  <!-- HEADER -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #C62761 0%%, #8B1A44 50%%, #F5A623 100%%); padding: 40px 48px; text-align: center;">
                      <h1 style="color: #FFFFFF !important; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px;">The Bridge</h1>
                      <p style="color: #FFFFFF !important; margin: 6px 0 0; font-size: 12px; letter-spacing: 4px; text-transform: uppercase; font-weight: 700;">9antra • Plateforme Certifiée</p>
                    </td>
                  </tr>

                  <!-- BODY -->
                  <tr>
                    <td style="padding: 44px; background-color: #0F0F2E;">

                      <!-- Greeting -->
                      <p style="color: #F8FAFC; font-size: 13px; margin: 0 0 8px; text-transform: uppercase; letter-spacing: 2px; font-weight: 700;">Bienvenue sur The Bridge</p>
                      <h2 style="color: #FFFFFF !important; font-size: 24px; font-weight: 700; margin: 0 0 20px;">Bonjour %s 👋</h2>

                      <p style="color: #E2E8F0; font-size: 15px; line-height: 1.7; margin: 0 0 32px;">
                        Vous venez de créer votre compte stagiaire sur <strong style="color: #FF4D8D;">The Bridge</strong>. Pour sécuriser votre compte et activer votre accès à la plateforme, veuillez confirmer votre adresse email en utilisant le code de vérification ci-dessous.
                      </p>

                      <!-- CODE BOX -->
                      <div style="text-align: center; margin: 36px 0; background-color: #121235; border-radius: 16px; padding: 28px 16px; border: 1px solid #2A2A4E;">
                        <p style="color: #E2E8F0; font-size: 13px; text-transform: uppercase; letter-spacing: 3px; margin: 0 0 20px; font-weight: 700;">Votre code de vérification</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" align="center" style="margin: 0 auto; border-collapse: collapse; table-layout: fixed;">
                          <tr>
                            %s
                          </tr>
                        </table>
                        <p style="color: #CBD5E1; font-size: 13px; margin: 20px 0 0;">Ce code expire dans <strong style="color: #F5A623;">15 minutes</strong></p>
                      </div>

                      <!-- DIVIDER -->
                      <div style="height: 1px; background: linear-gradient(to right, transparent, rgba(198,39,97,0.4), transparent); margin: 32px 0;"></div>

                      <!-- SECURITY NOTE -->
                      <table cellpadding="0" cellspacing="0" style="background-color: rgba(198,39,97,0.12); border: 1px solid rgba(198,39,97,0.35); border-radius: 12px; padding: 18px; width: 100%%;">
                        <tr>
                          <td width="36" valign="top">
                            <div style="width: 30px; height: 30px; background-color: rgba(198,39,97,0.3); border-radius: 8px; text-align: center; line-height: 30px; font-size: 16px;">🔒</div>
                          </td>
                          <td style="padding-left: 12px;">
                            <p style="color: #FF4D8D; font-size: 13px; font-weight: 800; margin: 0 0 4px; text-transform: uppercase; letter-spacing: 1px;">Note de sécurité</p>
                            <p style="color: #E2E8F0; font-size: 13px; margin: 0; line-height: 1.6;">Ne partagez jamais ce code avec personne. L'équipe de The Bridge ne vous demandera jamais votre code de vérification.</p>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- FOOTER -->
                  <tr>
                    <td style="padding: 28px 44px; background-color: #08081A; border-top: 1px solid rgba(255,255,255,0.08); text-align: center;">
                      <p style="color: #94A3B8; font-size: 12px; margin: 0 0 8px;">
                        Si vous n'avez pas créé de compte sur The Bridge, ignorez cet email.
                      </p>
                      <p style="color: #94A3B8; font-size: 11px; margin: 0;">
                        © 2026 9antra • The Bridge — Tous droits réservés
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """
        .formatted(firstName, codeBoxes.toString());
  }

  @Async
  public void sendPasswordResetEmail(String to, String firstName, String code) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("🔑 Réinitialisation de votre mot de passe — The Bridge");
      helper.setText(buildResetPasswordEmailHtml(firstName, code), true);

      mailSender.send(message);
    } catch (MessagingException | java.io.UnsupportedEncodingException e) {
      log.error("Password reset email build failed for {}", to, e);
    } catch (Exception e) {
      log.error("Password reset email send failed for {}", to, e);
    }
  }

  private String buildResetPasswordEmailHtml(String firstName, String code) {
    String[] digits = code.split("");
    StringBuilder codeBoxes = new StringBuilder();
    for (String d : digits) {
      codeBoxes.append("""
              <td align="center" style="padding: 0 6px; text-align: center;">
                <div style="
                  width: 52px; height: 64px;
                  background-color: #16163D;
                  border: 2px solid #F5A623;
                  border-radius: 12px;
                  display: inline-flex; align-items: center; justify-content: center;
                  font-size: 28px; font-weight: 800;
                  color: #FFFFFF !important;
                  font-family: 'Courier New', monospace;
                  text-align: center;
                  line-height: 64px;
                  box-shadow: 0 0 20px rgba(245,166,35,0.3);
                ">
                  %s
                </div>
              </td>
          """.formatted(d));
    }

    return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Réinitialisation du Mot de passe — The Bridge</title>
        </head>
        <body style="margin:0; padding:0; background-color:#08081A; font-family: 'Segoe UI', Arial, sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #08081A; padding: 40px 0;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%; background-color: #0F0F2E; border-radius: 24px; overflow: hidden; border: 1px solid rgba(245,166,35,0.3); box-shadow: 0 10px 40px rgba(0,0,0,0.6);">

                  <!-- HEADER -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #C62761 0%%, #8B1A44 50%%, #F5A623 100%%); padding: 40px 48px; text-align: center;">
                      <h1 style="color: #FFFFFF !important; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px;">The Bridge</h1>
                      <p style="color: #FFFFFF !important; margin: 6px 0 0; font-size: 12px; letter-spacing: 4px; text-transform: uppercase; font-weight: 700;">9antra • Plateforme Certifiée</p>
                    </td>
                  </tr>

                  <!-- BODY -->
                  <tr>
                    <td style="padding: 44px; background-color: #0F0F2E;">

                      <p style="color: #F5A623; font-size: 13px; margin: 0 0 8px; text-transform: uppercase; letter-spacing: 2px; font-weight: 700;">Sécurité du compte</p>
                      <h2 style="color: #FFFFFF !important; font-size: 24px; font-weight: 700; margin: 0 0 20px;">Bonjour %s 👋</h2>

                      <p style="color: #E2E8F0; font-size: 15px; line-height: 1.7; margin: 0 0 32px;">
                        Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte sur <strong style="color: #F5A623;">The Bridge</strong>. Veuillez utiliser le code OTP temporaire ci-dessous pour changer votre mot de passe en toute sécurité.
                      </p>

                      <!-- CODE BOX -->
                      <div style="text-align: center; margin: 36px 0; background-color: #121235; border-radius: 16px; padding: 28px 16px; border: 1px solid #2A2A4E;">
                        <p style="color: #E2E8F0; font-size: 13px; text-transform: uppercase; letter-spacing: 3px; margin: 0 0 20px; font-weight: 700;">Votre code de réinitialisation</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" align="center" style="margin: 0 auto; border-collapse: collapse; table-layout: fixed;">
                          <tr>
                            %s
                          </tr>
                        </table>
                        <p style="color: #CBD5E1; font-size: 13px; margin: 20px 0 0;">Ce code expire dans <strong style="color: #F5A623;">15 minutes</strong></p>
                      </div>

                      <!-- DIVIDER -->
                      <div style="height: 1px; background: linear-gradient(to right, transparent, rgba(245,166,35,0.4), transparent); margin: 32px 0;"></div>

                      <!-- SECURITY NOTE -->
                      <table cellpadding="0" cellspacing="0" style="background-color: rgba(245,166,35,0.12); border: 1px solid rgba(245,166,35,0.35); border-radius: 12px; padding: 18px; width: 100%%;">
                        <tr>
                          <td width="36" valign="top">
                            <div style="width: 30px; height: 30px; background-color: rgba(245,166,35,0.25); border-radius: 8px; text-align: center; line-height: 30px; font-size: 16px;">🔑</div>
                          </td>
                          <td style="padding-left: 12px;">
                            <p style="color: #F5A623; font-size: 13px; font-weight: 800; margin: 0 0 4px; text-transform: uppercase; letter-spacing: 1px;">Attention</p>
                            <p style="color: #E2E8F0; font-size: 13px; margin: 0; line-height: 1.6;">Si vous n'avez pas demandé de réinitialisation de mot de passe, vous pouvez ignorer cet email en toute sécurité. Votre mot de passe actuel restera inchangé.</p>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- FOOTER -->
                  <tr>
                    <td style="padding: 28px 44px; background-color: #08081A; border-top: 1px solid rgba(255,255,255,0.08); text-align: center;">
                      <p style="color: #94A3B8; font-size: 12px; margin: 0;">
                        © 2026 9antra • The Bridge — Tous droits réservés
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """
        .formatted(firstName, codeBoxes.toString());
  }

  @Async
  public void sendSessionReminder(String to, String firstName, String messageContent, com._antra.the_bridge.entity.Session session) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("⏰ Rappel de séance — The Bridge");
      
      String htmlContent = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Rappel de Séance</title></head>
          <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#08081A;padding:36px 16px;">
              <tr><td align="center">
                <table width="580" cellpadding="0" cellspacing="0"
                       style="background-color:#0F0F2E;border-radius:20px;overflow:hidden;border:1px solid #C62761;box-shadow:0 10px 40px rgba(0,0,0,0.6);">
                  <tr><td style="height:6px;background:linear-gradient(90deg,#C62761,#F5A623);"></td></tr>
                  <tr><td style="padding:36px 40px 20px;">
                    <span style="color:#F5A623;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:6px;">Planning Académique</span>
                    <h2 style="color:#FFFFFF !important;font-size:24px;font-weight:800;margin:0 0 16px;">⏰ Rappel de Séance de Cours</h2>
                    <p style="color:#E2E8F0;font-size:15px;line-height:1.7;margin:0 0 16px;">
                      Bonjour <strong style="color:#FFFFFF;">%s</strong>,
                    </p>
                    <p style="color:#CBD5E1;font-size:14px;line-height:1.7;margin:0 0 24px;">
                      %s
                    </p>
                    <div style="background-color:#16163D;border-left:4px solid #F5A623;padding:18px 20px;border-radius:8px;margin:0 0 24px;">
                      <p style="color:#FFFFFF !important;font-size:14px;font-weight:700;margin:0 0 6px;">
                        📅 Date & Heure de la séance :
                      </p>
                      <p style="color:#F5A623;font-size:16px;font-weight:800;margin:0;font-family:monospace;">
                        %s à %s
                      </p>
                    </div>
                  </td></tr>
                  <tr><td style="padding:20px 40px;border-top:1px solid #222244;background-color:#0A0A1C;text-align:center;">
                    <p style="color:#94A3B8;font-size:12px;margin:0;">L'équipe The Bridge — 9antra • Formation Professionnelle</p>
                  </td></tr>
                </table>
              </td></tr>
            </table>
          </body>
          </html>
          """.formatted(firstName, messageContent, session.getSessionDate().toString(), session.getStartTime().toString());

      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send session email reminder to {}", to, e);
    }
  }

  @Async
  public void sendPaymentReminder(String to, String firstName, String phaseTitle, Double amount, LocalDate dueDate, int daysRemaining) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("⚠️ Rappel de paiement — Échéance dans " + daysRemaining + " jours");

      String htmlContent = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Rappel de Paiement</title></head>
          <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#08081A;padding:36px 16px;">
              <tr><td align="center">
                <table width="580" cellpadding="0" cellspacing="0"
                       style="background-color:#0F0F2E;border-radius:20px;overflow:hidden;border:1px solid #F5A623;box-shadow:0 10px 40px rgba(0,0,0,0.6);">
                  <tr><td style="height:6px;background:linear-gradient(90deg,#C62761,#F5A623);"></td></tr>
                  <tr><td style="padding:36px 40px 20px;">
                    <span style="color:#F5A623;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:6px;">Règlement Académique</span>
                    <h2 style="color:#FFFFFF !important;font-size:24px;font-weight:800;margin:0 0 16px;">⚠️ Échéance de Paiement Approchante</h2>
                    <p style="color:#E2E8F0;font-size:15px;line-height:1.7;margin:0 0 16px;">
                      Bonjour <strong style="color:#FFFFFF;">%s</strong>,
                    </p>
                    <p style="color:#CBD5E1;font-size:14px;line-height:1.7;margin:0 0 20px;">
                      Ceci est un rappel concernant le règlement de votre formation pour la phase : <strong style="color:#FFFFFF;">%s</strong>.
                    </p>
                    <div style="background-color:#16163D;border-left:4px solid #C62761;padding:20px;border-radius:8px;margin:0 0 24px;">
                      <p style="color:#E2E8F0;font-size:14px;margin:6px 0;">
                        <strong style="color:#FFFFFF;">Montant :</strong> <span style="color:#F5A623;font-weight:800;font-size:16px;">%s TND</span>
                      </p>
                      <p style="color:#E2E8F0;font-size:14px;margin:6px 0;">
                        <strong style="color:#FFFFFF;">Date d'échéance :</strong> %s
                      </p>
                      <p style="color:#FF4D8D;font-size:14px;font-weight:700;margin:6px 0;">
                        Temps restant : %s jours
                      </p>
                    </div>
                    <p style="color:#CBD5E1;font-size:14px;line-height:1.7;margin:0 0 24px;">
                      Veuillez régulariser votre situation depuis votre espace stagiaire afin de poursuivre vos cours en toute sérénité.
                    </p>
                    <div style="text-align:center;margin:10px 0 10px;">
                      <a href="http://localhost:4200/dashboard/stagiaire/paiements"
                         style="display:inline-block;background:linear-gradient(90deg,#C62761,#F5A623);color:#FFFFFF;text-decoration:none;padding:12px 30px;border-radius:10px;font-size:13px;font-weight:800;">
                        Accéder à mes Paiements →
                      </a>
                    </div>
                  </td></tr>
                  <tr><td style="padding:20px 40px;border-top:1px solid #222244;background-color:#0A0A1C;text-align:center;">
                    <p style="color:#94A3B8;font-size:12px;margin:0;">L'équipe The Bridge — 9antra</p>
                  </td></tr>
                </table>
              </td></tr>
            </table>
          </body>
          </html>
          """.formatted(firstName, phaseTitle, amount, dueDate.toString(), daysRemaining);

      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send payment email reminder to {}", to, e);
    }
  }

  @Async
  public void sendCertificateEmail(String to, String firstName, String lastName,
                                    String phaseTitle, String formationTitle,
                                    String certificateNumber, String blockchainHash,
                                    String issueDate) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("🏅 Votre Certificat Blockchain — " + phaseTitle + " | The Bridge");
      helper.setText(buildCertificateEmailHtml(firstName, lastName, phaseTitle, formationTitle,
              certificateNumber, blockchainHash, issueDate), true);

      mailSender.send(message);
      log.info("Certificate email sent to {} for phase '{}'", to, phaseTitle);
    } catch (Exception e) {
      log.error("Failed to send certificate email to {}", to, e);
    }
  }

  private String buildCertificateEmailHtml(String firstName, String lastName,
                                            String phaseTitle, String formationTitle,
                                            String certNumber, String blockchainHash,
                                            String issueDate) {
    return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><title>Certificat The Bridge</title></head>
        <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
          <div style="max-width:650px;margin:0 auto;background-color:#08081A;padding:20px 10px;">

            <!-- HERO HEADER -->
            <div style="background:linear-gradient(135deg,#1A0A2E 0%%,#0D0D2B 50%%,#1A0A1A 100%%);padding:40px;text-align:center;border-bottom:3px solid #C62761;border-radius:20px 20px 0 0;">
              <div style="display:inline-block;color:#FFFFFF;font-size:32px;font-weight:900;letter-spacing:1px;margin-bottom:8px;">
                THE BRIDGE
              </div>
              <div style="color:#CBD5E1;font-size:12px;text-transform:uppercase;letter-spacing:4px;font-weight:700;">9antra · Formation Professionnelle</div>
            </div>

            <!-- GOLD BANNER -->
            <div style="background:linear-gradient(90deg,#C62761,#F5A623,#C62761);padding:14px;text-align:center;">
              <span style="color:#FFFFFF !important;font-size:14px;font-weight:800;text-transform:uppercase;letter-spacing:3px;">🏅 Certificat Blockchain Délivré</span>
            </div>

            <!-- MAIN CONTENT -->
            <div style="background-color:#0F0F2E;padding:44px 40px;border-left:1px solid #2A2A4E;border-right:1px solid #2A2A4E;">

              <!-- Greeting -->
              <p style="color:#F5A623;font-size:13px;text-transform:uppercase;letter-spacing:3px;font-weight:800;margin:0 0 8px;">Félicitations</p>
              <h2 style="color:#FFFFFF !important;font-size:26px;font-weight:800;margin:0 0 20px;">%s %s 🎉</h2>

              <p style="color:#E2E8F0;font-size:15px;line-height:1.8;margin:0 0 28px;">
                Vous avez réussi avec succès la phase <strong style="color:#F5A623;">"%s"</strong>
                du programme <strong style="color:#FF4D8D;">%s</strong>. Votre certificat officiel a été
                <strong style="color:#FFFFFF;">sécurisé sur la blockchain Polygon</strong> et est
                désormais infalsifiable et vérifiable dans le monde entier.
              </p>

              <!-- CERTIFICATE CARD -->
              <div style="background-color:#16163D;border:1px solid #C62761;border-radius:16px;padding:28px;margin:0 0 28px;position:relative;">
                <div style="margin-bottom:18px;">
                  <span style="color:#CBD5E1;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:4px;">Numéro de certificat officiel</span>
                  <span style="color:#F5A623;font-size:15px;font-weight:800;font-family:monospace;">%s</span>
                </div>

                <div style="margin-bottom:18px;">
                  <span style="color:#CBD5E1;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:4px;">Date d'émission certifiée</span>
                  <span style="color:#FFFFFF;font-size:15px;font-weight:700;">%s</span>
                </div>

                <div style="background-color:#0B0B20;border-radius:8px;padding:14px;border:1px solid #2A2A4E;">
                  <span style="color:#CBD5E1;font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:6px;">Empreinte Blockchain (Polygon)</span>
                  <span style="color:#FF6B9D;font-size:11px;font-family:monospace;word-break:break-all;font-weight:700;">%s</span>
                </div>
              </div>

              <!-- HOW TO VERIFY -->
              <div style="background-color:#1A1A35;border-left:4px solid #F5A623;padding:18px 20px;border-radius:4px;margin:0 0 28px;">
                <p style="color:#F5A623;font-size:14px;font-weight:800;margin:0 0 8px;">🔍 Comment vérifier votre certificat ?</p>
                <p style="color:#E2E8F0;font-size:13px;margin:0;line-height:1.7;">
                  Rendez-vous sur <strong style="color:#FFFFFF;">the-bridge.9antra.tn/verify</strong> et entrez votre numéro de certificat.
                  Votre certificat sera vérifié instantanément via la blockchain.
                </p>
              </div>

              <p style="color:#E2E8F0;font-size:14px;line-height:1.7;margin:0;">
                Vous pouvez télécharger votre certificat PDF depuis votre <strong style="color:#FF4D8D;">espace stagiaire</strong> à tout moment.
              </p>
            </div>

            <!-- FOOTER -->
            <div style="background-color:#060618;padding:28px 40px;border:1px solid #2A2A4E;border-top:none;border-radius:0 0 20px 20px;text-align:center;">
              <p style="color:#94A3B8;font-size:12px;margin:0 0 8px;">© 2026 The Bridge — 9antra · Formation Professionnelle</p>
              <p style="color:#94A3B8;font-size:11px;margin:0;">Ce message est envoyé automatiquement par la plateforme The Bridge.</p>
            </div>

          </div>
        </body>
        </html>
        """.formatted(firstName, lastName, phaseTitle, formationTitle,
            certNumber, issueDate, blockchainHash);
  }

  @Async
  public void sendFormateurWelcomeEmail(String to, String firstName, String lastName, String tempPassword) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("🎓 Bienvenue sur The Bridge — Vos identifiants d'accès");
      helper.setText("""
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Bienvenue Formateur — The Bridge</title></head>
          <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
            <div style="max-width:600px;margin:20px auto;background-color:#0F0F2E;border-radius:20px;overflow:hidden;border:1px solid rgba(198,39,97,0.3);box-shadow:0 10px 40px rgba(0,0,0,0.6);">
              <div style="background:linear-gradient(135deg,#C62761,#8B1A44,#F5A623);padding:36px;text-align:center;">
                <h1 style="color:#FFFFFF !important;margin:0;font-size:26px;font-weight:800;">The Bridge — 9antra</h1>
                <p style="color:#FFFFFF !important;margin:8px 0 0;font-size:12px;letter-spacing:3px;text-transform:uppercase;font-weight:700;">Espace Formateur</p>
              </div>
              <div style="padding:40px;">
                <h2 style="color:#FFFFFF !important;font-size:22px;margin:0 0 16px;">Bienvenue %s %s 👋</h2>
                <p style="color:#E2E8F0;font-size:14px;line-height:1.8;margin:0 0 24px;">
                  Un compte formateur a été créé pour vous sur <strong style="color:#FF4D8D;">The Bridge</strong>.
                  Vous pouvez accéder à la plateforme avec les identifiants ci-dessous :
                </p>
                <div style="background-color:#16163D;border:1px solid rgba(198,39,97,0.3);border-radius:12px;padding:24px;margin:0 0 24px;">
                  <p style="color:#CBD5E1;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:2px;margin:0 0 6px;">Identifiant / Email</p>
                  <p style="color:#FFFFFF !important;font-size:16px;font-weight:800;font-family:monospace;margin:0 0 16px;">%s</p>
                  <p style="color:#CBD5E1;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:2px;margin:0 0 6px;">Mot de passe temporaire</p>
                  <p style="color:#F5A623;font-size:18px;font-weight:800;font-family:monospace;margin:0;">%s</p>
                </div>
                <div style="background-color:rgba(245,166,35,0.12);border-left:4px solid #F5A623;padding:16px;border-radius:4px;">
                  <p style="color:#F5A623;font-size:13px;font-weight:800;margin:0 0 4px;">⚠️ Important</p>
                  <p style="color:#E2E8F0;font-size:13px;margin:0;line-height:1.6;">Vous serez invité(e) à changer votre mot de passe lors de votre première connexion.</p>
                </div>
                <div style="text-align:center;margin-top:28px;">
                  <a href="http://localhost:4200/auth/login"
                     style="display:inline-block;background:linear-gradient(90deg,#C62761,#F5A623);color:#FFFFFF;text-decoration:none;padding:14px 34px;border-radius:12px;font-size:14px;font-weight:800;">
                    Accéder à mon espace →
                  </a>
                </div>
              </div>
              <div style="background-color:#08081A;padding:24px;text-align:center;border-top:1px solid #1A1A3E;">
                <p style="color:#94A3B8;font-size:12px;margin:0;">© 2026 9antra • The Bridge</p>
              </div>
            </div>
          </body>
          </html>
          """.formatted(firstName, lastName, to, tempPassword), true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send formateur welcome email to {}", to, e);
    }
  }

  // ─── Combo Confirmation Email ───────────────────────────────────────────────

  @Async
  public void sendComboConfirmationEmail(
      String to,
      String firstName,
      String receiptRef,
      java.util.List<String> formationTitles,
      double totalPrice,
      double discountPercent,
      double finalPrice) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail, "The Bridge — 9antra");
      helper.setTo(to);
      helper.setSubject("The Bridge | ✅ Votre parcours personnalisé est activé — " + receiptRef);

      StringBuilder rows = new StringBuilder();
      for (String title : formationTitles) {
        rows.append("""
            <tr>
              <td style="padding:12px 18px;border-bottom:1px solid #2A2A55;color:#F8FAFC;font-size:14px;font-weight:600;">%s</td>
            </tr>
            """.formatted(title));
      }

      String html = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Parcours Confirmé</title></head>
          <body style="margin:0;padding:0;background-color:#08081A;font-family:'Segoe UI',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
              <tr><td align="center" style="padding:40px 20px;">
                <table width="600" cellpadding="0" cellspacing="0"
                       style="background-color:#0F0F2E;border-radius:20px;
                              border:1px solid #2A2A4E;overflow:hidden;box-shadow:0 10px 40px rgba(0,0,0,0.6);">
                  <!-- Header Gradient Bar -->
                  <tr><td style="background:linear-gradient(90deg,#C62761,#F5A623);height:6px;"></td></tr>
                  <tr><td style="padding:36px 40px 20px;">
                    <div style="font-size:28px;margin-bottom:6px;">🎉</div>
                    <h1 style="margin:0;color:#FFFFFF !important;font-size:24px;font-weight:800;">
                      Parcours activé, %s !
                    </h1>
                    <p style="color:#E2E8F0;font-size:14px;margin:8px 0 0;line-height:1.6;">
                      Votre parcours personnalisé a été confirmé et vos inscriptions sont désormais actives.
                    </p>
                  </td></tr>
                  <!-- Receipt ref -->
                  <tr><td style="padding:0 40px 24px;">
                    <div style="background-color:#16163D;border:1px solid #C62761;border-radius:12px;
                                padding:16px 20px;">
                      <span style="font-size:11px;color:#CBD5E1;text-transform:uppercase;
                                   letter-spacing:2px;font-weight:700;display:block;margin-bottom:4px;">Référence du reçu</span>
                      <span style="font-size:18px;font-weight:800;color:#F5A623;
                                   font-family:'Courier New',monospace;">%s</span>
                    </div>
                  </td></tr>
                  <!-- Formations -->
                  <tr><td style="padding:0 40px 16px;">
                    <p style="color:#E2E8F0;font-size:13px;text-transform:uppercase;
                               letter-spacing:1px;font-weight:700;margin:0 0 10px;">Formations incluses</p>
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background-color:#141438;border:1px solid #2A2A55;border-radius:12px;overflow:hidden;">
                      %s
                    </table>
                  </td></tr>
                  <!-- Pricing -->
                  <tr><td style="padding:0 40px 32px;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background-color:#141438;border:1px solid #2A2A55;border-radius:12px;padding:16px;">
                      <tr>
                        <td style="padding:10px 16px;color:#CBD5E1;font-size:14px;">Sous-total</td>
                        <td style="padding:10px 16px;text-align:right;color:#FFFFFF;font-size:14px;font-weight:700;">%.2f TND</td>
                      </tr>
                      <tr>
                        <td style="padding:10px 16px;color:#F5A623;font-size:14px;font-weight:700;">
                          Remise combo (%.0f%%)
                        </td>
                        <td style="padding:10px 16px;text-align:right;color:#F5A623;font-size:14px;font-weight:800;">
                          -%.2f TND
                        </td>
                      </tr>
                      <tr style="border-top:1px solid #2A2A55;">
                        <td style="padding:14px 16px;color:#FFFFFF;font-size:16px;font-weight:800;">Total réglé</td>
                        <td style="padding:14px 16px;text-align:right;color:#FF4D8D;
                                   font-size:19px;font-weight:900;font-family:'Courier New',monospace;">
                          %.2f TND
                        </td>
                      </tr>
                    </table>
                  </td></tr>
                  <!-- CTA -->
                  <tr><td style="padding:0 40px 40px;text-align:center;">
                    <a href="http://localhost:4200/dashboard/stagiaire/stage"
                       style="display:inline-block;background:linear-gradient(90deg,#C62761,#F5A623);
                              color:#FFFFFF;text-decoration:none;padding:14px 34px;border-radius:12px;
                              font-size:14px;font-weight:800;box-shadow:0 4px 20px rgba(198,39,97,0.4);">
                      Accéder à mon espace →
                    </a>
                  </td></tr>
                  <!-- Footer -->
                  <tr><td style="padding:20px 40px;border-top:1px solid #222244;background-color:#0A0A1C;text-align:center;">
                    <p style="color:#94A3B8;font-size:12px;margin:0;">© 2026 The Bridge — 9antra • Plateforme d'apprentissage</p>
                  </td></tr>
                </table>
              </td></tr>
            </table>
          </body>
          </html>
          """.formatted(
              firstName, receiptRef, rows.toString(),
              totalPrice, discountPercent, totalPrice - finalPrice, finalPrice);

      helper.setText(html, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send combo confirmation email to {}", to, e);
    }
  }
}
