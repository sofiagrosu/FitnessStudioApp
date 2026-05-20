# Sistem Integrat de Management pentru Săli de Fitness

Acest proiect reprezintă o aplicație web de tip Enterprise destinată automatizării complete a activităților operaționale, logistice și financiare din cadrul unui club de fitness. Sistemul este construit pe o arhitectură decuplată Client-Server (Decoupled Client-Server Architecture) și utilizează o bază de date relațională centralizată pentru asigurarea consistenței datelor în timp real.

---

## Arhitectura Sistemului (3-Tier Architecture)

Aplicația este structurată pe trei niveluri independente, asigurând separarea clară a responsabilităților, mentenanță facilă și scalabilitate:

1. Nivelul de Prezentare (Frontend - React & Vite): O aplicație Single Page Application (SPA) responsabilă de interfața grafică și gestionarea stărilor vizuale. Comunicarea cu serverul se realizează exclusiv asincron prin cereri HTTP REST (via Axios).
2. Nivelul Logicii de Business (Backend - Spring Boot): Motorul central de decizie al aplicației. Acesta preia cererile prin Controllere REST, le procesează în cadrul stratului de Servicii conform regulilor clubului și manipulează stările entităților.
3. Nivelul de Persistență (Bază de Date - MySQL & Spring Data JPA): Abstractizează accesul la date și interogările SQL prin intermediul ORM-ului Hibernate, asigurând maparea directă a tabelelor relaționale în obiecte Java.

---

## Modelul de Date și Strategia de Moștenire

Pentru gestionarea utilizatorilor cu roluri și permisiuni distincte (Membru, Antrenor, Recepționer, Administrator), s-a implementat strategia de moștenire la nivel de tabel InheritanceType.JOINED:

* Tabela Părinte (users): Stochează exclusiv metadatele comune de securitate și autentificare (ID, email, parolă, nume, status și rol general).
* Tabelele Copil (members, trainers, receptionists, admins): Sunt legate de tabela părinte prin chei străine și conțin doar atributele strict specifice fiecărui rol (de exemplu, codul QR pentru membri sau specializările pentru antrenori). Această abordare normalizează baza de date și elimină redundanța datelor (valorile de tip NULL).

---

## Structura Componentelor Backend

Pentru fiecare modul funcțional (Membri, Cursuri, Plăți etc.), codul backend este structurat decuplat, respectând responsabilitățile specifice fiecărui strat:

### 1. Stratul de Expunere (REST Controllere)
Clasele adnotate cu `@RestController` acționează ca puncte de intrare (API Endpoints) pentru aplicația React. Rolul lor este de a intercepta cererile HTTP (GET, POST, PUT, DELETE), de a mapa datele primite în format JSON către obiecte de tip DTO (Data Transfer Object) și de a returna răspunsurile cu codurile de status HTTP corespunzătoare (200 OK, 201 Created, 400 Bad Request, 409 Conflict etc.).

### 2. Stratul de Servicii (Business Logic Services)
Cele 9 servicii ale aplicației (`AuthService`, `MemberService`, `TrainerService`, `AdminService`, `PaymentService`, `SubscriptionService`, `CheckInService`, `CoursesService`, `LocationService`) încapsulează în întregime regulile operaționale ale clubului. Acestea validează datele în mod fail-fast, controlează tranzițiile de stări și asigură atomicitatea operațiunilor complexe (cum ar fi managementul cozilor de așteptare).

### 3. Stratul de Acces la Date (Spring Data JPA Repositories)
Interfețele care extind `JpaRepository` (ex: `PaymentsRepository`, `ReceiptsRepository`, `LocationRepository`, `SignUpsRepository` etc.) abstractizează interacțiunea cu serverul MySQL. Ele oferă metode predefinite pentru operațiunile CRUD standard și permit generarea automată a interogărilor SQL complexe pe baza numelor metodelor (Query Methods), asigurând totodată protecție nativă împotriva atacurilor de tip SQL Injection.

---

## Bune Practici și Mecanisme Avansate

* Managementul Dependențelor Circulare (@Lazy): Previne blocarea contextului Spring la inițializare prin încărcarea leneșă a serviciilor interdependente, injectându-le sub formă de proxy în runtime doar la apelarea lor efectivă.
* Evaluare Leneșă a Statusurilor (Lazy Evaluation): Optimizează utilizarea resurselor hardware prin reîmprospătarea stărilor și expirarea abonamentelor direct în momentul interogării (just-in-time), eliminând necesitatea unor procese periodice grele de tip cron-job.
* Integritate Referențială Programatică: Previne coruperea datelor și excepțiile de cheie străină în MySQL prin executarea unor curățări și ștergeri manuale ordonate în cascadă din cod, eliminând entitățile copil înainte de ștergerea entității părinte.

---

## Instrucțiuni de Configurare și Rulare Locală

### 1. Configurare MySQL
1. Se creează o bază de date nouă cu numele fitness-app.
2. Se accesează fișierul application.properties din resursele backend-ului și se definesc parametrii de conexiune:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitness-app
spring.datasource.username=nume_utilizator
spring.datasource.password=parola_bazei_de_date
spring.jpa.hibernate.ddl-auto=update