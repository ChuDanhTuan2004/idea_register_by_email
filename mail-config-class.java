@Configuration
public class MailConfiguration {
    
    @Value("${mail.from}")
    private String from;
    
    @Value("${mail.personal}")
    private String personal;
    
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        
        mailSender.setUsername("your-email@gmail.com");
        mailSender.setPassword("your-app-password");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Enabled for debugging, disable in production
        
        return mailSender;
    }
    
    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        return message;
    }
    
    // For HTML emails
    @Bean
    public MimeMessageHelper mimeMessageHelper(JavaMailSender mailSender) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        return new MimeMessageHelper(mimeMessage, true, "UTF-8");
    }
}
