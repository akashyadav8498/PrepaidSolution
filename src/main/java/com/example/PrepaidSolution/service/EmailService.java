package com.example.PrepaidSolution.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

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
            helper.setFrom("noreply@yourcompany.com"); // Replace with your sender email

            // Create email content
            String htmlContent = createEmailContent(ownerDetails.get("name"), ownerDetails.get("username"), ownerDetails.get("password"));
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send credentials email", e);
        }
    }

    private String createEmailContent(String fullName, String username, String password) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Welcome to " + SOFTWARE_NAME + "</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #2c3e50; color: white; padding: 30px 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                "        .content { background-color: #f8f9fa; padding: 30px 20px; border-radius: 0 0 8px 8px; }" +
                "        .credentials-box { background-color: white; border: 2px solid #3498db; border-radius: 8px; padding: 20px; margin: 20px 0; }" +
                "        .credential-item { margin: 15px 0; padding: 10px; background-color: #f1f2f6; border-radius: 4px; }" +
                "        .credential-label { font-weight: bold; color: #2c3e50; }" +
                "        .credential-value { font-family: monospace; font-size: 16px; color: #e74c3c; background-color: white; padding: 5px 8px; border-radius: 3px; border: 1px solid #ddd; }" +
                "        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin: 20px 0; }" +
                "        .footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-size: 12px; }" +
                "        .button { display: inline-block; padding: 12px 24px; background-color: #3498db; color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }" +
                "        .security-tips { background-color: #e8f8f5; border-left: 4px solid #27ae60; padding: 15px; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='header'>" +
                "        <h1>" + COMPANY_NAME + "</h1>" +
                "        <h2>Welcome to " + SOFTWARE_NAME + "!</h2>" +
                "    </div>" +
                "    <div class='content'>" +
                "        <h3>Hello " + fullName + ",</h3>" +
                "        <p>Welcome to " + SOFTWARE_NAME + "! Your account has been successfully created. Below are your login credentials:</p>" +
                "        " +
                "        <div class='credentials-box'>" +
                "            <h4>🔐 Your Login Credentials</h4>" +
                "            <div class='credential-item'>" +
                "                <div class='credential-label'>Username:</div>" +
                "                <div class='credential-value'>" + username + "</div>" +
                "            </div>" +
                "            <div class='credential-item'>" +
                "                <div class='credential-label'>Password:</div>" +
                "                <div class='credential-value'>" + password + "</div>" +
                "            </div>" +
                "        </div>" +
                "        " +
                "        <div class='warning'>" +
                "            <strong>⚠️ Important Security Notice:</strong><br>" +
                "            Please change your password after your first login for security purposes." +
                "        </div>" +
                "        " +
                "        <div style='text-align: center;'>" +
                "            <a href='" + COMPANY_WEBSITE + "/login' class='button'>Login Now</a>" +
                "        </div>" +
                "        " +
                "        <div class='security-tips'>" +
                "            <h4>🛡️ Security Best Practices:</h4>" +
                "            <ul>" +
                "                <li>Keep your credentials confidential</li>" +
                "                <li>Use a strong, unique password</li>" +
                "                <li>Enable two-factor authentication if available</li>" +
                "                <li>Log out after each session</li>" +
                "            </ul>" +
                "        </div>" +
                "        " +
                "        <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>" +
                "        " +
                "        <p>Best regards,<br>" +
                "        The " + COMPANY_NAME + " Team</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "        <p>© 2024 " + COMPANY_NAME + ". All rights reserved.</p>" +
                "        <p>Contact us: <a href='mailto:" + SUPPORT_EMAIL + "'>" + SUPPORT_EMAIL + "</a> | " +
                "        Visit: <a href='" + COMPANY_WEBSITE + "'>" + COMPANY_WEBSITE + "</a></p>" +
                "        <p><em>This is an automated message. Please do not reply to this email.</em></p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}

