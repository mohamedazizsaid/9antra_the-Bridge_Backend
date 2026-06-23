package com._antra.the_bridge.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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

  private String buildEmailHtml(String firstName, String code) {
    String[] digits = code.split("");
    StringBuilder codeBoxes = new StringBuilder();
    for (String d : digits) {
      codeBoxes.append("""
              <td align="center" style="padding: 0 6px; text-align: center;">
                <div style="
                  width: 52px; height: 64px;
                  background: linear-gradient(135deg, #1A1A3E 0%%, #0D0D2B 100%%);
                  border: 2px solid #C62761;
                  border-radius: 12px;
                  display: inline-flex; align-items: center; justify-content: center;
                  font-size: 28px; font-weight: 800;
                  color: #FFFFFF;
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
        <body style="margin:0; padding:0; background-color:#08081A; font-family: 'Inter', Arial, sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background: #08081A; padding: 40px 0;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%; background: #0F0F2E; border-radius: 24px; overflow: hidden; border: 1px solid rgba(198,39,97,0.2); box-shadow: 0 0 60px rgba(198,39,97,0.1);">

                  <!-- HEADER -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #C62761 0%%, #8B1A44 50%%, #F5A623 100%%); padding: 40px 48px; text-align: center;">
                      <!-- Bridge Logo SVG -->
                      <svg width="48" height="60" viewBox="0 0 80 100" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-bottom: 16px;">
                        <path d="M40 10 C20 10 10 25 10 38 C10 51 20 58 40 58 C48 58 54 55 58 50" stroke="white" stroke-width="8" stroke-linecap="round" fill="none"/>
                        <path d="M40 90 C60 90 70 75 70 62 C70 49 60 42 40 42 C32 42 26 45 22 50" stroke="rgba(255,255,255,0.7)" stroke-width="8" stroke-linecap="round" fill="none"/>
                      </svg>
                      <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px;">The Bridge</h1>
                      <p style="color: rgba(255,255,255,0.7); margin: 6px 0 0; font-size: 12px; letter-spacing: 4px; text-transform: uppercase;">9antra • Plateforme Certifiée</p>
                    </td>
                  </tr>

                  <!-- BODY -->
                  <tr>
                    <td style="padding: 48px; background: #0F0F2E;">

                      <!-- Greeting -->
                      <p style="color: #A0A0C8; font-size: 14px; margin: 0 0 8px; text-transform: uppercase; letter-spacing: 2px; font-weight: 600;">Bienvenue sur The Bridge</p>
                      <h2 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 0 0 24px;">Bonjour %s 👋</h2>

                      <p style="color: #8888AA; font-size: 15px; line-height: 1.7; margin: 0 0 32px;">
                        Vous venez de créer votre compte stagiaire sur <strong style="color: #C62761;">The Bridge</strong>. Pour sécuriser votre compte et activer votre accès à la plateforme, veuillez confirmer votre adresse email en utilisant le code de vérification ci-dessous.
                      </p>

                      <!-- CODE BOX -->
                      <div style="text-align: center; margin: 40px 0;">
                        <p style="color: #A0A0C8; font-size: 12px; text-transform: uppercase; letter-spacing: 3px; margin: 0 0 20px; font-weight: 600;">Votre code de vérification</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" align="center" style="margin: 0 auto; border-collapse: collapse; table-layout: fixed;">
                          <tr>
                            %s
                          </tr>
                        </table>
                        <p style="color: #666688; font-size: 12px; margin: 20px 0 0;">Ce code expire dans <strong style="color: #F5A623;">15 minutes</strong></p>
                      </div>

                      <!-- DIVIDER -->
                      <div style="height: 1px; background: linear-gradient(to right, transparent, rgba(198,39,97,0.4), transparent); margin: 40px 0;"></div>

                      <!-- SECURITY NOTE -->
                      <table cellpadding="0" cellspacing="0" style="background: rgba(198,39,97,0.08); border: 1px solid rgba(198,39,97,0.2); border-radius: 12px; padding: 20px; width: 100%%;">
                        <tr>
                          <td width="40" valign="top">
                            <div style="width: 32px; height: 32px; background: rgba(198,39,97,0.2); border-radius: 8px; text-align: center; line-height: 32px; font-size: 16px;">🔒</div>
                          </td>
                          <td style="padding-left: 12px;">
                            <p style="color: #C62761; font-size: 12px; font-weight: 700; margin: 0 0 4px; text-transform: uppercase; letter-spacing: 1px;">Note de sécurité</p>
                            <p style="color: #8888AA; font-size: 13px; margin: 0; line-height: 1.5;">Ne partagez jamais ce code avec personne. L'équipe de The Bridge ne vous demandera jamais votre code de vérification.</p>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- FOOTER -->
                  <tr>
                    <td style="padding: 32px 48px; background: #08081A; border-top: 1px solid rgba(255,255,255,0.05); text-align: center;">
                      <p style="color: #444466; font-size: 12px; margin: 0 0 8px;">
                        Si vous n'avez pas créé de compte sur The Bridge, ignorez cet email.
                      </p>
                      <p style="color: #333355; font-size: 11px; margin: 0;">
                        © 2026 9antra • The Bridge — Tous droits réservés
                      </p>
                      <div style="margin-top: 20px;">
                        <div style="display: inline-block; width: 24px; height: 3px; background: linear-gradient(to right, #C62761, #F5A623); border-radius: 2px;"></div>
                      </div>
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
                  background: linear-gradient(135deg, #1A1A3E 0%%, #0D0D2B 100%%);
                  border: 2px solid #F5A623;
                  border-radius: 12px;
                  display: inline-flex; align-items: center; justify-content: center;
                  font-size: 28px; font-weight: 800;
                  color: #FFFFFF;
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
        <body style="margin:0; padding:0; background-color:#08081A; font-family: 'Inter', Arial, sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background: #08081A; padding: 40px 0;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%; background: #0F0F2E; border-radius: 24px; overflow: hidden; border: 1px solid rgba(245,166,35,0.2); box-shadow: 0 0 60px rgba(245,166,35,0.1);">

                  <!-- HEADER -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #C62761 0%%, #8B1A44 50%%, #F5A623 100%%); padding: 40px 48px; text-align: center;">
                      <svg width="48" height="60" viewBox="0 0 80 100" fill="none" xmlns="http://www.w3.org/2000/svg" style="margin-bottom: 16px;">
                        <path d="M40 10 C20 10 10 25 10 38 C10 51 20 58 40 58 C48 58 54 55 58 50" stroke="white" stroke-width="8" stroke-linecap="round" fill="none"/>
                        <path d="M40 90 C60 90 70 75 70 62 C70 49 60 42 40 42 C32 42 26 45 22 50" stroke="rgba(255,255,255,0.7)" stroke-width="8" stroke-linecap="round" fill="none"/>
                      </svg>
                      <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px;">The Bridge</h1>
                      <p style="color: rgba(255,255,255,0.7); margin: 6px 0 0; font-size: 12px; letter-spacing: 4px; text-transform: uppercase;">9antra • Plateforme Certifiée</p>
                    </td>
                  </tr>

                  <!-- BODY -->
                  <tr>
                    <td style="padding: 48px; background: #0F0F2E;">

                      <p style="color: #F5A623; font-size: 14px; margin: 0 0 8px; text-transform: uppercase; letter-spacing: 2px; font-weight: 600;">Sécurité du compte</p>
                      <h2 style="color: #FFFFFF; font-size: 24px; font-weight: 700; margin: 0 0 24px;">Bonjour %s 👋</h2>

                      <p style="color: #8888AA; font-size: 15px; line-height: 1.7; margin: 0 0 32px;">
                        Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte sur <strong style="color: #F5A623;">The Bridge</strong>. Veuillez utiliser le code OTP temporaire ci-dessous pour changer votre mot de passe.
                      </p>

                      <!-- CODE BOX -->
                      <div style="text-align: center; margin: 40px 0;">
                        <p style="color: #A0A0C8; font-size: 12px; text-transform: uppercase; letter-spacing: 3px; margin: 0 0 20px; font-weight: 600;">Votre code de réinitialisation</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" align="center" style="margin: 0 auto; border-collapse: collapse; table-layout: fixed;">
                          <tr>
                            %s
                          </tr>
                        </table>
                        <p style="color: #666688; font-size: 12px; margin: 20px 0 0;">Ce code expire dans <strong style="color: #F5A623;">15 minutes</strong></p>
                      </div>

                      <!-- DIVIDER -->
                      <div style="height: 1px; background: linear-gradient(to right, transparent, rgba(245,166,35,0.4), transparent); margin: 40px 0;"></div>

                      <!-- SECURITY NOTE -->
                      <table cellpadding="0" cellspacing="0" style="background: rgba(245,166,35,0.08); border: 1px solid rgba(245,166,35,0.2); border-radius: 12px; padding: 20px; width: 100%%;">
                        <tr>
                          <td width="40" valign="top">
                            <div style="width: 32px; height: 32px; background: rgba(245,166,35,0.2); border-radius: 8px; text-align: center; line-height: 32px; font-size: 16px;">🔑</div>
                          </td>
                          <td style="padding-left: 12px;">
                            <p style="color: #F5A623; font-size: 12px; font-weight: 700; margin: 0 0 4px; text-transform: uppercase; letter-spacing: 1px;">Attention</p>
                            <p style="color: #8888AA; font-size: 13px; margin: 0; line-height: 1.5;">Si vous n'avez pas demandé de réinitialisation de mot de passe, vous pouvez ignorer cet email en toute sécurité. Votre mot de passe actuel restera inchangé.</p>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- FOOTER -->
                  <tr>
                    <td style="padding: 32px 48px; background: #08081A; border-top: 1px solid rgba(255,255,255,0.05); text-align: center;">
                      <p style="color: #333355; font-size: 11px; margin: 0;">
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
}
