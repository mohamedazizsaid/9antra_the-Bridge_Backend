package com._antra.the_bridge.config;

import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import com._antra.the_bridge.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final PhaseRepository phaseRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final EvaluationRepository evaluationRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      FormationRepository formationRepository,
                      PhaseRepository phaseRepository,
                      SessionRepository sessionRepository,
                      EnrollmentRepository enrollmentRepository,
                      PaymentRepository paymentRepository,
                      EvaluationRepository evaluationRepository,
                      NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.phaseRepository = phaseRepository;
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentRepository = paymentRepository;
        this.evaluationRepository = evaluationRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("=== Bridge DataSeeder: Initialisation des données ===");

        // ─── 1. Create Users ─────────────────────────────────────────────────────
        createUser("Karim", "Bennani", "karim.bennani@9antra.tn",
                "admin123", Role.ADMIN, Status.ACTIVE, 40,
                "https://api.dicebear.com/7.x/initials/svg?seed=KB&backgroundColor=c62761");

        User formateur1 = createUser("Amine", "Hadj", "amine.hadj@9antra.tn",
                "form123", Role.FORMATEUR, Status.ACTIVE, 35,
                "https://api.dicebear.com/7.x/initials/svg?seed=AH&backgroundColor=c62761");

        User formateur2 = createUser("Sonia", "Belhadj", "sonia.belhadj@9antra.tn",
                "form123", Role.FORMATEUR, Status.ACTIVE, 32,
                "https://api.dicebear.com/7.x/initials/svg?seed=SB&backgroundColor=f5a623");

        User[] stagiaires = {
            createUser("Mohamed", "Trabelsi", "m.trabelsi@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 24,
                    "https://api.dicebear.com/7.x/initials/svg?seed=MT&backgroundColor=3b82f6"),
            createUser("Fatma", "Zahra", "f.zahra@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 22,
                    "https://api.dicebear.com/7.x/initials/svg?seed=FZ&backgroundColor=8b5cf6"),
            createUser("Yassine", "Khelifi", "y.khelifi@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 26,
                    "https://api.dicebear.com/7.x/initials/svg?seed=YK&backgroundColor=10b981"),
            createUser("Nour", "Mghirbi", "n.mghirbi@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 23,
                    "https://api.dicebear.com/7.x/initials/svg?seed=NM&backgroundColor=f59e0b"),
            createUser("Samir", "Benhassine", "s.benhassine@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 28,
                    "https://api.dicebear.com/7.x/initials/svg?seed=SB2&backgroundColor=ef4444"),
            createUser("Rim", "Chatti", "r.chatti@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 21,
                    "https://api.dicebear.com/7.x/initials/svg?seed=RC&backgroundColor=06b6d4"),
            createUser("Bilel", "Ayari", "b.ayari@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 25,
                    "https://api.dicebear.com/7.x/initials/svg?seed=BA&backgroundColor=84cc16"),
            createUser("Houda", "Sassi", "h.sassi@email.com", "pass123", Role.STAGIAIRE, Status.ACTIVE, 27,
                    "https://api.dicebear.com/7.x/initials/svg?seed=HS&backgroundColor=f472b6"),
        };

        // ─── 2. Create Formations ─────────────────────────────────────────────────
        Formation f1 = createFormation("Développement Full Stack — Spring Boot & Angular",
                "Maîtrisez l'architecture microservices et le développement full stack moderne avec Spring Boot et Angular. Un parcours intensif pour devenir un développeur backend/frontend compétitif.",
                "Développement Web", 1800.0, List.of(formateur1));

        Formation f2 = createFormation("Data Science & Machine Learning avec Python",
                "Plongez dans le monde de la data science : analyse exploratoire, modélisation ML, deep learning et déploiement de modèles en production avec Python, Pandas, Scikit-learn et TensorFlow.",
                "Data & IA", 2200.0, List.of(formateur2));

        Formation f3 = createFormation("DevOps & Cloud Engineering — AWS & Docker",
                "Formation DevOps complète : CI/CD, containerisation Docker/Kubernetes, infrastructure as code avec Terraform et déploiement cloud AWS. Pour les ingénieurs qui veulent industrialiser leurs livraisons.",
                "DevOps & Cloud", 2500.0, List.of(formateur1, formateur2));

        // ─── 3. Create Phases for Formation 1 ────────────────────────────────────
        Phase f1p1 = createPhase(f1, 1, "Fondamentaux Java & Spring Boot",
                "Architecture REST, JPA/Hibernate, Spring Security, JWT. Conception d'APIs robustes et sécurisées.", 600.0);
        Phase f1p2 = createPhase(f1, 2, "Angular & Architecture Frontend",
                "Composants, Services, RxJS, State Management. Connexion au backend et déploiement d'applications SPA.", 700.0);
        Phase f1p3 = createPhase(f1, 3, "Projet Fil Rouge & Déploiement",
                "Conception et réalisation d'une application complète. Docker, CI/CD avec GitHub Actions, déploiement AWS.", 500.0);

        // ─── 4. Create Phases for Formation 2 ────────────────────────────────────
        Phase f2p1 = createPhase(f2, 1, "Python & Analyse de Données",
                "NumPy, Pandas, Matplotlib. Manipulation et visualisation de données réelles.", 700.0);
        Phase f2p2 = createPhase(f2, 2, "Machine Learning Appliqué",
                "Régression, Classification, Clustering, Validation croisée. Scikit-learn et XGBoost.", 800.0);
        Phase f2p3 = createPhase(f2, 3, "Deep Learning & Mise en Production",
                "Réseaux de neurones avec Keras/TensorFlow, MLOps avec MLflow, déploiement FastAPI.", 700.0);

        // ─── 5. Create Phases for Formation 3 ────────────────────────────────────
        Phase f3p1 = createPhase(f3, 1, "Containerisation Docker & Kubernetes",
                "Images Docker, Docker Compose, Kubernetes — Pods, Services, Deployments, Helm Charts.", 800.0);
        Phase f3p2 = createPhase(f3, 2, "CI/CD & Infrastructure as Code",
                "GitHub Actions, GitLab CI, Terraform pour AWS. Automatisation complète du cycle de livraison.", 950.0);
        Phase f3p3 = createPhase(f3, 3, "Cloud AWS — Architecture & Sécurité",
                "EC2, S3, RDS, Lambda, IAM, VPC. Certifications AWS Solutions Architect préparation.", 750.0);

        // ─── 6. Create Sessions ───────────────────────────────────────────────────
        // Formation 1 - Phase 1 sessions
        createSession(f1p1, LocalDate.now().minusDays(30), "09:00", 3, "Salle Alpha", null);
        createSession(f1p1, LocalDate.now().minusDays(23), "09:00", 3, "Salle Alpha", null);
        createSession(f1p1, LocalDate.now().minusDays(16), "14:00", 3, "Salle Alpha", null);
        createSession(f1p1, LocalDate.now().minusDays(9), "09:00", 3, "Salle Alpha", null);

        // Formation 1 - Phase 2 sessions
        createSession(f1p2, LocalDate.now().minusDays(5), "09:00", 3, "Salle Beta", null);
        createSession(f1p2, LocalDate.now(), "10:00", 3, "Salle Beta", null);
        createSession(f1p2, LocalDate.now().plusDays(7), "09:00", 3, "Salle Beta", null);
        createSession(f1p2, LocalDate.now().plusDays(14), "14:00", 3, "Salle Beta", "https://meet.google.com/bridge-f1-p2");

        // Formation 1 - Phase 3 sessions
        createSession(f1p3, LocalDate.now().plusDays(21), "09:00", 4, "Lab Cloud", "https://meet.google.com/bridge-f1-p3");
        createSession(f1p3, LocalDate.now().plusDays(28), "09:00", 4, "Lab Cloud", "https://meet.google.com/bridge-f1-p3");

        // Formation 2 - Phase 1 sessions
        createSession(f2p1, LocalDate.now().minusDays(20), "09:00", 3, "Salle Data", null);
        createSession(f2p1, LocalDate.now().minusDays(13), "09:00", 3, "Salle Data", null);
        createSession(f2p1, LocalDate.now().plusDays(1), "14:00", 3, "Salle Data", null);

        // Formation 3 - Phase 1 sessions
        createSession(f3p1, LocalDate.now().plusDays(3), "09:00", 4, "Lab DevOps", null);
        createSession(f3p1, LocalDate.now().plusDays(10), "09:00", 4, "Lab DevOps", "https://meet.google.com/bridge-f3-p1");

        // ─── 7. Enroll Students ───────────────────────────────────────────────────
        // Formation 1: 5 students
        enrollStudent(stagiaires[0], f1, List.of(f1p1, f1p2, f1p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[1], f1, List.of(f1p1, f1p2, f1p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[2], f1, List.of(f1p1, f1p2, f1p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[3], f1, List.of(f1p1, f1p2, f1p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[4], f1, List.of(f1p1, f1p2, f1p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);

        // Formation 2: 4 students
        enrollStudent(stagiaires[2], f2, List.of(f2p1, f2p2, f2p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[5], f2, List.of(f2p1, f2p2, f2p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[6], f2, List.of(f2p1, f2p2, f2p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[7], f2, List.of(f2p1, f2p2, f2p3), PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.PENDING);

        // Formation 3: 3 students
        enrollStudent(stagiaires[0], f3, List.of(f3p1, f3p2, f3p3), PaymentStatus.PENDING, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[1], f3, List.of(f3p1, f3p2, f3p3), PaymentStatus.PENDING, PaymentStatus.PENDING, PaymentStatus.PENDING);
        enrollStudent(stagiaires[4], f3, List.of(f3p1, f3p2, f3p3), PaymentStatus.PENDING, PaymentStatus.PENDING, PaymentStatus.PENDING);

        // ─── 8. Create Evaluations for Phase 1 of Formation 1 ────────────────────
        createEvaluation(formateur1, stagiaires[0], f1p1, 17.5,
                "Excellent travail. Maîtrise parfaite des fondamentaux Spring Boot. APIs bien structurées, sécurisation JWT impeccable.",
                "Spring Boot, JPA, Spring Security, JWT, REST API");
        createEvaluation(formateur1, stagiaires[1], f1p1, 15.0,
                "Très bon niveau. Quelques erreurs de gestion d'exceptions mais l'ensemble est solide.",
                "Spring Boot, JPA, REST API, Maven");
        createEvaluation(formateur1, stagiaires[2], f1p1, 14.0,
                "Bon travail. L'architecture REST est correcte, des améliorations à faire sur la sécurité.",
                "Spring Boot, JPA, REST API");
        createEvaluation(formateur1, stagiaires[3], f1p1, 12.5,
                "Niveau satisfaisant. Doit approfondir la gestion des relations JPA et les concepts de sécurité.",
                "Spring Boot, JPA");
        createEvaluation(formateur1, stagiaires[4], f1p1, 18.0,
                "Performance remarquable. Code propre, architecture exemplaire, très bonne maîtrise de Spring Security.",
                "Spring Boot, JPA, Spring Security, JWT, Docker, Clean Code");

        // Phase 1 de Formation 2
        createEvaluation(formateur2, stagiaires[5], f2p1, 16.0,
                "Excellente maîtrise de Pandas et NumPy. Visualisations très propres et pertinentes.",
                "Python, Pandas, NumPy, Matplotlib, Seaborn");
        createEvaluation(formateur2, stagiaires[6], f2p1, 13.0,
                "Bon niveau général. L'analyse exploratoire est correcte mais les visualisations peuvent être améliorées.",
                "Python, Pandas, NumPy");
        createEvaluation(formateur2, stagiaires[7], f2p1, 15.5,
                "Très bon travail. Excellente maîtrise de la manipulation de données complexes.",
                "Python, Pandas, NumPy, Matplotlib");

        // ─── 9. Create Notifications ──────────────────────────────────────────────
        createNotification(formateur1, "🎓 Nouvelle Inscription",
                "Mohamed Trabelsi vient de rejoindre votre formation Full Stack.");
        createNotification(formateur1, "📋 Séance Aujourd'hui",
                "Rappel : vous avez une séance Angular & Architecture Frontend ce matin à 10h00 en Salle Beta.");
        createNotification(formateur2, "⭐ Évaluation requise",
                "Les stagiaires de la phase Python & Analyse de Données attendent leur évaluation.");
        createNotification(stagiaires[0], "✅ Inscription confirmée",
                "Votre inscription à la formation Full Stack est confirmée. Bienvenue chez 9antra The Bridge !");
        createNotification(stagiaires[1], "📚 Nouvelle phase débloquée",
                "Félicitations ! La phase Angular & Architecture Frontend est maintenant accessible.");
        createNotification(stagiaires[4], "🏅 Évaluation publiée",
                "Dr. Amine Hadj a publié votre évaluation de phase. Note : 18/20 — Excellent !");

        System.out.println("=== Bridge DataSeeder: Données initialisées avec succès ===");
    }

    // ─── Helper Methods ───────────────────────────────────────────────────────────

    private User createUser(String firstName, String lastName, String email,
                             String password, Role role, Status status, int age, String avatar) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(status);
        user.setAge(age);
        user.setAvatar(avatar);
        user.setEmailVerified(true);
        user.setAuthProvider("LOCAL");
        user.setCreatedAt(LocalDate.now().minusDays((long)(Math.random() * 90)));
        return userRepository.save(user);
    }

    private Formation createFormation(String title, String description, String category,
                                       Double totalPrice, List<User> trainers) {
        Formation f = new Formation();
        f.setTitle(title);
        f.setDescription(description);
        f.setCategory(category);
        f.setTotalPrice(totalPrice);
        f.setTrainers(trainers);
        return formationRepository.save(f);
    }

    private Phase createPhase(Formation formation, int order, String title,
                               String content, Double price) {
        Phase p = new Phase();
        p.setFormation(formation);
        p.setPhaseOrder(order);
        p.setTitle(title);
        p.setContent(content);
        p.setPrice(price);
        p.setMinimumAttendance(75.0);
        p.setMinimumGrade(12.0);
        return phaseRepository.save(p);
    }

    private Session createSession(Phase phase, LocalDate date, String startTime,
                                   int duration, String location, String meetingLink) {
        Session s = new Session();
        s.setPhase(phase);
        s.setSessionDate(date);
        s.setStartTime(LocalTime.parse(startTime));
        s.setDuration(duration);
        s.setLocation(location);
        s.setMeetingLink(meetingLink);
        return sessionRepository.save(s);
    }

    private void enrollStudent(User student, Formation formation, List<Phase> phases,
                                PaymentStatus... paymentStatuses) {
        if (enrollmentRepository.existsByStudentIdAndFormationId(student.getId(), formation.getId())) {
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setFormation(formation);
        enrollment.setEnrollmentDate(LocalDate.now().minusDays((long)(Math.random() * 60 + 5)));
        enrollmentRepository.save(enrollment);

        for (int i = 0; i < phases.size(); i++) {
            Phase phase = phases.get(i);
            Payment payment = new Payment();
            payment.setEnrollment(enrollment);
            payment.setPhase(phase);
            payment.setAmount(phase.getPrice() != null ? phase.getPrice() : 0.0);
            payment.setStatus(i < paymentStatuses.length ? paymentStatuses[i] : PaymentStatus.PENDING);
            payment.setDueDate(LocalDate.now().plusDays((long)(i * 30 + 5)));
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                payment.setPaymentDate(LocalDate.now().minusDays((long)(Math.random() * 20 + 1)));
                payment.setPaymentMethod("VIREMENT");
                payment.setTransactionReference("TXN-" + System.currentTimeMillis() + "-" + i);
            }
            paymentRepository.save(payment);
        }
    }

    private void createEvaluation(User trainer, User student, Phase phase,
                                   double grade, String comment, String skills) {
        if (evaluationRepository.findByStudentIdAndPhaseId(student.getId(), phase.getId()).isPresent()) {
            return;
        }
        Evaluation eval = new Evaluation();
        eval.setTrainer(trainer);
        eval.setStudent(student);
        eval.setPhase(phase);
        eval.setGrade(grade);
        eval.setComment(comment);
        eval.setSkills(skills);
        eval.setEvaluationDate(LocalDate.now().minusDays((long)(Math.random() * 7 + 1)));
        evaluationRepository.save(eval);
    }

    private void createNotification(User user, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setCreatedAt(java.time.LocalDateTime.now().minusHours((long)(Math.random() * 48)));
        n.setReadStatus(false);
        notificationRepository.save(n);
    }
}
