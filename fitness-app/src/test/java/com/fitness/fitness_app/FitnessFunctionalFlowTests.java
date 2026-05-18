package com.fitness.fitness_app;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.model.CheckIn;
import com.fitness.fitness_app.model.CheckInResult;
import com.fitness.fitness_app.model.Course;
import com.fitness.fitness_app.model.CourseType;
import com.fitness.fitness_app.model.DayOfWeek;
import com.fitness.fitness_app.model.Member;
import com.fitness.fitness_app.model.Payment;
import com.fitness.fitness_app.model.Receipt;
import com.fitness.fitness_app.model.SignUp;
import com.fitness.fitness_app.model.Subscription;
import com.fitness.fitness_app.model.UserI;
import com.fitness.fitness_app.model.enums.PaymentMethod;
import com.fitness.fitness_app.model.enums.Role;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import com.fitness.fitness_app.repository.CoursesRepository;
import com.fitness.fitness_app.repository.SignUpsRepository;
import com.fitness.fitness_app.repository.WaitlistsRepository;
import com.fitness.fitness_app.service.AuthService;
import com.fitness.fitness_app.service.CheckInService;
import com.fitness.fitness_app.service.CoursesService;
import com.fitness.fitness_app.service.MemberService;
import com.fitness.fitness_app.service.PaymentService;
import com.fitness.fitness_app.service.SubscriptionService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FitnessFunctionalFlowTests {

    private static final Path TEST_DATA_DIR = createTestDataDirectory();

    @Autowired private AuthService authService;
    @Autowired private MemberService memberService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private PaymentService paymentService;
    @Autowired private CheckInService checkInService;
    @Autowired private CoursesService coursesService;
    @Autowired private CoursesRepository coursesRepository;
    @Autowired private SignUpsRepository signUpsRepository;
    @Autowired private WaitlistsRepository waitlistsRepository;

    private Long memberId;
    private String memberQrCode;
    private Long subscriptionId;
    private Long courseId;

    @DynamicPropertySource
    static void configureTestDataFiles(DynamicPropertyRegistry registry) {
        prepareTestFiles();
        registry.add("data.users.path", () -> TEST_DATA_DIR.resolve("users.json").toString());
        registry.add("data.locations.path", () -> TEST_DATA_DIR.resolve("locations.json").toString());
        registry.add("data.courses.path", () -> TEST_DATA_DIR.resolve("courses.json").toString());
        registry.add("data.signups.path", () -> TEST_DATA_DIR.resolve("signups.json").toString());
        registry.add("data.waitlist.path", () -> TEST_DATA_DIR.resolve("waitlistEntries.json").toString());
        registry.add("data.members.path", () -> TEST_DATA_DIR.resolve("members.json").toString());
        registry.add("data.subscriptions.path", () -> TEST_DATA_DIR.resolve("subscriptions.json").toString());
        registry.add("data.payments.path", () -> TEST_DATA_DIR.resolve("payments.json").toString());
        registry.add("data.receipts.path", () -> TEST_DATA_DIR.resolve("receipts.json").toString());
        registry.add("data.checkins.path", () -> TEST_DATA_DIR.resolve("checkins.json").toString());
    }

    @Test
    @Order(1)
    void memberCanRegisterLoginChangePasswordAndIsPersistedCorrectly() throws IOException {
        Member member = new Member();
        member.setFirstName("Elena");
        member.setLastName("Marinescu");
        member.setEmail("elena.member@test.com");
        member.setPassword("initial123");
        member.setPhone("0722222222");

        Member registered = authService.register(member);

        assertTrue(registered.getId() > 0);
        assertEquals(Role.MEMBER, registered.getRole());
        assertTrue(registered.isActive());
        assertNotNull(registered.getQrCode());
        assertFalse(registered.getQrCode().isBlank());

        this.memberId = registered.getId();
        this.memberQrCode = registered.getQrCode();

        UserI loggedIn = authService.login("elena.member@test.com", "initial123");
        assertNotNull(loggedIn);
        assertEquals(Role.MEMBER, loggedIn.getRole());

        memberService.changePassword(memberId, "initial123", "newpass123");
        assertNull(authService.login("elena.member@test.com", "initial123"));
        assertNotNull(authService.login("elena.member@test.com", "newpass123"));

        String membersJson = Files.readString(TEST_DATA_DIR.resolve("members.json"));
        assertTrue(membersJson.contains("firstName"));
        assertTrue(membersJson.contains("lastName"));
        assertTrue(membersJson.contains("password"), "Password must be persisted for login after restart/demo reload");
        assertFalse(membersJson.contains("fullName"), "getFullName() must not be persisted to JSON");
    }

    @Test
    @Order(2)
    void subscriptionPaymentReceiptAndQrCheckInFlowWorks() {
        Subscription subscription = subscriptionService.createSubscription(memberId, SubscriptionType.TEN_ENTRIES, 100.0);

        assertNotNull(subscription.getId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(10, subscription.getRemainingEntries());
        this.subscriptionId = subscription.getId();

        assertThrows(ConflictException.class,
                () -> subscriptionService.createSubscription(memberId, SubscriptionType.MONTHLY, 150.0));

        Payment payment = paymentService.registerPayment(memberId, subscriptionId, 100.0, PaymentMethod.CARD);
        assertNotNull(payment.getId());
        assertEquals(memberId, payment.getMemberId());
        assertEquals(subscriptionId, payment.getSubscriptionId());

        Receipt receipt = paymentService.getReceiptForPayment(payment.getId());
        assertNotNull(receipt);
        assertTrue(receipt.getReceiptNumber().startsWith("REC-"));

        CheckInResult result = checkInService.checkInByQrCode(memberQrCode, 1L, 101L);
        assertTrue(result.isAllowed());
        assertEquals("GREEN", result.getScreenColor());
        assertNotNull(result.getCheckIn());

        Subscription afterCheckIn = subscriptionService.getSubscriptionById(subscriptionId);
        assertEquals(9, afterCheckIn.getRemainingEntries());

        CheckInResult duplicate = checkInService.checkInByQrCode(memberQrCode, 1L, 101L);
        assertFalse(duplicate.isAllowed());
        assertEquals("RED", duplicate.getScreenColor());

        CheckIn closed = checkInService.checkOut(result.getCheckIn().getId());
        assertNotNull(closed.getCheckOutTime());

        CheckInResult invalidZone = checkInService.checkInByQrCode(memberQrCode, 1L, 999L);
        assertFalse(invalidZone.isAllowed());
        assertEquals("RED", invalidZone.getScreenColor());
    }

    @Test
    @Order(3)
    void coursesSignUpsWaitlistAttendanceAndValidationWork() {
        Course course = new Course(
                null,
                12L,
                "Morning Yoga",
                CourseType.YOGA,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                60,
                1,
                true,
                99,
                1L
        );

        Course createdCourse = coursesService.createCourse(course);
        assertNotNull(createdCourse);
        assertNotNull(createdCourse.getId());
        assertEquals(0, course.getCurrentOccupancy(), "New course occupancy must be reset to 0");
        this.courseId = course.getId();

        assertThrows(NotFoundException.class, () -> coursesService.createCourse(new Course(
                null, 999L, "Invalid Trainer", CourseType.YOGA, DayOfWeek.TUESDAY,
                LocalTime.of(12, 0), 60, 10, true, 0, 1L)));

        assertThrows(NotFoundException.class, () -> coursesService.createCourse(new Course(
                null, 12L, "Invalid Location", CourseType.YOGA, DayOfWeek.TUESDAY,
                LocalTime.of(12, 0), 60, 10, true, 0, 999L)));

        String signupMessage = coursesService.createSignUp(memberId, courseId);
        assertTrue(signupMessage.contains("successfully"));
        assertEquals(1, coursesRepository.findById(courseId).getCurrentOccupancy());

        SignUp enrolled = signUpsRepository.findByCourseIdAndMemberId(courseId, memberId);
        assertNotNull(enrolled);
        assertNotNull(enrolled.getId(), "SignUpsRepository must assign an id");
        assertEquals("ENROLLED", coursesService.getSignUpStatus(courseId, memberId).status());

        Member secondMember = new Member();
        secondMember.setFirstName("Maria");
        secondMember.setLastName("Ionescu");
        secondMember.setEmail("maria.member@test.com");
        secondMember.setPassword("maria123");
        secondMember.setPhone("0733333333");
        Member registeredSecondMember = authService.register(secondMember);

        String waitlistMessage = coursesService.createSignUp(registeredSecondMember.getId(), courseId);
        assertTrue(waitlistMessage.contains("waitlist"));
        assertEquals("WAITLISTED", coursesService.getSignUpStatus(courseId, registeredSecondMember.getId()).status());
        assertEquals(1, waitlistsRepository.findByCourseId(courseId).size());

        assertTrue(signUpsRepository.findNotAttended().size() >= 1, "Null/false attended values must be treated as not attended");
        coursesService.setMemberAttendance(enrolled.getId(), true);
        assertTrue(signUpsRepository.findAttended().stream().anyMatch(s -> enrolled.getId().equals(s.getId())));

        assertEquals(1, coursesService.countAccumulatedAttendanceForMember(memberId));
    }

    private static Path createTestDataDirectory() {
        try {
            return Files.createTempDirectory("fitness-functional-test-");
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary test directory", e);
        }
    }

    private static void prepareTestFiles() {
        try {
            Files.createDirectories(TEST_DATA_DIR);
            Files.writeString(TEST_DATA_DIR.resolve("users.json"), """
                    [
                      {
                        "type": "ADMIN",
                        "id": 10,
                        "email": "admin@fitness.com",
                        "password": "admin123",
                        "role": "ADMIN",
                        "active": true,
                        "firstName": "Bianca",
                        "lastName": "Manager"
                      },
                      {
                        "type": "TRAINER",
                        "id": 12,
                        "email": "trainer@fitness.com",
                        "password": "trainer123",
                        "role": "TRAINER",
                        "active": true,
                        "firstName": "Ana",
                        "lastName": "Trainer"
                      }
                    ]
                    """);
            Files.writeString(TEST_DATA_DIR.resolve("locations.json"), """
                    [
                      {
                        "id": 1,
                        "name": "Centru",
                        "address": "Str. Principala 1",
                        "zones": [
                          { "id": 101, "name": "Sala Cursuri", "maxCapacity": 15 },
                          { "id": 102, "name": "Fitness Liber", "maxCapacity": 50 }
                        ]
                      }
                    ]
                    """);
            writeEmptyJsonArray("courses.json");
            writeEmptyJsonArray("signups.json");
            writeEmptyJsonArray("waitlistEntries.json");
            writeEmptyJsonArray("members.json");
            writeEmptyJsonArray("subscriptions.json");
            writeEmptyJsonArray("payments.json");
            writeEmptyJsonArray("receipts.json");
            writeEmptyJsonArray("checkins.json");
        } catch (IOException e) {
            throw new RuntimeException("Could not prepare JSON test files", e);
        }
    }

    private static void writeEmptyJsonArray(String fileName) throws IOException {
        Files.writeString(TEST_DATA_DIR.resolve(fileName), "[]");
    }
}
