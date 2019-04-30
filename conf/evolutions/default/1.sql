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

INSERT INTO "stand_ups"("id", "name", "display_name") VALUES('1', 'All hands', 'All Hands');
INSERT INTO "teams"("id", "name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES('1', 'team 1', 'scrum master team 1', '120' ,'1');
INSERT INTO "teams"("id", "name", "speaker", "allocation_in_seconds", "stand_up_id") VALUES('2', 'team 2', 'scrum master team 2', '120' ,'1');

-- !Downs

DROP TABLE "teams";
DROP TABLE "stand_ups";