// RegistrationService.java
@Service
@Slf4j
public class RegistrationService {
    @Autowired
    private RegistrationRequestRepository registrationRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public RegistrationResponseDTO createRegistrationRequest(RegistrationRequestDTO dto) {
        // Kiểm tra trùng lặp
        if (userRepository.existsByStudentId(dto.getStudentId())) {
            throw new DuplicateResourceException("Student ID already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Tạo yêu cầu đăng ký mới
        RegistrationRequest request = new RegistrationRequest();
        request.setStudentId(dto.getStudentId());
        request.setFullName(dto.getFullName());
        request.setEmail(dto.getEmail());
        request.setStatus(RegistrationStatus.PENDING);
        
        request = registrationRequestRepository.save(request);
        
        return mapToResponseDTO(request);
    }
    
    @Transactional
    public void approveRegistration(Long requestId, RegistrationApprovalDTO dto, User librarian) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));
            
        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new InvalidOperationException("Request is already processed");
        }
        
        // Tạo user mới
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStudentId(request.getStudentId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        
        // Thêm role USER
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        user.getRoles().add(userRole);
        
        userRepository.save(user);
        
        // Cập nhật trạng thái yêu cầu
        request.setStatus(RegistrationStatus.APPROVED);
        request.setApprovedBy(librarian);
        request.setApprovedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);
        
        // Gửi email thông báo
        emailService.sendRegistrationApprovalEmail(
            request.getEmail(),
            dto.getUsername(),
            dto.getPassword()
        );
    }
    
    @Transactional
    public void rejectRegistration(Long requestId, User librarian) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));
            
        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new InvalidOperationException("Request is already processed");
        }
        
        request.setStatus(RegistrationStatus.REJECTED);
        request.setApprovedBy(librarian);
        request.setApprovedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);
        
        // Gửi email thông báo
        emailService.sendRegistrationRejectionEmail(request.getEmail());
    }
    
    public Page<RegistrationResponseDTO> getPendingRegistrations(Pageable pageable) {
        return registrationRequestRepository.findByStatus(RegistrationStatus.PENDING, pageable)
            .map(this::mapToResponseDTO);
    }
    
    private RegistrationResponseDTO mapToResponseDTO(RegistrationRequest request) {
        return new RegistrationResponseDTO(
            request.getId(),
            request.getStudentId(),
            request.getFullName(),
            request.getEmail(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}

// RegistrationController.java
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponseDTO createRequest(@Valid @RequestBody RegistrationRequestDTO dto) {
        return registrationService.createRegistrationRequest(dto);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public Page<RegistrationResponseDTO> getPendingRequests(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return registrationService.getPendingRegistrations(pageable);
    }
    
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void approveRequest(
        @PathVariable Long requestId,
        @Valid @RequestBody RegistrationApprovalDTO dto,
        @AuthenticationPrincipal User librarian
    ) {
        registrationService.approveRegistration(requestId, dto, librarian);
    }
    
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void rejectRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal User librarian
    ) {
        registrationService.rejectRegistration(requestId, librarian);
    }
}

// EmailService.java
@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendRegistrationApprovalEmail(String to, String username, String password) {
        String subject = "Library Registration Approved";
        String content = String.format("""
            Dear User,
            
            Your library registration request has been approved.
            Here are your login credentials:
            
            Username: %s
            Password: %s
            
            Please change your password after first login.
            
            Best regards,
            Library Team
            """, username, password);
            
        sendEmail(to, subject, content);
    }
    
    public void sendRegistrationRejectionEmail(String to) {
        String subject = "Library Registration Rejected";
        String content = """
            Dear User,
            
            Your library registration request has been rejected.
            Please contact the library staff for more information.
            
            Best regards,
            Library Team
            """;
            
        sendEmail(to, subject, content);
    }
    
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
