# --- !Ups

CREATE TABLE "stand_ups" (
    "id"  BIGSERIAL NOT NULL,
    "name" VARCHAR(100) NOT NULL UNIQUE,
    "display_name" VARCHAR(100) NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "teams" (
     "id"  BIGSERIAL NOT NULL,
     "name" VARCHAR(100) NOT NULL UNIQUE,
     "speaker" VARCHAR(100) NOT NULL,
     "allocation_in_seconds" INT NOT NULL CHECK ( "allocation_in_seconds" > 0 ),
     "stand_up_id" BIGSERIAL NOT NULL REFERENCES stand_ups(id),
     PRIMARY KEY ("id")
);

-- INSERT INTO "stand_ups"("id", "name", "display_name") VALUES(1, "main", "Access UK Main Standup");
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Releases", "Steff", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Fes", "Fred", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("L3", "David", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Out of country", "Victor", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("CI", "Katie", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Tech CI", "Dominic", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Standard Sections", "Iuliana", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Small projects", "Jeremy", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("CWI", "Shiv", 45, 1);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Actions", "Matt", 45, 1);
--
-- INSERT INTO "stand_ups"("id", "name", "display_name") VALUES(2, "ba", "Access UK BA Standup");
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Katie", "Katie", 90, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Kate", "Kate", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Samier", "Samier", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Jeremy", "Jeremy", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Fred", "Fred", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Harry", "Harry", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Alice", "Alice", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Dean", "Dean", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Eoin", "Eoin", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Victor", "Victor", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Fred", "Fred", 45, 2);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Jamie", "Jamie", 45, 2);
--
-- INSERT INTO "stand_ups"("id", "name", "display_name") VALUES(3, "team5", "Access UK Team 5 Standup");
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Cristi", 90, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Tiberiu", 45, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Alan", 45, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Alejandro", 45, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Raaj", 45, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Ajay", 45, 3);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Jeremy", 45, 3);
--
-- INSERT INTO "stand_ups"("id", "name", "display_name") VALUES(4, "dev", "Access UK Dev Symposium");
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Releases", "Steff", 90, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("L3", "David", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("CWI", "Shiv", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Out of Country", "Alua", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Standard Sections", "Daniel N", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Tech CI", "Dom / Parvez", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("CI", "Rahul / Elliot", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Fes", "Daniel T", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("Team 5", "Alan / Alejandro", 45, 4);
-- INSERT INTO "teams"("name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES("EEA FP", "Ethan /Adam", 45, 4);


-- !Downs

DROP TABLE "teams";
DROP TABLE "stand_ups";