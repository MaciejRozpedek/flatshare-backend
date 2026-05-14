package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

public class EmailTemplateWrapper {
    public static String wrapInTemplate(String content) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    background-color: #f4f7f6;
                    margin: 0;
                    padding: 40px 0;
                    color: #333333;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: #ffffff;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.05);
                }
                .header {
                    background: linear-gradient(135deg, #4f46e5, #6366f1);
                    color: #ffffff;
                    padding: 35px 40px;
                    text-align: center;
                }
                .header h1 {
                    margin: 0;
                    font-size: 32px;
                    font-weight: 800;
                    letter-spacing: -1px;
                }
                .content {
                    padding: 40px;
                    line-height: 1.7;
                    font-size: 16px;
                }
                .content h2 {
                    color: #111827;
                    font-size: 24px;
                    margin-top: 0;
                    margin-bottom: 20px;
                    font-weight: 700;
                }
                .content p {
                    margin: 0 0 15px 0;
                    color: #4b5563;
                }
                .footer {
                    background-color: #f9fafb;
                    padding: 24px 40px;
                    text-align: center;
                    font-size: 13px;
                    color: #9ca3af;
                    border-top: 1px solid #f3f4f6;
                }
                .btn {
                    display: inline-block;
                    background-color: #4f46e5;
                    color: #ffffff !important;
                    text-decoration: none;
                    padding: 14px 32px;
                    border-radius: 8px;
                    font-weight: 600;
                    margin-top: 10px;
                    text-align: center;
                }
                .highlight-box {
                    background-color: #f3f4f6;
                    border-left: 4px solid #4f46e5;
                    padding: 16px 20px;
                    margin: 24px 0;
                    border-radius: 0 8px 8px 0;
                    font-size: 18px;
                    font-weight: 700;
                    color: #111827;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>FlatShare</h1>
                </div>
                <div class="content">
                    %s
                </div>
                <div class="footer">
                    &copy; 2026 FlatShare Team. Wszelkie prawa zastrzeżone.<br>
                    Wiadomość została wygenerowana automatycznie, prosimy na nią nie odpowiadać.
                </div>
            </div>
        </body>
        </html>
        """.formatted(content);
    }
}
