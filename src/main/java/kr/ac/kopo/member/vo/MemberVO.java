package kr.ac.kopo.member.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MemberVO {

    private Integer memberId;
    private String loginId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;
    private String skinType;
    private String skinConcern;
    private String regDate;
    private String role;

    public MemberVO() {}

    public Integer getMemberId()         { return memberId; }
    public void setMemberId(Integer v)   { this.memberId = v; }

    public String getLoginId()           { return loginId; }
    public void setLoginId(String v)     { this.loginId = v; }

    public String getPassword()          { return password; }
    public void setPassword(String v)    { this.password = v; }

    public String getNickname()          { return nickname; }
    public void setNickname(String v)    { this.nickname = v; }

    public String getSkinType()          { return skinType; }
    public void setSkinType(String v)    { this.skinType = v; }

    public String getSkinConcern()       { return skinConcern; }
    public void setSkinConcern(String v) { this.skinConcern = v; }

    public String getRegDate()           { return regDate; }
    public void setRegDate(String v)     { this.regDate = v; }

    public String getRole()              { return role; }
    public void setRole(String v)        { this.role = v; }

    @Override
    public String toString() {
        return "MemberVO{memberId=" + memberId + ", loginId=" + loginId
                + ", nickname=" + nickname + ", skinType=" + skinType + "}";
    }
}
