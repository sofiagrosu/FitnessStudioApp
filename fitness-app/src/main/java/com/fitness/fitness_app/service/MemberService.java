package com.fitness.fitness_app.service;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.exception.ValidationException;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private final MemberRepository membersRepository;

    public MemberService(MemberRepository membersRepository) {
        this.membersRepository = membersRepository;
    }

    public Member registerMember(Member member) {
        validateMember(member);

        if (membersRepository.findByEmailIgnoreCase(member.getEmail()) != null) {
            throw new ConflictException("A member with this email already exists");
        }

        if (member.getQrCode() == null || member.getQrCode().isBlank()) {
            member.setQrCode(generateUniqueQrCode());
        }

        return membersRepository.save(member);
    }

    public Member updateMember(Long memberId, Member updatedMember) {
        Member existingMember = getMemberById(memberId);

        if (updatedMember == null) {
            throw new ValidationException("Member data is required");
        }

        if (updatedMember.getFirstName() == null || updatedMember.getFirstName().isBlank()) {
            throw new ValidationException("First name is required");
        }

        if (updatedMember.getLastName() == null || updatedMember.getLastName().isBlank()) {
            throw new ValidationException("Last name is required");
        }

        if (updatedMember.getEmail() == null || updatedMember.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }

        validateEmail(updatedMember.getEmail());

        if (!updatedMember.getEmail().equalsIgnoreCase(existingMember.getEmail())) {
            if (membersRepository.findByEmailIgnoreCase(updatedMember.getEmail()) != null) {
                throw new ConflictException("Email already used by another member");
            }
        }

        validatePhone(updatedMember.getPhone());

        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setPhone(updatedMember.getPhone());

        return membersRepository.save(existingMember);
    }

    public Member changePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = getMemberById(memberId);

        if (member.getPassword() == null || !member.getPassword().equals(oldPassword)) {
            throw new ValidationException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password is required");
        }

        member.setPassword(newPassword);
        return membersRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return membersRepository.findAll();
    }

    public Member getMemberById(Long memberId) {
        if (memberId == null) {
            throw new ValidationException("Member id is required");
        }

        return membersRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
    }

    public Member findByEmail(String email) {
        return membersRepository.findByEmailIgnoreCase(email);
    }

    public Member findByQrCode(String qrCode) {
        Member member = membersRepository.findByQrCode(qrCode);

        if (member == null) {
            throw new NotFoundException("No member found for this QR code");
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
        if (member == null) {
            throw new ValidationException("Member data is required");
        }

        if (member.getFirstName() == null || member.getFirstName().isBlank()) {
            throw new ValidationException("First name is required");
        }

        if (member.getLastName() == null || member.getLastName().isBlank()) {
            throw new ValidationException("Last name is required");
        }

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }

        validateEmail(member.getEmail());

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }

        validatePhone(member.getPhone());
    }

    private void validateEmail(String email) {
        if (email != null && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    private void validatePhone(String phone) {
        if (phone != null && !phone.isBlank() && !phone.matches("\\d{10}")) {
            throw new ValidationException("Phone number must be exactly 10 digits");
        }
    }
}