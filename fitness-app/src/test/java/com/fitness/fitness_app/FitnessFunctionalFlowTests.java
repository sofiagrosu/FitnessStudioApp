package com.fitness.fitness_app;

import com.fitness.fitness_app.exception.ConflictException;
import com.fitness.fitness_app.exception.NotFoundException;
import com.fitness.fitness_app.model.*;
import com.fitness.fitness_app.model.enums.PaymentMethod;
import com.fitness.fitness_app.model.enums.Role;
import com.fitness.fitness_app.model.enums.SubscriptionStatus;
import com.fitness.fitness_app.model.enums.SubscriptionType;
import com.fitness.fitness_app.repository.*;
import com.fitness.fitness_app.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired private UserRepository userRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long memberId;
    private String memberQrCode;
    private Long subscriptionId;
    private Long courseId;
    private Long trainerId;
    private Long locationId;

    @BeforeAll
    void cleanUpPreviousTestData() {
        // Sterge datele lasate de rulari anterioare, in ordinea corecta a FK-urilor.
        // Foloseste multi-table DELETE MySQL pentru a evita subquery-uri pe acelasi tabel.

        // 1. receipts -> payments -> member
        jdbcTemplate.update("""
            DELETE r FROM receipts r
            JOIN payments p ON r.payment_id = p.id
            JOIN users u ON p.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 2. waitlist_entries -> member
        jdbcTemplate.update("""
            DELETE we FROM waitlist_entries we
            JOIN users u ON we.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 3. sign_ups -> member
        jdbcTemplate.update("""
            DELETE su FROM sign_ups su
            JOIN users u ON su.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 4. check_ins -> member
        jdbcTemplate.update("""
            DELETE ci FROM check_ins ci
            JOIN users u ON ci.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 5. payments -> member
        jdbcTemplate.update("""
            DELETE p FROM payments p
            JOIN users u ON p.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 6. subscriptions -> member
        jdbcTemplate.update("""
            DELETE s FROM subscriptions s
            JOIN users u ON s.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);

        // 7. waitlist_entries pentru cursurile trainerului de test
        jdbcTemplate.update("""
            DELETE we FROM waitlist_entries we
            JOIN courses c ON we.course_id = c.id
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);

        // 8. sign_ups pentru cursurile trainerului de test
        jdbcTemplate.update("""
            DELETE su FROM sign_ups su
            JOIN courses c ON su.course_id = c.id
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);

        // 9. cursurile trainerului de test
        jdbcTemplate.update("""
            DELETE c FROM courses c
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);

        // 10. locatia de test
        jdbcTemplate.update("""
            DELETE FROM locations
            WHERE name = 'Centru' AND address = 'Str. Principala 1'
        """);

        // 12. sub-tabelele JPA (JOINED inheritance)
        jdbcTemplate.update("""
            DELETE m FROM members m
            JOIN users u ON m.id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE t FROM trainers t
            JOIN users u ON t.id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);

        // 13. randul din tabela de baza users
        jdbcTemplate.update("""
            DELETE FROM users
            WHERE email IN ('trainer.functional@test.com','elena.functional@test.com','maria.functional@test.com')
        """);
    }

    @AfterAll
    void cleanUpAfterTests() {
        jdbcTemplate.update("""
            DELETE r FROM receipts r
            JOIN payments p ON r.payment_id = p.id
            JOIN users u ON p.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE we FROM waitlist_entries we
            JOIN users u ON we.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE su FROM sign_ups su
            JOIN users u ON su.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE ci FROM check_ins ci
            JOIN users u ON ci.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE p FROM payments p
            JOIN users u ON p.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE s FROM subscriptions s
            JOIN users u ON s.member_id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE we FROM waitlist_entries we
            JOIN courses c ON we.course_id = c.id
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);
        jdbcTemplate.update("""
            DELETE su FROM sign_ups su
            JOIN courses c ON su.course_id = c.id
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);
        jdbcTemplate.update("""
            DELETE c FROM courses c
            JOIN users u ON c.trainer_id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);
        jdbcTemplate.update("""
            DELETE FROM locations
            WHERE name = 'Centru' AND address = 'Str. Principala 1'
        """);
        jdbcTemplate.update("""
            DELETE m FROM members m
            JOIN users u ON m.id = u.id
            WHERE u.email IN ('elena.functional@test.com','maria.functional@test.com')
        """);
        jdbcTemplate.update("""
            DELETE t FROM trainers t
            JOIN users u ON t.id = u.id
            WHERE u.email = 'trainer.functional@test.com'
        """);
        jdbcTemplate.update("""
            DELETE FROM users
            WHERE email IN ('trainer.functional@test.com','elena.functional@test.com','maria.functional@test.com')
        """);
    }

    @DynamicPropertySource
    static void configureTestDataFiles(DynamicPropertyRegistry registry) {
        prepareTestFiles();

        registry.add("data.subscriptions.path", () -> TEST_DATA_DIR.resolve("subscriptions.json").toString());
        registry.add("data.payments.path", () -> TEST_DATA_DIR.resolve("payments.json").toString());
        registry.add("data.receipts.path", () -> TEST_DATA_DIR.resolve("receipts.json").toString());
        registry.add("data.checkins.path", () -> TEST_DATA_DIR.resolve("checkins.json").toString());

        registry.add("data.users.path", () -> TEST_DATA_DIR.resolve("users.json").toString());
        registry.add("data.locations.path", () -> TEST_DATA_DIR.resolve("locations.json").toString());
        registry.add("data.courses.path", () -> TEST_DATA_DIR.resolve("courses.json").toString());
        registry.add("data.signups.path", () -> TEST_DATA_DIR.resolve("signups.json").toString());
        registry.add("data.waitlist.path", () -> TEST_DATA_DIR.resolve("waitlistEntries.json").toString());
        registry.add("data.members.path", () -> TEST_DATA_DIR.resolve("members.json").toString());
    }

    @Test
    @Order(1)
    void seedTrainerAndLocation() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Ana");
        trainer.setLastName("Trainer");
        trainer.setEmail("trainer.functional@test.com");
        trainer.setPassword("trainer123");
        trainer.setActive(true);

        Trainer savedTrainer = (Trainer) userRepository.save(trainer);
        this.trainerId = savedTrainer.getId();

        Location location = new Location();
        location.setName("Centru");
        location.setAddress("Str. Principala 1");

        Location savedLocation = locationRepository.save(location);
        this.locationId = savedLocation.getId();

        assertNotNull(trainerId);
        assertNotNull(locationId);
    }

    @Test
    @Order(2)
    void memberCanRegisterLoginChangePasswordAndIsPersistedCorrectly() {
        Member member = new Member();
        member.setFirstName("Elena");
        member.setLastName("Marinescu");
        member.setEmail("elena.functional@test.com");
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

        UserI loggedIn = authService.login("elena.functional@test.com", "initial123");
        assertNotNull(loggedIn);
        assertEquals(Role.MEMBER, loggedIn.getRole());

        memberService.changePassword(memberId, "initial123", "newpass123");

        assertNull(authService.login("elena.functional@test.com", "initial123"));
        assertNotNull(authService.login("elena.functional@test.com", "newpass123"));
    }

    @Test
    @Order(3)
    void subscriptionPaymentReceiptAndQrCheckInFlowWorks() {
        Subscription subscription = subscriptionService.createSubscription(
                memberId,
                SubscriptionType.TEN_ENTRIES,
                100.0
        );

        assertNotNull(subscription.getId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(10, subscription.getRemainingEntries());

        this.subscriptionId = subscription.getId();

        assertThrows(ConflictException.class,
                () -> subscriptionService.createSubscription(memberId, SubscriptionType.MONTHLY, 150.0));

        Payment payment = paymentService.registerPayment(
                memberId,
                subscriptionId,
                100.0,
                PaymentMethod.CARD
        );

        assertNotNull(payment.getId());
        assertEquals(memberId, payment.getMemberId());
        assertEquals(subscriptionId, payment.getSubscriptionId());

        Receipt receipt = paymentService.getReceiptForPayment(payment.getId());

        assertNotNull(receipt);
        assertTrue(receipt.getReceiptNumber().startsWith("REC-"));

        CheckInResult result = checkInService.checkInByQrCode(memberQrCode, locationId);

        assertTrue(result.isAllowed());
        assertEquals("GREEN", result.getScreenColor());
        assertNotNull(result.getCheckIn());

        Subscription afterCheckIn = subscriptionService.getSubscriptionById(subscriptionId);
        assertEquals(9, afterCheckIn.getRemainingEntries());

        CheckInResult duplicate = checkInService.checkInByQrCode(memberQrCode, locationId);

        assertFalse(duplicate.isAllowed());
        assertEquals("RED", duplicate.getScreenColor());

        CheckIn closed = checkInService.checkOut(result.getCheckIn().getId());
        assertNotNull(closed.getCheckOutTime());

    }

    @Test
    @Order(4)
    void coursesSignUpsWaitlistAttendanceAndValidationWork() {
        Trainer trainer = (Trainer) userRepository.findById(trainerId)
                .orElseThrow();

        Location location = locationRepository.findById(locationId)
                .orElseThrow();

        Course course = new Course();
        course.setTrainer(trainer);
        course.setLocation(location);
        course.setName("Morning Yoga");
        course.setType(CourseType.YOGA);
        course.setDayOfWeek(DayOfWeek.MONDAY);
        course.setStartTime(LocalTime.of(10, 0));
        course.setDuration(60);
        course.setMaxCapacity(1);
        course.setRecurring(true);
        course.setCurrentOccupancy(99);

        Course createdCourse = coursesService.createCourse(course);

        assertNotNull(createdCourse);
        assertNotNull(createdCourse.getId());
        assertEquals(0, createdCourse.getCurrentOccupancy());

        this.courseId = createdCourse.getId();

        Course invalidTrainerCourse = new Course();
        invalidTrainerCourse.setTrainer(new Trainer());
        invalidTrainerCourse.getTrainer().setRole(Role.TRAINER);
        invalidTrainerCourse.setLocation(location);
        invalidTrainerCourse.setName("Invalid Trainer");
        invalidTrainerCourse.setType(CourseType.YOGA);
        invalidTrainerCourse.setDayOfWeek(DayOfWeek.TUESDAY);
        invalidTrainerCourse.setStartTime(LocalTime.of(12, 0));
        invalidTrainerCourse.setDuration(60);
        invalidTrainerCourse.setMaxCapacity(10);
        invalidTrainerCourse.setRecurring(true);

        assertThrows(Exception.class, () -> coursesService.createCourse(invalidTrainerCourse));

        Course invalidLocationCourse = new Course();
        invalidLocationCourse.setTrainer(trainer);
        invalidLocationCourse.setLocation(new Location());
        invalidLocationCourse.setName("Invalid Location");
        invalidLocationCourse.setType(CourseType.YOGA);
        invalidLocationCourse.setDayOfWeek(DayOfWeek.TUESDAY);
        invalidLocationCourse.setStartTime(LocalTime.of(12, 0));
        invalidLocationCourse.setDuration(60);
        invalidLocationCourse.setMaxCapacity(10);
        invalidLocationCourse.setRecurring(true);

        assertThrows(Exception.class, () -> coursesService.createCourse(invalidLocationCourse));

        String signupMessage = coursesService.createSignUp(memberId, courseId);

        assertTrue(signupMessage.contains("successfully"));
        assertEquals(1, coursesRepository.findById(courseId).orElseThrow().getCurrentOccupancy());

        SignUp enrolled = signUpsRepository.findByCourse_IdAndMember_Id(courseId, memberId);

        assertNotNull(enrolled);
        assertNotNull(enrolled.getId());
        assertEquals("ENROLLED", coursesService.getSignUpStatus(courseId, memberId).status());

        Member secondMember = new Member();
        secondMember.setFirstName("Maria");
        secondMember.setLastName("Ionescu");
        secondMember.setEmail("maria.functional@test.com");
        secondMember.setPassword("maria123");
        secondMember.setPhone("0733333333");

        Member registeredSecondMember = authService.register(secondMember);

        // Give the second member a paid subscription so they can join a course
        Subscription secondSubscription = subscriptionService.createSubscription(
                registeredSecondMember.getId(),
                SubscriptionType.MONTHLY,
                150.0
        );
        paymentService.registerPayment(
                registeredSecondMember.getId(),
                secondSubscription.getId(),
                150.0,
                PaymentMethod.CARD
        );

        String waitlistMessage = coursesService.createSignUp(registeredSecondMember.getId(), courseId);

        assertTrue(waitlistMessage.contains("waitlist"));
        assertEquals("WAITLISTED", coursesService.getSignUpStatus(courseId, registeredSecondMember.getId()).status());
        assertEquals(1, waitlistsRepository.findByCourse_IdOrderByPositionAsc(courseId).size());

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

            writeEmptyJsonArray("users.json");
            writeEmptyJsonArray("locations.json");
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