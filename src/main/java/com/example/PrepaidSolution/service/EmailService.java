package com.example.PrepaidSolution.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@Service
public class EmailService {

    private static final String OTP_LOGO_CONTENT_ID = "ariotLogo";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String COMPANY_NAME = "YourCompany"; // Replace with your company name
    private static final String SOFTWARE_NAME = "YourSoftware"; // Replace with your software name
    private static final String SUPPORT_EMAIL = "support@yourcompany.com"; // Replace with your support email
    private static final String COMPANY_WEBSITE = "https://www.yourcompany.com"; // Replace with your website

    public void sendCredentialsEmail(Map<String,String> ownerDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(ownerDetails.get("email"));
            helper.setSubject("Welcome to " + SOFTWARE_NAME + " - Your Account Credentials");
            helper.setFrom(fromEmail);

            // Create email content
            String htmlContent = createEmailContent(ownerDetails.get("name"), ownerDetails.get("username"), ownerDetails.get("password"));
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send credentials email", e);
        }
    }

    private String createEmailContent(String fullName, String username, String password) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>AR IoT Solutions Credentials</title>
                </head>
                <body style="margin:0;padding:0;background:#eef2f7;font-family:Arial,sans-serif;color:#1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#eef2f7;padding:24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:22px;overflow:hidden;box-shadow:0 16px 40px rgba(15,23,42,0.10);">
                                    <tr>
                                        <td style="background:linear-gradient(135deg,#2e3192 0%%,#1f225f 55%%,#f37021 100%%);padding:28px 32px 24px;text-align:center;">
                                            <div style="font-size:26px;line-height:1.2;font-weight:800;color:#ffffff;letter-spacing:0.4px;">AR IoT Solutions</div>
                                            <div style="margin-top:8px;font-size:14px;line-height:1.5;color:rgba(255,255,255,0.88);">Pay As You Go access details</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <div style="font-size:16px;line-height:1.8;color:#475569;">
                                                Hello <strong style="color:#1e293b;">%s</strong>,
                                            </div>
                                            <div style="margin-top:12px;font-size:16px;line-height:1.8;color:#475569;">
                                                Your account has been created successfully. Use the credentials below to sign in to the AR IoT Solutions platform.
                                            </div>

                                            <div style="margin:28px 0 22px;padding:24px;border-radius:18px;background:#f8fafc;border:1px solid #dbe5f0;">
                                                <div style="font-size:13px;font-weight:700;letter-spacing:1.8px;text-transform:uppercase;color:#64748b;margin-bottom:16px;">Login Credentials</div>

                                                <div style="padding:14px 16px;border-radius:14px;background:#ffffff;border:1px solid #e2e8f0;margin-bottom:12px;">
                                                    <div style="font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:1.2px;color:#64748b;">Username</div>
                                                    <div style="margin-top:8px;font-size:18px;font-weight:700;color:#2e3192;word-break:break-word;">%s</div>
                                                </div>

                                                <div style="padding:14px 16px;border-radius:14px;background:#ffffff;border:1px solid #e2e8f0;">
                                                    <div style="font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:1.2px;color:#64748b;">Password</div>
                                                    <div style="margin-top:8px;font-size:18px;font-weight:700;color:#f37021;word-break:break-word;">%s</div>
                                                </div>
                                            </div>

                                            <div style="padding:16px 18px;border-left:4px solid #f37021;background:#fff7ed;border-radius:12px;font-size:14px;line-height:1.7;color:#7c2d12;">
                                                For security, please change your password after your first login and do not share your credentials with anyone.
                                            </div>

                                            <div style="margin-top:22px;padding:16px 18px;border-radius:12px;background:#eff6ff;font-size:14px;line-height:1.7;color:#1d4ed8;">
                                                Keep this email safe in case you need to refer to your initial login details later.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 32px 28px;">
                                            <div style="height:1px;background:#e2e8f0;"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 32px 30px;text-align:center;font-size:12px;line-height:1.7;color:#64748b;">
                                            Powered by <span style="font-weight:700;color:#2e3192;">AR IoT Solutions</span><br>
                                            This is an automated email. Please do not reply.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(fullName, username, password);
    }

    public void sendOTP(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("AR IoT Solutions - Your Login OTP");
            helper.setFrom(fromEmail);

            String htmlContent = createOtpEmailContent(otp);
            helper.setText(htmlContent, true);
            helper.addInline(OTP_LOGO_CONTENT_ID, new ClassPathResource("static/images/ariot_logo.jpeg"));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String createOtpEmailContent(String otp) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>AR IoT Solutions OTP</title>
                </head>
                <body style="margin:0;padding:0;background:#eef2f7;font-family:Arial,sans-serif;color:#1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#eef2f7;padding:24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:22px;overflow:hidden;box-shadow:0 16px 40px rgba(15,23,42,0.10);">
                                    <tr>
                                        <td style="background:linear-gradient(135deg,#2e3192 0%%,#1f225f 55%%,#f37021 100%%);padding:28px 32px 24px;text-align:center;">
                                            <img src="cid:%s" alt="AR IoT Solutions" style="width:170px;max-width:100%%;display:block;margin:0 auto 18px;">
                                            <div style="font-size:26px;line-height:1.2;font-weight:800;color:#ffffff;letter-spacing:0.4px;">AR IoT Solutions</div>
                                            <div style="margin-top:8px;font-size:14px;line-height:1.5;color:rgba(255,255,255,0.88);">Secure Login Verification</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <div style="font-size:16px;line-height:1.7;color:#475569;">
                                                An OTP has been generated for your login request. Use the code below to continue signing in.
                                            </div>

                                            <div style="margin:28px 0 22px;padding:22px 20px;border-radius:18px;background:#f8fafc;border:1px solid #dbe5f0;text-align:center;">
                                                <div style="font-size:13px;font-weight:700;letter-spacing:1.8px;text-transform:uppercase;color:#64748b;">Your One-Time Password</div>
                                                <div style="margin-top:14px;font-size:34px;font-weight:800;letter-spacing:8px;color:#2e3192;">%s</div>
                                                <div style="margin-top:14px;font-size:13px;color:#64748b;">Valid for 5 minutes only</div>
                                            </div>

                                            <div style="padding:16px 18px;border-left:4px solid #f37021;background:#fff7ed;border-radius:12px;font-size:14px;line-height:1.7;color:#7c2d12;">
                                                If you did not request this OTP, you can safely ignore this email. For your security, do not share this code with anyone.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 32px 28px;">
                                            <div style="height:1px;background:#e2e8f0;"></div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 32px 30px;text-align:center;font-size:12px;line-height:1.7;color:#64748b;">
                                            Powered by <span style="font-weight:700;color:#2e3192;">AR IoT Solutions</span><br>
                                            This is an automated verification email. Please do not reply.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(OTP_LOGO_CONTENT_ID, otp);
    }
}
