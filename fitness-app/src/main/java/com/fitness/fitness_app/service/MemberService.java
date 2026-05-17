package com.fitness.fitness_app.service;

import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.repository.MembersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private final MembersRepository membersRepository;

    public MemberService(MembersRepository membersRepository) {
        this.membersRepository = membersRepository;
    }

    public Member registerMember(Member member) {
        validateMember(member);
        if (membersRepository.findByEmail(member.getEmail()) != null) {
            throw new RuntimeException("A member with this email already exists");
        }
        if (member.getQrCode() == null || member.getQrCode().isBlank()) {
            member.setQrCode(generateUniqueQrCode());
        }
        member.setActive(true);
        membersRepository.add(member);
        return member;
    }

    public Member updateMember(Long memberId, Member updatedMember) {
        Member existingMember = getMemberById(memberId);
        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setPhone(updatedMember.getPhone());
        existingMember.setActive(updatedMember.isActive());
        membersRepository.update(existingMember);
        return existingMember;
    }

    public void deactivateMember(Long memberId) {
        membersRepository.delete(memberId);
    }

    public List<Member> getAllMembers() {
        return membersRepository.getAll();
    }

    public Member getMemberById(Long memberId) {
        Member member = membersRepository.findById(memberId);
        if (member == null) {
            throw new RuntimeException("Member not found");
        }
        return member;
    }

    public Member findByQrCode(String qrCode) {
        Member member = membersRepository.findByQrCode(qrCode);
        if (member == null) {
            throw new RuntimeException("No member found for this QR code");
        }
        return member;
    }

    public String generateUniqueQrCode() {
        String qrCode;
        do {
            qrCode = "MEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (membersRepository.findByQrCode(qrCode) != null);
        return qrCode;
    }

    private void validateMember(Member member) {
        if (member == null) throw new RuntimeException("Member data is required");
        if (member.getFirstName() == null || member.getFirstName().isBlank()) throw new RuntimeException("First name is required");
        if (member.getLastName() == null || member.getLastName().isBlank()) throw new RuntimeException("Last name is required");
        if (member.getEmail() == null || member.getEmail().isBlank()) throw new RuntimeException("Email is required");
    }
}
