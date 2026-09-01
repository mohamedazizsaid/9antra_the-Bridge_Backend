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
          <html>
          <body style="background-color:#08081A; color:#FFFFFF; font-family:Arial,sans-serif; padding:20px;">
            <div style="max-width:600px; margin:0 auto; background-color:#0F0F2E; border:1px solid #C62761; border-radius:12px; padding:30px;">
              <h2 style="color:#F5A623;">Rappel de Cours</h2>
              <p>Bonjour %s,</p>
              <p>%s</p>
              <p>Date : %s à %s</p>
              <hr style="border-color:#C62761;"/>
              <p style="font-size:11px; color:#8888AA;">L'équipe The Bridge — 9antra</p>
            </div>
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
          <html>
          <body style="background-color:#08081A; color:#FFFFFF; font-family:Arial,sans-serif; padding:20px;">
            <div style="max-width:600px; margin:0 auto; background-color:#0F0F2E; border:1px solid #F5A623; border-radius:12px; padding:30px;">
              <h2 style="color:#F5A623; text-align:center;">⚠️ Échéance de paiement</h2>
              <p>Bonjour %s,</p>
              <p>Ceci est un rappel concernant le paiement de votre formation pour la phase : <strong>%s</strong>.</p>
              <div style="background-color:#1A1A3E; border-left:4px solid #C62761; padding:15px; margin:20px 0; border-radius:4px;">
                <p style="margin:5px 0;"><strong>Montant :</strong> %s TND</p>
                <p style="margin:5px 0;"><strong>Date d'échéance :</strong> %s</p>
                <p style="margin:5px 0; color:#F5A623;"><strong>Temps restant :</strong> %s jours</p>
              </div>
              <p>Veuillez régulariser votre paiement depuis votre espace étudiant afin de continuer à accéder à vos cours en toute sérénité.</p>
              <hr style="border-color:#C62761; margin-top:20px;"/>
              <p style="font-size:11px; color:#8888AA; text-align:center;">L'équipe The Bridge — 9antra</p>
            </div>
          </body>
          </html>
          """.formatted(firstName, phaseTitle, amount, dueDate.toString(), daysRemaining);

      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send payment email reminder to {}", to, e);
    }
  }

  @org.springframework.scheduling.annotation.Async
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
          <div style="max-width:650px;margin:0 auto;background-color:#08081A;">

            <!-- HERO HEADER -->
            <div style="background:linear-gradient(135deg,#1A0A2E 0%%,#0D0D2B 50%%,#1A0A1A 100%%);padding:48px 40px;text-align:center;border-bottom:3px solid #C62761;">
              <div style="display:inline-block;background:linear-gradient(135deg,#C62761,#F5A623);-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-size:32px;font-weight:900;letter-spacing:-1px;margin-bottom:8px;">
                THE BRIDGE
              </div>
              <div style="color:#A0A0C8;font-size:11px;text-transform:uppercase;letter-spacing:4px;font-weight:600;">9antra · Formation Professionnelle</div>
            </div>

            <!-- GOLD BANNER -->
            <div style="background:linear-gradient(90deg,#C62761,#F5A623,#C62761);padding:14px;text-align:center;">
              <span style="color:#FFFFFF;font-size:14px;font-weight:800;text-transform:uppercase;letter-spacing:3px;">🏅 Certificat Blockchain Délivré</span>
            </div>

            <!-- MAIN CONTENT -->
            <div style="background:#0F0F2E;padding:48px 40px;">

              <!-- Greeting -->
              <p style="color:#A0A0C8;font-size:12px;text-transform:uppercase;letter-spacing:3px;font-weight:700;margin:0 0 8px;">Félicitations</p>
              <h2 style="color:#FFFFFF;font-size:28px;font-weight:800;margin:0 0 24px;">%s %s 🎉</h2>

              <p style="color:#8888AA;font-size:15px;line-height:1.8;margin:0 0 32px;">
                Vous avez réussi avec succès la phase <strong style="color:#F5A623;">"%s"</strong>
                du programme <strong style="color:#C62761;">%s</strong>. Votre certificat a été
                <strong style="color:#FFFFFF;">enregistré sur la blockchain Polygon</strong> et est
                désormais infalsifiable et vérifiable publiquement.
              </p>

              <!-- CERTIFICATE CARD -->
              <div style="background:linear-gradient(135deg,#1A1A3E,#12122E);border:1px solid #C62761;border-radius:16px;padding:32px;margin:0 0 32px;position:relative;">
                <div style="position:absolute;top:16px;right:20px;background:linear-gradient(135deg,#C62761,#F5A623);color:white;font-size:10px;font-weight:800;padding:4px 10px;border-radius:20px;text-transform:uppercase;letter-spacing:1px;">Certifié Blockchain</div>

                <div style="margin-bottom:20px;">
                  <span style="color:#A0A0C8;font-size:10px;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:4px;">Numéro de certificat</span>
                  <span style="color:#F5A623;font-size:14px;font-weight:700;font-family:monospace;">%s</span>
                </div>

                <div style="margin-bottom:20px;">
                  <span style="color:#A0A0C8;font-size:10px;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:4px;">Date d'émission</span>
                  <span style="color:#FFFFFF;font-size:14px;font-weight:600;">%s</span>
                </div>

                <div style="background:#08081A;border-radius:8px;padding:12px;margin-top:16px;">
                  <span style="color:#A0A0C8;font-size:9px;text-transform:uppercase;letter-spacing:2px;display:block;margin-bottom:6px;">Transaction Blockchain (Polygon)</span>
                  <span style="color:#C62761;font-size:9px;font-family:monospace;word-break:break-all;">%s</span>
                </div>
              </div>

              <!-- HOW TO VERIFY -->
              <div style="background:#1A1A2E;border-left:4px solid #F5A623;padding:20px;border-radius:4px;margin:0 0 32px;">
                <p style="color:#F5A623;font-size:13px;font-weight:700;margin:0 0 8px;">🔍 Comment vérifier votre certificat ?</p>
                <p style="color:#8888AA;font-size:13px;margin:0;line-height:1.7;">
                  Rendez-vous sur <strong style="color:#FFFFFF;">the-bridge.9antra.tn/verify</strong> et entrez votre numéro de certificat.
                  Votre certificat sera vérifié en temps réel via la blockchain.
                </p>
              </div>

              <p style="color:#8888AA;font-size:14px;line-height:1.7;margin:0;">
                Vous pouvez télécharger votre certificat PDF depuis votre <strong style="color:#C62761;">espace stagiaire</strong> à tout moment. Ce document sera reconnu par les employeurs et les organismes de certification.
              </p>
            </div>

            <!-- FOOTER -->
            <div style="background:#060618;padding:32px 40px;border-top:1px solid #1A1A3E;text-align:center;">
              <p style="color:#444466;font-size:11px;margin:0 0 8px;">© 2025 The Bridge — 9antra · Formation Professionnelle en Tunisie</p>
              <p style="color:#333355;font-size:10px;margin:0;">Ce message est envoyé automatiquement. Ne pas répondre à cet email.</p>
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
            <div style="max-width:600px;margin:0 auto;background:#0F0F2E;border-radius:16px;overflow:hidden;border:1px solid rgba(198,39,97,0.3);">
              <div style="background:linear-gradient(135deg,#C62761,#8B1A44,#F5A623);padding:40px;text-align:center;">
                <h1 style="color:white;margin:0;font-size:26px;font-weight:800;">The Bridge — 9antra</h1>
                <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:12px;letter-spacing:3px;text-transform:uppercase;">Espace Formateur</p>
              </div>
              <div style="padding:40px;">
                <h2 style="color:#FFFFFF;font-size:22px;margin:0 0 16px;">Bienvenue %s %s 👋</h2>
                <p style="color:#8888AA;font-size:14px;line-height:1.8;margin:0 0 24px;">
                  Un compte formateur a été créé pour vous sur <strong style="color:#C62761;">The Bridge</strong>.
                  Vous pouvez accéder à la plateforme avec les identifiants ci-dessous.
                </p>
                <div style="background:#1A1A3E;border:1px solid rgba(198,39,97,0.3);border-radius:12px;padding:24px;margin:0 0 24px;">
                  <p style="color:#A0A0C8;font-size:11px;text-transform:uppercase;letter-spacing:2px;margin:0 0 8px;">Email</p>
                  <p style="color:#FFFFFF;font-size:16px;font-weight:700;font-family:monospace;margin:0 0 16px;">%s</p>
                  <p style="color:#A0A0C8;font-size:11px;text-transform:uppercase;letter-spacing:2px;margin:0 0 8px;">Mot de passe temporaire</p>
                  <p style="color:#F5A623;font-size:16px;font-weight:700;font-family:monospace;margin:0;">%s</p>
                </div>
                <div style="background:rgba(245,166,35,0.08);border-left:4px solid #F5A623;padding:16px;border-radius:4px;">
                  <p style="color:#F5A623;font-size:12px;font-weight:700;margin:0 0 4px;">⚠️ Important</p>
                  <p style="color:#8888AA;font-size:13px;margin:0;">Vous serez invité à changer votre mot de passe lors de votre première connexion.</p>
                </div>
              </div>
              <div style="background:#060618;padding:24px;text-align:center;border-top:1px solid #1A1A3E;">
                <p style="color:#444466;font-size:11px;margin:0;">© 2026 9antra • The Bridge</p>
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
              <td style="padding:10px 16px;border-bottom:1px solid #1E1E40;color:#C8C8E8;font-size:13px;">%s</td>
            </tr>
            """.formatted(title));
      }

      String html = """
          <!DOCTYPE html>
          <html lang="fr">
          <head><meta charset="UTF-8"><title>Parcours Confirmé</title></head>
          <body style="margin:0;padding:0;background:#08081A;font-family:'Inter',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
              <tr><td align="center" style="padding:40px 20px;">
                <table width="600" cellpadding="0" cellspacing="0"
                       style="background:linear-gradient(160deg,#12122A,#0D0D22);border-radius:20px;
                              border:1px solid #2A2A4A;overflow:hidden;">
                  <!-- Header -->
                  <tr><td style="background:linear-gradient(90deg,#C62761,#F5A623);padding:4px;"></td></tr>
                  <tr><td style="padding:36px 40px 20px;">
                    <div style="font-size:28px;margin-bottom:6px;">🎉</div>
                    <h1 style="margin:0;color:#FFFFFF;font-size:22px;font-weight:800;">
                      Parcours activé, %s !
                    </h1>
                    <p style="color:#8888AA;font-size:13px;margin:8px 0 0;">
                      Votre parcours personnalisé a été confirmé et vos inscriptions sont actives.
                    </p>
                  </td></tr>
                  <!-- Receipt ref -->
                  <tr><td style="padding:0 40px 24px;">
                    <div style="background:#1A1A35;border:1px solid #C62761;border-radius:12px;
                                padding:14px 20px;display:flex;align-items:center;gap:12px;">
                      <span style="font-size:11px;color:#8888AA;text-transform:uppercase;
                                   letter-spacing:1px;display:block;margin-bottom:4px;">Référence reçu</span>
                      <span style="font-size:18px;font-weight:800;color:#F5A623;
                                   font-family:'Courier New',monospace;">%s</span>
                    </div>
                  </td></tr>
                  <!-- Formations -->
                  <tr><td style="padding:0 40px 16px;">
                    <p style="color:#8888AA;font-size:11px;text-transform:uppercase;
                               letter-spacing:1px;margin:0 0 8px;">Formations incluses</p>
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#111130;border:1px solid #2A2A4A;border-radius:12px;overflow:hidden;">
                      %s
                    </table>
                  </td></tr>
                  <!-- Pricing -->
                  <tr><td style="padding:0 40px 32px;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#111130;border:1px solid #2A2A4A;border-radius:12px;padding:16px;">
                      <tr>
                        <td style="padding:8px 16px;color:#8888AA;font-size:13px;">Sous-total</td>
                        <td style="padding:8px 16px;text-align:right;color:#C8C8E8;font-size:13px;">%.2f TND</td>
                      </tr>
                      <tr>
                        <td style="padding:8px 16px;color:#F5A623;font-size:13px;font-weight:700;">
                          Remise combo (%.0f%%)
                        </td>
                        <td style="padding:8px 16px;text-align:right;color:#F5A623;font-size:13px;font-weight:700;">
                          -%.2f TND
                        </td>
                      </tr>
                      <tr style="border-top:1px solid #2A2A4A;">
                        <td style="padding:14px 16px;color:#FFFFFF;font-size:16px;font-weight:800;">Total payé</td>
                        <td style="padding:14px 16px;text-align:right;color:#C62761;
                                   font-size:18px;font-weight:800;font-family:'Courier New',monospace;">
                          %.2f TND
                        </td>
                      </tr>
                    </table>
                  </td></tr>
                  <!-- CTA -->
                  <tr><td style="padding:0 40px 40px;text-align:center;">
                    <a href="http://localhost:4200/dashboard/stagiaire/overview"
                       style="display:inline-block;background:linear-gradient(90deg,#C62761,#F5A623);
                              color:#FFFFFF;text-decoration:none;padding:14px 32px;border-radius:12px;
                              font-size:14px;font-weight:800;">
                      Accéder à mon espace →
                    </a>
                  </td></tr>
                  <!-- Footer -->
                  <tr><td style="padding:20px 40px;border-top:1px solid #1E1E40;text-align:center;">
                    <p style="color:#555577;font-size:11px;margin:0;">© 2026 The Bridge — 9antra</p>
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

