package com.fitness.fitness_app.controller;

import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@CrossOrigin(origins = "http://localhost:3000")
public class MembersController {
    private final MemberService memberService;

    public MembersController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.getMemberById(memberId));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<Member> updateMember(@PathVariable Long memberId, @RequestBody Member member) {
        return ResponseEntity.ok(memberService.updateMember(memberId, member));
    }

    @PatchMapping("/{memberId}/password")
    public ResponseEntity<String> changePassword(@PathVariable Long memberId,
                                                  @RequestBody ChangePasswordRequest request) {
        memberService.changePassword(memberId, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Member> findByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(memberService.findByQrCode(qrCode));
    }

    public record ChangePasswordRequest(String oldPassword, String newPassword) {}
}
