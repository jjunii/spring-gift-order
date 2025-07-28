package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = true, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_type", nullable = false, length = 20)
    private SignupType signupType;

    @Column(name = "kakao_access_token", nullable = true)
    private String kakaoAccessToken;

    @Column(name = "kakao_refresh_token", nullable = true)
    private String kakaoRefreshToken;

    protected Member() {
    }

    public Member(Long id, String email, String password, MemberRole role, SignupType signupType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.signupType = signupType;
    }

    public static Member createLocalMember(String email, String password, MemberRole role) {
        return new Member(null, email, password, role, SignupType.LOCAL);
    }

    public static Member createKakaoMember(String email, MemberRole role) {
        return new Member(null, email, null, role, SignupType.KAKAO);
    }

    public void updateKakaoTokens(String kakaoAccessToken, String kakaoRefreshToken) {
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    public SignupType getSignupType() {
        return signupType;
    }
}
