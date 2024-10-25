// RegistrationRequestDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDTO {
    @NotBlank
    private String studentId;
    
    @NotBlank
    private String fullName;
    
    @NotBlank
    @Email
    private String email;
}

// RegistrationApprovalDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationApprovalDTO {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}

// RegistrationResponseDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDTO {
    private Long id;
    private String studentId;
    private String fullName;
    private String email;
    private RegistrationStatus status;
    private LocalDateTime createdAt;
}
